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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Java 6 implementation of XMLElement.
 */
public final class XMLElementImpl implements XMLElement {

	private final Document document;
	private final Element element;

	protected XMLElementImpl(final String name) {
		this(name, null);
	}

	protected XMLElementImpl(final String name, @Nullable final String namespace) {
		document = XMLUtil.newDocument();
		if (namespace != null) {
			element = document.createElementNS(namespace, name);
		} else {
			element = document.createElement(name);
		}
		document.appendChild(element);
	}

	protected XMLElementImpl(final Element element) {
		document = element.getOwnerDocument();
		this.element = element;
	}

	@Override
	public String getTagName() {
		return element.getTagName();
	}

	@Override
	public String getNamespace() {
		return element.getNamespaceURI();
	}

	@Override
	@Nullable
	public XMLElement getParent() {
		final Node parent = element.getParentNode();
		if (parent == null || !(parent instanceof Element))
			return null;

		return new XMLElementImpl((Element) parent);
	}

	@Override
	public XMLElement getFirstParent() {
		return new XMLElementImpl(document.getDocumentElement());
	}

	@Override
	public boolean hasAttribute(final String name) {
		return element.hasAttribute(checkNotNull(name));
	}

	@Override
	public ImmutableMap<String, String> getAttributes() {
		final ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
		final NamedNodeMap attribs = element.getAttributes();
		for (int i = 0; i < attribs.getLength(); i++) {
			final Attr attrib = (Attr) attribs.item(i);
			result.put(attrib.getName(), attrib.getValue());
		}
		return result.build();
	}
	
	@Override
	@Nullable
	public String getAttribute(final String name) {
		checkNotNull(name);

		if (!element.hasAttribute(name))
			return null;

		return element.getAttribute(name);
	}

	@Override
	public void setAttribute(final String name, @Nullable final String value) {
		checkNotNull(name);

		if (value != null) {
			element.setAttribute(name, value);
		} else {
			element.removeAttribute(name);
		}
	}

	@Override
	public boolean hasChild(final String name) {
		return hasChild(name, "*");
	}

	@Override
	public boolean hasChild(final String name, final String namespace) {
		return getFirstChild(name, namespace) != null;
	}

	@Override
	public XMLElement addChild(final String name) {
		return addChild(name, null);
	}

	@Override
	public XMLElement addChild(final String name, @Nullable final String namespace) {
		final Element newElement;
		if (namespace != null) {
			newElement = document.createElementNS(namespace, checkNotNull(name));
		} else {
			newElement = document.createElement(checkNotNull(name));
		}
		element.appendChild(newElement);
		return new XMLElementImpl(newElement);
	}

	@Override
	public XMLElement addChild(final HasXML child) {
		checkArgument(checkNotNull(child.getXML()) instanceof XMLElementImpl);

		final Element newElement = (Element) document.importNode(((XMLElementImpl) child.getXML()).element, true);
		element.appendChild(newElement);
		return new XMLElementImpl(newElement);
	}

	@Override
	@Nullable
	public XMLElement getFirstChild(final String name) {
		return getFirstChild(name, "*");
	}

	@Override
	@Nullable
	public XMLElement getFirstChild(final String name, final String namespace) {
		checkNotNull(name);
		checkNotNull(namespace);

		final NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final Node node = nodes.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			final Element element = (Element) node;
			if (!"*".equals(name) && !name.equals(element.getTagName())) {
				continue;
			}

			if (!"*".equals(namespace) && !namespace.equals(element.getNamespaceURI())) {
				continue;
			}

			return new XMLElementImpl(element);
		}

		return null;
	}

	@Override
	@Nullable
	public XMLElement getFirstChild(final Predicate<XMLElement> matcher) {
		checkNotNull(matcher);

		for (final XMLElement element : getChildren()) {
			if (matcher.apply(element))
				return element;
		}

		return null;
	}

	@Override
	public ImmutableList<XMLElement> getChildren() {
		return getChildren("*", "*");
	}

	@Override
	public ImmutableList<XMLElement> getChildren(final String name) {
		return getChildren(name, "*");
	}

	@Override
	public ImmutableList<XMLElement> getChildren(final String name, final String namespace) {
		checkNotNull(name);
		checkNotNull(namespace);

		final ImmutableList.Builder<XMLElement> result = ImmutableList.builder();

		final NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			final Node node = nodes.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			final Element element = (Element) node;
			if (!"*".equals(name) && !name.equals(element.getTagName())) {
				continue;
			}

			if (!"*".equals(namespace) && !namespace.equals(element.getNamespaceURI())) {
				continue;
			}

			result.add(new XMLElementImpl(element));
		}

		return result.build();
	}

	@Override
	public ImmutableList<XMLElement> getChildren(final Predicate<XMLElement> matcher) {
		checkNotNull(matcher);
		
		final ImmutableList.Builder<XMLElement> result = ImmutableList.builder();

		for (final XMLElement element : getChildren()) {
			if (matcher.apply(element)) {
				result.add(element);
			}
		}

		return result.build();
	}

	@Override
	public void removeChild(final HasXML child) {
		checkArgument(child.getXML() instanceof XMLElementImpl);
		
		element.removeChild(((XMLElementImpl) child.getXML()).element);
	}

	@Override
	public String getText() {
		return element.getTextContent();
	}

	@Override
	@Nullable
	public String getChildText(final String name) {
		return getChildText(name, "*");
	}

	@Override
	@Nullable
	public String getChildText(final String name, final String namespace) {
		final XMLElement child = getFirstChild(name, namespace);
		if (child == null)
			return null;

		return child.getText();
	}

	@Override
	public void setText(@Nullable final String text) {
		element.setTextContent(text);
	}

	@Override
	public void setChildText(final String name, @Nullable final String text) {
		setChildText(name, null, text);
	}

	@Override
	public void setChildText(final String name, @Nullable final String namespace, @Nullable final String text) {
		XMLElement child = getFirstChild(name, namespace != null ? namespace : "*");
		if (child == null) {
			child = addChild(name, namespace);
		}

		child.setText(text);
	}

	@Override
	public XMLElement getXML() {
		return this;
	}

	/**
	 * Parses a string into a XMLElement.
	 * 
	 * @param xml the string to parse
	 * @return the resulting element
	 */
	public static XMLElement fromString(final String xml) {
		return new XMLElementImpl(XMLUtil.fromString(xml));
	}
	
	/**
	 * Creates an XMLElement from a DOM Element.
	 * 
	 * @param element the DOM element
	 * @return the resulting element
	 */
	public static XMLElement fromElement(final Element element) {
		return new XMLElementImpl(element);
	}

	@Override
	public String toString() {
		return XMLUtil.toString(element);
	}

}
