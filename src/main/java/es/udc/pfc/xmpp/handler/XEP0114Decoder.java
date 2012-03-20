/**
 * Copyright 2012 José Martínez
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.udc.pfc.xmpp.handler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.util.CharsetUtil;

import com.google.common.hash.Hashing;

import es.udc.pfc.xmpp.stanza.Stanza;
import es.udc.pfc.xmpp.stanza.XMPPNamespaces;
import es.udc.pfc.xmpp.xml.XMLElement;

/**
 * XEP-0114 Stream Decoder.
 */
public class XEP0114Decoder extends SimpleChannelHandler {

	private static final QName STREAM_NAME = new QName(XMPPNamespaces.STREAM, "stream", "stream");

	private static enum Status {
		CONNECT, AUTHENTICATE, READY, DISCONNECTED;
	}

	private Status status;
	private String serverName;
	private String secret;
	private String streamID;

	public XEP0114Decoder(String serverName, String secret) throws XMLStreamException {
		super();
		status = Status.CONNECT;

		this.serverName = serverName;
		this.secret = secret;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		Channels.write(ctx.getChannel(), ChannelBuffers.copiedBuffer("<stream:stream xmlns='jabber:component:accept' xmlns:stream='http://etherx.jabber.org/streams' to='" + serverName + "'>", CharsetUtil.UTF_8));

		ctx.sendUpstream(e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() instanceof XMLEvent) {
			final XMLEvent event = (XMLEvent) e.getMessage();

			switch (status) {
			case CONNECT:
				if (event.isStartElement()) {
					final StartElement element = event.asStartElement();

					if (STREAM_NAME.equals(element.getName()) && XMPPNamespaces.ACCEPT.equals(element.getNamespaceURI(null))) {
						if (!serverName.equals(element.getAttributeByName(new QName("from")).getValue())) {
							throw new Exception("server name mismatch");
						}
						streamID = element.getAttributeByName(new QName("id")).getValue();

						status = Status.AUTHENTICATE;
						Channels.write(ctx.getChannel(), ChannelBuffers.copiedBuffer("<handshake>" + Hashing.sha1().hashString(streamID + secret, CharsetUtil.UTF_8).toString() + "</handshake>", CharsetUtil.UTF_8));
					}
				} else {
					throw new Exception("Expected stream:stream element");
				}
				break;
			case AUTHENTICATE:
			case READY:
				if (event.isEndElement()) {
					final EndElement element = event.asEndElement();

					if (STREAM_NAME.equals(element.getName())) {
						System.out.println("end of stream");
						Channels.disconnect(ctx.getChannel());
						return;
					}
				}
				break;
			case DISCONNECTED:
				throw new Exception("received DISCONNECTED");
			}
		}
		else if (e.getMessage() instanceof XMLElement) {
			final XMLElement element = (XMLElement) e.getMessage();
			
			switch (status) {
			case AUTHENTICATE:
				if (!"handshake".equals(element.getTagName()))
					throw new Exception("expected handshake");
				status = Status.READY;
				System.out.println("logged in");
				break;
			case READY:
				final Stanza stanza = Stanza.fromElement(element);
				if (stanza == null)
					throw new Exception("Unknown stanza");

				Channels.fireMessageReceived(ctx, stanza);
				break;
			default:
				throw new Exception("unexpected handleElement");
			}
		}
		else {
			ctx.sendUpstream(e);
		}
	}

	@Override
	public void disconnectRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		Channels.write(ctx, e.getFuture(), ChannelBuffers.copiedBuffer("</stream:stream>", CharsetUtil.UTF_8));

		//ctx.sendDownstream(e);
	}

}
