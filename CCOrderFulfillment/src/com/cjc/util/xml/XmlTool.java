/**
 * <a href="http://www.code42.com">(c)Code 42 Software, Inc.</a>
 * $Id: XmlTool.java,v 1.15 2008/03/24 22:33:35 bbispala Exp $
 */
package com.cjc.util.xml;

import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XmlTool is a utility class for working with xml files.
 * 
 * @author <a href="mailto:brian@code42.com">Brian Bispala </a>
 */
public class XmlTool {

	private static final Logger log = Logger.getLogger(XmlTool.class.getName());

	public static final String EMPTY = "";// the empty string is returned if a
											// requested attribute does not
											// exist
	public static final String NIL = "nil";// null value
	public static final char newLine = '\n';

	public static void useInternalDocumentBuilderFactory() {
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
				"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
	}

	public static void log() {
		log.config("XmlTool.factory=" + DocumentBuilderFactory.newInstance());
	}

	/**
	 * Helper to get a new empty Document.
	 * 
	 * @return a new document
	 * @throws Exception
	 */
	public static Document newDocument() throws Exception {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.newDocument();
	}

	/**
	 * Helper to return a Document parsed from the input stream.
	 * 
	 * @param in
	 * @return the parsed document
	 * @throws Exception
	 */
	public static Document parseDocument(final InputStream in) throws Exception {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(in);
	}

	/*
	 * ELEMENTS
	 */

	/**
	 * Get the element list
	 * 
	 * @param e
	 * @param tagName
	 * @return the element list
	 */
	public static ElementList getElements(final Element e, final String tagName) {
		final NodeList nodes = e.getElementsByTagName(tagName);
		return new ElementList(nodes);
	}

	/**
	 * Get an element from the parent element. If no elements then NULL is
	 * returned. If more than one than the first is returned.
	 * 
	 * @param e
	 * @param tagName
	 * @return the element or NULL if none
	 */
	public static Element getElement(final Element e, final String tagName) {
		final ElementList list = getElements(e, tagName);
		return list.get(0);
	}

	/**
	 * Helper to create and add a child Element to the given parent
	 * 
	 * @param doc
	 * @param parent
	 * @param tagName
	 * @return the new child Element
	 */
	public static Element add(final Document doc, final Node parent, final String tagName) {
		final Element child = create(doc, tagName);
		append(parent, child);
		return child;
	}

	/**
	 * Helper to append the child to the parent
	 * 
	 * @param parent
	 * @param child
	 */
	public static void append(final Node parent, final Node child) {
		parent.appendChild(child);
	}

	/**
	 * Helper to create a new Element
	 * 
	 * @param doc
	 * @param tagName
	 * @return the new element
	 */
	public static Element create(final Document doc, final String tagName) {
		return doc.createElement(tagName);
	}

	/**
	 * Helper to create and add a Comment node to the given parent
	 * 
	 * @param doc
	 * @param parent
	 * @param comment
	 */
	public static void addComment(final Document doc, final Node parent, final String comment) {
		addComment(doc, parent, comment, false);
	}

	/**
	 * Helper to create and add a Comment node to the given parent
	 * 
	 * @param doc
	 * @param parent
	 * @param comment
	 * @param newlines
	 *            include newlines before and after the comment
	 */
	public static void addComment(final Document doc, final Node parent, String comment, final boolean newlines) {
		if (newlines) {
			// add the preceding and following new lines so that the comment
			// tags are on their own lines.
			comment = newLine + comment + newLine;
		}
		final Node child = doc.createComment(comment);
		parent.appendChild(child);
	}

	/**
	 * Helper to walk the DOM backwards looking for a comment. Continue if the
	 * previous sibling is a text node (i.e. whitespace). If a comment node is
	 * found return the comment value. Any other node results in a NULL value.
	 * 
	 * @param node
	 * @return the comment or NULL if not found
	 */
	public static String getComment(final Node node) {
		final Node prev = node.getPreviousSibling();
		if (prev != null) {
			switch (prev.getNodeType()) {
			case Node.TEXT_NODE:

				// recurse backwards
				return getComment(prev);

			case Node.COMMENT_NODE:

				// found it
				return ((Comment) prev).getTextContent();

			default:
				// otherwise return nothing
				return null;
			}
		}
		return null;
	}

	/*
	 * ATTRIBUTES
	 */
	/**
	 * Set an attribute
	 * 
	 * @param e
	 * @param attrName
	 * @param value
	 */
	public static void set(final Element e, final String attrName, final String value) {
		if (value != null) {
			e.setAttribute(attrName, value);
		}
	}

	/**
	 * Set a boolean attribute
	 * 
	 * @param e
	 * @param attrName
	 * @param b
	 */
	public static void setBoolean(final Element e, final String attrName, final Boolean b) {
		if (b != null) {
			e.setAttribute(attrName, String.valueOf(b));
		}
	}

	/**
	 * Set an integer attribute
	 * 
	 * @param e
	 * @param attrName
	 * @param i
	 */
	public static void setInt(final Element e, final String attrName, final Integer i) {
		if (i != null) {
			e.setAttribute(attrName, String.valueOf(i));
		}
	}

	/**
	 * Set a long attribute
	 * 
	 * @param e
	 * @param attrName
	 * @param l
	 */
	public static void setLong(final Element e, final String attrName, final Long l) {
		if (l != null) {
			e.setAttribute(attrName, String.valueOf(l));
		}
	}

	/**
	 * Set the element as null
	 * 
	 * @param e
	 */
	public static void setNull(final Element e) {
		// set the null (nil) indicator
		setBoolean(e, NIL, true);
	}

	/**
	 * Get the attribute value of the given name
	 * 
	 * @param e
	 * @param name
	 * @return the value of the attribute or NULL if not found
	 */
	public static String get(final Element e, final String name) {
		final String val = e.getAttribute(name);
		return !EMPTY.equals(val) ? val : null;// null if not found
	}

	/**
	 * 
	 * @param e
	 * @param name
	 * @return the boolean value of the attribute of the given name; a
	 *         non-existent attribute results in 'false'.
	 */
	public static boolean getBoolean(final Element e, final String name) {
		return Boolean.valueOf(get(e, name)).booleanValue();// NULL is false
	}

	/**
	 * Is the Element marked as having a NULL value?
	 * 
	 * @param e
	 * @return is the element null?
	 */
	public static boolean isNull(final Element e) {
		return getBoolean(e, NIL);
	}

	/**
	 * Get the int value from the given attribute.
	 * 
	 * @param e
	 * @param name
	 * @return the int or 0 if not found
	 */
	public static int getInt(final Element e, final String name) {
		final Integer i = getIntOptional(e, name);
		return i != null ? i : 0;
	}

	/**
	 * Get the optional Integer value from the given attribute.
	 * 
	 * @param e
	 * @param name
	 * @return the Integer or NULL if not found
	 */
	public static Integer getIntOptional(final Element e, final String name) {
		Integer i = null;
		final String val = get(e, name);
		if (val != null) {
			try {
				i = new Integer(val);
			} catch (final Exception ex) {
				log.log(Level.WARNING,
						"Exception getting a Integer from xml! name=" + name + ", element=" + e + ", " + ex, ex);
			}
		}
		return i;
	}

	/**
	 * Get the long value from the given attribute.
	 * 
	 * @param e
	 * @param name
	 * @return the long or 0 if not found
	 */
	public static long getLong(final Element e, final String name) {
		final Long l = getLongOptional(e, name);
		return l != null ? l : 0;
	}

	/**
	 * Get the optional Long value from the given attribute.
	 * 
	 * @param e
	 * @param name
	 * @return the Long or NULL if not found
	 */
	public static Long getLongOptional(final Element e, final String name) {
		Long l = null;
		final String val = get(e, name);
		if (val != null) {
			try {
				l = new Long(val);
			} catch (final Exception ex) {
				log.log(Level.WARNING, "Exception getting a Long from xml! name=" + name + ", element=" + e + ", " + ex,
						ex);
			}
		}
		return l;
	}

	/**
	 * ElementList
	 * 
	 * @author <a href="mailto:brian@code42.com">Brian Bispala </a>
	 */
	public static class ElementList implements Iterable<Element> {

		private final NodeList nodes;

		/**
		 * @param nodes
		 */
		public ElementList(final NodeList nodes) {
			super();
			this.nodes = nodes;
		}

		public int size() {
			return this.nodes.getLength();
		}

		public Element get(final int i) {
			return (Element) this.nodes.item(i);
		}

		/**
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<Element> iterator() {
			return new ElementIterator(this.nodes);
		}
	}

	/**
	 * ElementIterator
	 * 
	 * @author <a href="mailto:brian@code42.com">Brian Bispala </a>
	 */
	public static class ElementIterator implements Iterator<Element> {

		private final NodeList nodes;
		private int pos = 0;

		/**
		 * @param nodes
		 */
		public ElementIterator(final NodeList nodes) {
			super();
			this.nodes = nodes;
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			// yes if pos is < length
			return (this.pos < this.nodes.getLength());
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Element next() {
			final Node node = this.nodes.item(this.pos++);// get the item at the
															// pos and increment
															// the pos
			assert (node.getNodeType() == Node.ELEMENT_NODE) : "Unexpected node type=" + node.getNodeType();
			return (Element) node;
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException("ElementIterator does not support remove()!");
		}
	}
}
