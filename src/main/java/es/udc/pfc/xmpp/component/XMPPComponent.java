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

import org.jboss.netty.channel.Channel;

import es.udc.pfc.xmpp.stanza.IQ;
import es.udc.pfc.xmpp.stanza.JID;
import es.udc.pfc.xmpp.stanza.Message;
import es.udc.pfc.xmpp.stanza.Presence;

/**
 * Represents an XMPP component
 */
public interface XMPPComponent {
	
	/**
	 * Called by the stream handler to initialize the component.
	 * 
	 * Do not call this function directly.
	 * 
	 * @param channel the Channel this component is bound to
	 * @param serverID the server's XMPP address
	 * @param componentID the component's XMPP address
	 */
	void init(Channel channel, JID serverID, JID componentID);
	
	/**
	 * Returns the server's XMPP address.
	 * 
	 * @return the server's XMPP address
	 */
	JID getServerJID();
	
	/**
	 * Returns the component's XMPP address.
	 * @return the component's XMPP address
	 */
	JID getJID();
	
	/**
	 * Returns the name of this component.
	 * 
	 * This is used for service discovery.
	 * 
	 * @return the name of this component
	 */
	String getName();
	
	/**
	 * Returns a short description of this component.
	 * 
	 * This is used for service discovery.
	 * 
	 * @return a short description of this component
	 */
	String getDescription();
	
	/**
	 * Called when this component connects to the server.
	 */
	void connected();
	
	/**
	 * Called before the component disconnects during a clean exit.
	 * 
	 * This might never be called. Stanzas can still be sent.
	 */
	void willDisconnect();
	
	/**
	 * Called when this component becomes disconnected.
	 * 
	 * No stanzas can be sent at this point.
	 */
	void disconnected();
	
	/**
	 * Called when a message is received.
	 * 
	 * @param message the received message
	 */
	void receivedMessage(Message message);
	
	/**
	 * Called when a presence is received.
	 * 
	 * @param presence the received presence
	 */
	void receivedPresence(Presence presence);
	
	/**
	 * Called when an IQ is received.
	 * 
	 * @param iq the received IQ
	 */
	void receivedIQ(IQ iq);
	
}
