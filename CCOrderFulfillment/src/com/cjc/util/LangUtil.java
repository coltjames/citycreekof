package com.cjc.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class LangUtil {

	/**
	 * @return true if the string contains some text (not null and not empty)
	 */
	public static boolean hasValue(final String str) {
		return length(str) > 0;
	}

	/**
	 * @return the length of the given string or 0 if null
	 */
	public static int length(final String str) {
		return (str != null) ? str.trim().length() : 0;
	}

	public static String getDateTimeString() {
		return getDateTimeString(System.currentTimeMillis());
	}

	public static String getDateTimeString(long time) {
		return getDateTimeString(new Date(time), "yyyyMMddHHmm");
	}

	public static String getDateTimeString(Date date, String format) {
		final SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern(format);
		final String timeString = sdf.format(date).toString();
		return timeString;
	}

	/**
	 * Converts a flat string into properties. The string must seperate key/value pairs with a '&' and key/value with a
	 * '='. key=value&key=value&key=value
	 */
	public static Properties fromString(final String flat) {
		final Properties props = new Properties();
		if (!LangUtil.hasValue(flat)) {
			return props;
		}
		final String[] pairs = flat.split("[&]");
		for (final String pair : pairs) {
			if (!LangUtil.hasValue(pair)) {
				continue;
			}
			final String[] splitPair = pair.split("[=]");
			if (splitPair.length < 2) {
				continue;
			}
			final String key = splitPair[0];
			if (!LangUtil.hasValue(key)) {
				continue;
			}
			String value = splitPair[1];
			for (int j = 2; j < splitPair.length; j++) {
				value += ("=" + splitPair[j]);
			}
			props.setProperty(key.trim(), value.trim());
		}
		return props;
	}

	/**
	 * Provides a null-safe way to compare two Comparable objects.
	 * 
	 * @param c1 The first comparable... can be null
	 * @param c2 The second comparable (of the same class as the first)... can be null
	 * @param nullsHigh - true when nulls should be considered greater than everything else, otherwise null sorts lowest
	 * @return
	 */
	public static int compare(Comparable c1, Comparable c2, boolean nullsHigh) {
		if (c1 == null) {
			return (c2 == null) ? 0 : (nullsHigh ? +1 : -1);
		} else if (c2 == null) {
			return nullsHigh ? -1 : +1;
		}
		return c1.compareTo(c2);
	}

	public static boolean equals(Comparable c1, Comparable c2) {
		return compare(c1, c2, true) == 0;
	}
}
