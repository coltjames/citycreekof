/**
 * <a href="http://www.code42.com">(c)Code 42 Software, Inc.</a>
 * $Id: IOUtil.java,v 1.1 2008/07/02 21:16:05 bbispala Exp $
 */
package com.cjc.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:brian@code42.com">Brian Bispala </a>
 */
public class IOUtil {

	private static final Logger log = Logger.getLogger(IOUtil.class.getName());

	public static void close(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				final RuntimeException d = new RuntimeException("Unable to close, " + c, e);
				log.log(Level.WARNING, d.getMessage(), d);
			}
		}
	}
}
