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

package es.udc.pfc.xmpp.component;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.logging.Logger;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import es.udc.pfc.xmpp.stanza.IQ;
import es.udc.pfc.xmpp.stanza.JID;
import es.udc.pfc.xmpp.stanza.Message;
import es.udc.pfc.xmpp.stanza.Presence;
import es.udc.pfc.xmpp.stanza.Stanza;

/**
 * Basic implementation of an XMPP component.
 */
public abstract class AbstractXMPPComponent implements XMPPComponent {
	
	protected static final Logger log = Logger.getLogger(XMPPComponent.class.getName());

	private final Map<String, SettableFuture<IQ>> futureHandlers;
	
	private Channel channel;
	private JID componentID;
	private JID serverID;
	
	protected AbstractXMPPComponent() {
		futureHandlers = Maps.newHashMap();
	}
	
	protected abstract void handleMessage(Message message);
	protected abstract void handlePresence(Presence presence);
	protected abstract IQ handleIQ(IQ iq);
	
	@Override
	public final void init(final Channel channel, final JID serverID, final JID componentID) {
		this.channel = checkNotNull(channel);
		this.componentID = checkNotNull(componentID);
		this.serverID = checkNotNull(serverID);
	}
	
	@Override
	public final JID getServerJID() {
		return serverID;
	}
	
	@Override
	public final JID getJID() {
		return componentID;
	}
	
	@Override
	public final void receivedMessage(final Message message) {
		checkNotNull(message);
		log.finest("Received message: " + message.toString());
		handleMessage(message);
	}
	
	@Override
	public final void receivedPresence(final Presence presence) {
		checkNotNull(presence);
		log.finest("Received presence: " + presence.toString());
		handlePresence(presence);
	}
	
	@Override
	public final void receivedIQ(final IQ iq) {
		checkNotNull(iq);
		log.finest("Received iq: " + iq.toString());
		if (iq.isRequest()) {
			final IQ response = handleIQ(iq);
			if (response == null) {
				log.warning("No IQ response");
				// TODO: send an error
				return;
			}
			
			send(response);
		}
		else if (iq.isResponse()) {
			final SettableFuture<IQ> future = futureHandlers.remove(iq.getId());
			if (future == null) {
				log.warning("No handler for ID " + iq.getId());
				return;
			}
			
			if (iq.getType() == IQ.Type.result) {
				future.set(iq);
			}
			else if (iq.getType() == IQ.Type.error) {
				future.setException(new Exception("Error IQ: " + iq.toString()));
			}
		}
		else {
			log.warning("IQ not request or response");
		}
	}

	/**
	 * Send a Stanza to the server.
	 * 
	 * @param stanza the Stanza to be sent
	 */
	public final void send(final Stanza stanza) {
		checkNotNull(stanza);
		if (channel == null || !channel.isConnected()) {
			log.warning("Disconnected, can't send stanza: " + stanza.toString());
			return;
		}
		
		log.info("Sending stanza: " + stanza.toString());
		Channels.write(channel, stanza);
	}
	
	/**
	 * Send an IQ request, handling the response using a Future.
	 * 
	 * @param iq the IQ request to be sent
	 * @return a Future 
	 */
	public final ListenableFuture<IQ> sendIQ(final IQ iq) {
		checkNotNull(iq);
		checkArgument(iq.isRequest());
		final SettableFuture<IQ> future = SettableFuture.create();
		// TODO: set random ID if missing
		futureHandlers.put(iq.getId(), future);
		send(iq);
		return future;
	}
	
	@Override
	public void connected() {
		log.fine("Connected");
	}

	@Override
	public void willDisconnect() {
		log.fine("Will disconnect");
	}

	@Override
	public void disconnected() {
		log.fine("Disconnected");
	}
	
}
