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

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.dom.DOMResult;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.w3c.dom.Document;

import com.fasterxml.aalto.stax.OutputFactoryImpl;

import es.udc.pfc.xmpp.xml.XMLElementImpl;
import es.udc.pfc.xmpp.xml.XMLUtil;

/**
 * Processes XML Events into XML Elements.
 */
public class XMLElementDecoder extends SimpleChannelUpstreamHandler {
	
	private static final XMLOutputFactory xmlOutputFactory = new OutputFactoryImpl();
	
	private final int skip;
	
	private XMLEventWriter writer;
	private DOMResult result;
	private Document document;
	private int depth;
	
	public XMLElementDecoder() {
		this(1);
	}
	
	public XMLElementDecoder(int skip) {
		this.skip = skip;
		resetWriter();
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (!(e.getMessage() instanceof XMLEvent)) {
			ctx.sendUpstream(e);
			return;
		}

		final XMLEvent event = (XMLEvent) e.getMessage();
		
		if (event.isStartDocument() || event.isEndDocument())
			return;
		
		if (depth < skip && event.isStartElement()) {
			ctx.sendUpstream(e);
			depth++;
			return;
		}
		
		if (depth <= skip && event.isEndElement()) {
			ctx.sendUpstream(e);
			depth--;
			return;
		}
		
		writer.add(event);

		if (event.isStartElement()) {
			depth++;
		} else if (event.isEndElement()) {
			depth--;

			if (depth == skip) {
				writer.flush();
				Channels.fireMessageReceived(ctx, XMLElementImpl.fromElement(document.getDocumentElement()));
				writer.close();
				resetWriter();
			}
		}
	}
	
	private void resetWriter() {
		try {
			document = XMLUtil.newDocument();
			result = new DOMResult(document);
			writer = xmlOutputFactory.createXMLEventWriter(result);
		} catch (XMLStreamException e) {
			throw new InternalError("Error creating writer");
		}
		
	}
	
}
