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

package es.udc.pfc.xmpp.xml;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

/**
 * Helper class to build and parse XML elements.
 */
public final class XMLBuilder implements HasXML {
	
	/**
	 * Creates a new XMLElement with a given tag name.
	 * 
	 * @param name the tag name for the new XML element
	 * @return the new XMLElement
	 */
	public static final XMLBuilder create(final String name) {
		return new XMLBuilder(new XMLElementImpl(name));
	}
	
	/**
	 * Creates a new XMLElement with a given tag name and namespace.
	 * 
	 * @param name the tag name for the new XML element
	 * @param namespace the namespace for the new XML element
	 * @return the new XMLElement
	 */
	public static final XMLBuilder create(final String name, @Nullable final String namespace) {
		return new XMLBuilder(new XMLElementImpl(name, namespace));
	}
	
	private final XMLElement xml;
	
	private XMLBuilder(final XMLElement xml) {
		this.xml = checkNotNull(xml);
	}
	
	/**
	 * Adds a new attribute to the current element.
	 * 
	 * @param name the attribute name
	 * @param value the attribute value
	 * @return the same XMLBuilder
	 */
	public final XMLBuilder attribute(final String name, final String value) {
		xml.setAttribute(name, value);
		return this;
	}
	
	/**
	 * Adds a new child to the current element.
	 * 
	 * Note: unlike other child() methods, this one returns the same XMBLuider.
	 * 
	 * @param child the child to be added
	 * @return the same XMLBuilder
	 */
	public final XMLBuilder child(final HasXML child) {
		xml.addChild(child);
		return this;
	}
	
	/**
	 * Adds a new child with the given name.
	 * 
	 * @param name the tag name of the child to be added
	 * @return a new XMLBuilder for the child
	 */
	public final XMLBuilder child(final String name) {
		return new XMLBuilder(xml.addChild(name));
	}
	
	/**
	 * Adds a new child with the given name and namespace.
	 * 
	 * @param name the tag name for the child to be added
	 * @param namespace the namespace for the child to be added
	 * @return a new XMLBuilder for the child
	 */
	public final XMLBuilder child(final String name, final String namespace) {
		return new XMLBuilder(xml.addChild(name, namespace));
	}
	
	/**
	 * Adds a new child with the given text.
	 * 
	 * @param name the tag name for the child to be added
	 * @param text the text contents for the child to be added
	 * @return the same XMLBuilder
	 */
	public final XMLBuilder childText(final String name, final String text) {
		xml.setChildText(name, text);
		return this;
	}
	
	/**
	 * Sets the text contents of the current element.
	 * 
	 * @param text the text content to be set
	 * @return the same XMLBuilder
	 */
	public final XMLBuilder text(final String text) {
		xml.setText(text);
		return this;
	}
	
	/**
	 * Returns the parent builder.
	 * 
	 * @return the builder for this element's parent
	 */
	public final XMLBuilder parent() {
		final XMLElement parent = xml.getParent();
		return parent != null ? new XMLBuilder(parent) : this;
	}
	
	@Override
	public final XMLElement getXML() {
		return xml.getFirstParent();
	}
	
	@Override
	public final String toString() {
		return getXML().toString();
	}
	
}
