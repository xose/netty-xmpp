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

package es.udc.pfc.xmpp.stanza;

/**
 * Registry of XMPP namespaces.
 */
public final class XMPPNamespaces {

	/** {@value} */
	public static final String CLIENT = "jabber:client";
	/** {@value} */
	public static final String SERVER = "jabber:server";

	/** {@value} */
	public static final String ACCEPT = "jabber:component:accept";
	/** {@value} */
	public static final String DATA = "jabber:x:data";
	/** {@value} */
	public static final String DELAY_LEGACY = "jabber:x:delay";
	/** {@value} */
	public static final String ROSTER = "jabber:iq:roster";
	/** {@value} */
	public static final String REGISTER = "jabber:iq:register";
	/** {@value} */
	public static final String SEARCH = "jabber:iq:search";
	/** {@value} */
	public static final String PRIVATE = "jabber:iq:private";
	/** {@value} */
	public static final String PRIVACY = "jabber:iq:privacy";

	/** {@value} */
	public static final String XBOSH = "urn:xmpp:xbosh";
	/** {@value} */
	public static final String DELAY = "urn:xmpp:delay";
	/** {@value} */
	public static final String SESSION = "urn:ietf:params:xml:ns:xmpp-session";
	/** {@value} */
	public static final String BIND = "urn:ietf:params:xml:ns:xmpp-bind";
	/** {@value} */
	public static final String SASL = "urn:ietf:params:xml:ns:xmpp-sasl";
	
	/** {@value} */
	public static final String STREAM = "http://etherx.jabber.org/streams";

	/** {@value} */
	public static final String DISCO_INFO = "http://jabber.org/protocol/disco#info";
	/** {@value} */
	public static final String DISCO_ITEMS = "http://jabber.org/protocol/disco#items";

	/** {@value} */
	public static final String MUC = "http://jabber.org/protocol/muc";
	/** {@value} */
	public static final String MUC_ADMIN = "http://jabber.org/protocol/muc#admin";
	/** {@value} */
	public static final String MUC_OWNER = "http://jabber.org/protocol/muc#owner";
	/** {@value} */
	public static final String MUC_USER = "http://jabber.org/protocol/muc#user";

	/** {@value} */
	public static final String HTTPBIND = "http://jabber.org/protocol/httpbind";
	/** {@value} */
	public static final String CHATSTATES = "http://jabber.org/protocol/chatstates";
	/** {@value} */
	public static final String NICK = "http://jabber.org/protocol/nick";

	private XMPPNamespaces() {
	}

}
