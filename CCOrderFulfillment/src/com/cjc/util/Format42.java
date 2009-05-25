/*
 * <a href="http://www.code42.com">(c)2002 Code 42 Software, Inc.</a>
 * $id: $
 */
package com.cjc.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This custom formatter defines the default Code 42 logging format.
 * <p>
 * Based on Sun's SimpleFormatter
 * 
 * @author <a href="mailto:matthew@code42.com">Matthew Dornquast</a>
 */
public class Format42 extends Formatter {

	/**
	 * The following are keys for properties passed in.
	 */
	public final static String PROP_COMPRESS_CATEGORY = "PropCompressCategory";
	public final static String PROP_COLUMIZE = "PropColumize";

	Date dat = new Date();
	// columize indicates if we should use white space instead of duplicates from record to record.
	private static boolean columize;
	// should we compress the category from entire package to simply class.method.
	private static boolean compressCategory = false;
	private final static int COL_THREAD = 20; // 20 for thread id
	private final static int COL_DATETIME = 15; // 15 for date/time
	private final static int COL_LEVEL = 7; // 7 for level (warning)
	private final static int COL_CATEGORY_SHORT = 40; // 40 for class.method
	private final static int COL_CATEGORY_LONG = 50; // 50 for class.method
	private static char[] BLANKS = new char[COL_CATEGORY_LONG];
	private static final char[] DELIMS = new char[] { '[', ']', ' ', '.', ':', '@', '$' };
	private final static String format = "{0,date,MM.dd.yy HH:mm:ss.SSS}";
	private final MessageFormat formatter = new MessageFormat(format);
	/** the last method invoked, used to simply logging output */
	private StringBuffer lasttime = new StringBuffer(COL_DATETIME);
	private StringBuffer lastcategory = new StringBuffer(COL_CATEGORY_SHORT);
	private String lastlevel = "";
	private String lastthread = "";
	private final Object args[] = new Object[1];
	// Line separator string. This is the value of the line.separator
	// property at the moment that the SimpleFormatter was created.
	private final String lineSeparator = java.security.AccessController
			.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));

	static { // build up an array of spaces for speedy concatination
		for (int i = 0; i < COL_CATEGORY_LONG; i++) {
			BLANKS[i] = DELIMS[2];
		}
	}

	/**
	 * Format the given LogRecord.
	 * 
	 * @param record the log record to be formatted.
	 * @return a formatted log record
	 */
	@Override
	public synchronized String format(final LogRecord record) {
		final StringBuffer sb = new StringBuffer(256);
		this.dat.setTime(record.getMillis());
		this.args[0] = this.dat;

		sb.append(DELIMS[0]);
		// part 1: the date/time of message
		{
			final StringBuffer time = new StringBuffer(COL_DATETIME);
			this.formatter.format(this.args, time, null);
			StringBuffer sb1 = time;
			if (columize) {
				sb1 = Columizer.columize(this.lasttime, time, DELIMS);
			}
			sb.append(sb1);
			this.lasttime = time;
		}

		sb.append(DELIMS[2]).append(DELIMS[2]);

		// part 2: the level
		{
			final StringBuffer level = new StringBuffer(COL_LEVEL);
			final String recentLevel = record.getLevel().getLocalizedName();
			if (!columize || !this.lastlevel.equals(recentLevel)) { // new level
				this.lastlevel = recentLevel;
				level.append(recentLevel);
			}
			// append enough spaces
			final int len = level.length();
			if (len < COL_LEVEL) {
				// Append
				level.append(BLANKS, 0, COL_LEVEL - len);
			}
			sb.append(level);
		}

		sb.append(DELIMS[2]);

		// part 3: thread id (just use the last 20 chars)
		{
			String threadId = Thread.currentThread().getName();
			if (!columize || !this.lastthread.equals(threadId)) { // new level
				this.lastthread = threadId;
			} else {
				threadId = "";
			}
			final int tLen = threadId.length();
			if (tLen > COL_THREAD) {
				// Crop
				threadId = threadId.substring(tLen - COL_THREAD);
			} else if (tLen < COL_THREAD) {
				// Append
				threadId = (new StringBuffer(threadId).append(BLANKS, 0, COL_THREAD - tLen)).toString();
			}
			sb.append(threadId);
		}

		sb.append(DELIMS[2]).append(DELIMS[2]);

		// part 4: the class and method name or category
		{
			StringBuffer classAndMethod = new StringBuffer(COL_CATEGORY_SHORT);
			if (record.getSourceClassName() != null) {
				// strip off code 42 package stuff if it's there.
				classAndMethod.append(record.getSourceClassName());
			} else {
				classAndMethod.append(record.getLoggerName());
			}
			if (record.getSourceMethodName() != null) {
				classAndMethod.append(".");
				classAndMethod.append(record.getSourceMethodName());
			}
			if (columize) {
				final StringBuffer classAndMethodColumnized = Columizer.stripDups(this.lastcategory, classAndMethod, DELIMS);
				this.lastcategory = classAndMethod;
				classAndMethod = classAndMethodColumnized;
			}
			// append enough spaces
			String finalClassAndMethod = classAndMethod.toString();
			final int currentTabStop = (compressCategory) ? COL_CATEGORY_SHORT : COL_CATEGORY_LONG;
			final int len = classAndMethod.length();
			if (len > currentTabStop) {
				finalClassAndMethod = classAndMethod.substring(len - currentTabStop);
			} else if (len < currentTabStop) {
				// Append
				finalClassAndMethod = (classAndMethod.append(BLANKS, 0, currentTabStop - len)).toString();
			}
			sb.append(finalClassAndMethod);
		}

		sb.append(DELIMS[1]).append(DELIMS[2]).append(DELIMS[2]);

		// part 5: Message (the last and final part)
		{
			final String message = this.formatMessage(record);
			sb.append(message);
			sb.append(this.lineSeparator);

			// exception happened?
			if (record.getThrown() != null) {
				try {
					final StringWriter sw = new StringWriter();
					final PrintWriter pw = new PrintWriter(sw);
					record.getThrown().printStackTrace(pw);
					pw.close();
					sb.append(sw.toString());
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		return sb.toString();
	}

	/**
	 * <p>
	 * Forces all logging handlers to use this formatter. And set parameters to indicate the behavior of the formatter.
	 * The
	 * <p>
	 * Properties include:<br>
	 * - PropColumize<br>
	 * - PropCompressCategory<br>
	 * 
	 * @param props Properties
	 */
	public static void use(final Properties props) {
		String property = "";
		if (props.containsKey(PROP_COLUMIZE)) {
			property = (String) props.get(PROP_COLUMIZE);
			Format42.columize = (property.equalsIgnoreCase("true"));
		}
		if (props.containsKey(PROP_COMPRESS_CATEGORY)) {
			property = (String) props.get(PROP_COMPRESS_CATEGORY);
			Format42.compressCategory = (property.equalsIgnoreCase("true"));
		}
		final Logger l = Logger.getLogger("");
		final Handler[] handlers = l.getHandlers();
		for (final Handler handler : handlers) {
			handler.setFormatter(new Format42());
		}
	}

	/**
	 * This sets all logging to use Format42. Replaces the 'configureLogger' code that has been cut and pasted everywhere!
	 */
	public static void start(final Level level) {
		// logger
		final Logger log = Logger.getLogger("");
		log.setLevel(level);
		// set logging
		final Formatter formatter = new Format42();
		final Handler[] handlers = log.getHandlers();
		for (final Handler element : handlers) {
			element.setFormatter(formatter);
			element.setLevel(level);
		}
	}

	/**
	 * Unit test
	 */
	public static void main(final String[] args) throws Exception {
		// instantiate our logging stuff
		final Logger log = Logger.getLogger(""); // top logger
		final ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.ALL);
		log.addHandler(ch);
		log.setLevel(Level.ALL);

		final Logger l = Logger.getLogger(Format42.class.getName());

		// default.
		final Properties props = new Properties();

		// columnize, short format
		props.put(Format42.PROP_COLUMIZE, "true");
		props.put(Format42.PROP_COMPRESS_CATEGORY, "true");
		Format42.use(props);
		l.info("columnize, short format, message 1");
		Thread.sleep(100);
		l.finer("columnize, short format, message 2");
		Thread.sleep(100);
		l.warning("columnize, short format, message 3");
		Thread.sleep(100);

		// columnize, long format
		props.put(Format42.PROP_COLUMIZE, "true");
		props.put(Format42.PROP_COMPRESS_CATEGORY, "false");
		Format42.use(props);
		l.info("columnize, long format, message 1");
		Thread.sleep(100);
		l.finer("columnize, long format, message 2");
		Thread.sleep(100);
		l.warning("columnize, long format, message 3");
		Thread.sleep(100);

		// no columnize, short format
		props.put(Format42.PROP_COLUMIZE, "false");
		props.put(Format42.PROP_COMPRESS_CATEGORY, "true");
		Format42.use(props);
		l.info("no columnize, short format, message 1");
		Thread.sleep(100);
		l.finer("no columnize, short format, message 2");
		Thread.sleep(100);
		l.warning("no columnize, short format, message 3");
		Thread.sleep(100);

		// no columnize, long format
		props.put(Format42.PROP_COLUMIZE, "false");
		props.put(Format42.PROP_COMPRESS_CATEGORY, "false");
		Format42.use(props);
		l.info("no columnize, long format, message 1");
		Thread.sleep(100);
		l.finer("no columnize, long format, message 2");
		Thread.sleep(100);
		l.warning("no columnize, long format, message 3");
		Thread.sleep(100);
	}
}
