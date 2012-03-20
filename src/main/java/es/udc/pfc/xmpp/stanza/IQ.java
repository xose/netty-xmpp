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

import javax.annotation.Nullable;

import es.udc.pfc.xmpp.xml.XMLElement;

/**
 * An Info/Query stanza.
 * 
 * @see <a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-semantics-iq">RFC 6120 - Section 8.2.3</a>
 */
public class IQ extends Stanza {

	/**
	 * Possible <i>type</i> values for IQs.
	 * 
	 * @see <a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-semantics-iq">RFC 6120 - Section 8.2.3</a>
	 */
	public static enum Type {
		get, set, result, error;
	}

	/**
	 * Creates a new IQ from a XML element.
	 * 
	 * No checks are done to the element, so it's only meant for internal use.
	 * 
	 * @param xml the XML element for this IQ
	 */
	public IQ(final XMLElement xml) {
		super(xml);
	}

	/**
	 * Create a new IQ with the given type.
	 * 
	 * @param type the type for the new IQ
	 */
	public IQ(final Type type) {
		super("iq");
		setType(type);
	}

	/**
	 * Returns the <i>type</i> attribute for this IQ.
	 * 
	 * @return the type for this stanza, or {@code null} if not found
	 */
	@Nullable
	public final Type getType() {
		try {
			return Type.valueOf(xml.getAttribute("type"));
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Sets a new <i>type</i> attribute for this IQ.
	 * 
	 * @param type the new type for this IQ
	 */
	public final void setType(final Type type) {
		xml.setAttribute("type", type.toString());
	}
	
	/**
	 * Checks if this is a request IQ.
	 * 
	 * @return true if this IQ is a request, false otherwise
	 */
	public final boolean isRequest() {
		return getType() == Type.get || getType() == Type.set;
	}
	
	/**
	 * Checks if this is a response IQ.
	 * 
	 * @return true if this IQ is a response, false otherwise
	 */
	public final boolean isResponse() {
		return getType() == Type.result || getType() == Type.error;
	}

	/**
	 * Retrieves the query from this IQ.
	 * 
	 * @param namespace the namespace of the query
	 * @return the query, or {@code null} if not found
	 */
	@Nullable
	public final XMLElement getQuery(final String namespace) {
		return getExtension("query", namespace);
	}
	
	/**
	 * Adds a new query to this element.
	 * 
	 * @param namespace the namespace of the query
	 * @return the new query
	 */
	public final XMLElement addQuery(final String namespace) {
		return addExtension("query", namespace);
	}
}
