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

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import es.udc.pfc.xmpp.xml.HasXML;
import es.udc.pfc.xmpp.xml.XMLBuilder;
import es.udc.pfc.xmpp.xml.XMLElement;

/**
 * Represents an XMPP Stanza.
 * 
 * Basic stanzas have <i>id</i>, <i>from</i> and <i>to</i> attributes,
 * as well as support for XML extensions.
 */
public abstract class Stanza implements HasXML {

	/**
	 * Holds the XML element associated with this stanza.
	 */
	protected final XMLElement xml;

	/**
	 * Creates a new stanza from an XML element.
	 * 
	 * No checks are done to the element, so it's only meant for internal use.
	 * 
	 * @param xml the XML element for this stanza
	 */
	protected Stanza(final XMLElement xml) {
		this.xml = checkNotNull(xml);
	}

	/**
	 * Create a new stanza with the given tag name.
	 * 
	 * @param name the tag name for this stanza
	 */
	protected Stanza(final String name) {
		this(XMLBuilder.create(name).getXML());
	}

	/**
	 * Creates a new stanza with the given tag name and namespace.
	 * 
	 * @param name the tag name for this stanza
	 * @param namespace the namespace for this stanza
	 */
	protected Stanza(final String name, final String namespace) {
		this(XMLBuilder.create(name, namespace).getXML());
	}

	/**
	 * Returns the <i>id</i> attribute for this stanza.
	 * 
	 * @return the ID for this stanza, or {@code null} if none
	 */
	@Nullable
	public final String getId() {
		return xml.getAttribute("id");
	}

	/**
	 * Sets a new <i>id</i> attribute for this stanza.
	 * 
	 * @param id the new ID for this stanza
	 */
	public final void setId(@Nullable final String id) {
		xml.setAttribute("id", id);
	}

	/**
	 * Returns the <i>from</i> attribute for this stanza.
	 * 
	 * @return the sender for this stanza, or {@code null} if none
	 */
	@Nullable
	public final JID getFrom() {
		return JID.jid(xml.getAttribute("from"));
	}

	/**
	 * Sets a new <i>from</i> attribute for this stanza.
	 * 
	 * @param from the new sender for this stanza
	 */
	public final void setFrom(@Nullable final JID from) {
		xml.setAttribute("from", from != null ? from.toString() : null);
	}

	/**
	 * Returns the <i>to</i> attribute for this stanza.
	 * 
	 * @return the recipient for this stanza, or {@code null} if none
	 */
	@Nullable
	public final JID getTo() {
		return JID.jid(xml.getAttribute("to"));
	}

	/**
	 * Sets a new <i>to</i> attribute for this stanza.
	 * 
	 * @param to the new recipient for this stanza
	 */
	public final void setTo(@Nullable final JID to) {
		xml.setAttribute("to", to != null ? to.toString() : null);
	}
	
	/**
	 * Retrieves a XML extension from this element.
	 * 
	 * @param name the name of the extension
	 * @param namespace the namespace of the extension
	 * @return the XML extension, or {@code null} if not found
	 */
	@Nullable
	public final XMLElement getExtension(final String name, final String namespace) {
		return xml.getFirstChild(name, namespace);
	}
	
	/**
	 * Adds a new XML extension to this element.
	 * 
	 * @param name the name of the extension
	 * @param namespace the namespace of the extension
	 * @return the new XML extension
	 */
	public final XMLElement addExtension(final String name, final String namespace) {
		return xml.addChild(name, namespace);
	}
	
	/**
	 * Creates a new Stanza of the proper type from a XMLElement.
	 * 
	 * @param element the XMLElement to convert into a Stanza
	 * @return the Stanza for the XMLElement
	 */
	@Nullable
	public static final Stanza fromElement(final XMLElement element) {
		final String type = element.getTagName();
		
		if ("message".equals(type)) {
			return new Message(element);
		}
		else if ("presence".equals(type)) {
			return new Presence(element);
		}
		else if ("iq".equals(type)) {
			return new IQ(element);
		}
		
		return null;
	}
	
	@Override
	public final XMLElement getXML() {
		return xml;
	}

	@Override
	public final String toString() {
		return xml.toString();
	}

}
