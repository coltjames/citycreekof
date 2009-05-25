/**
 * <a href="http://www.code42.com">(c)Code 42 Software, Inc.</a>
 * $Id: TransformerException.java,v 1.2 2007/04/26 22:15:00 ccovingt Exp $
 */
package com.cjc.util.xml;

/**
 * @author <a href="mailto:brian@code42.com">Brian Bispala </a>
 */
public class TransformerException extends Exception {

	private static final long serialVersionUID = 3806966070150499234L;

	/**
	 * @param message
	 */
	public TransformerException(final String message) {
		super(message);

	}

}
