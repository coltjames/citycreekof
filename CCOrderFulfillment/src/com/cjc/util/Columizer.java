/*
 * <a href="http://www.code42.com">(c)2002 Code 42 Software, Inc.</a>
 * $id: $
 */
package com.cjc.util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A utility that replaces duplicate data in columns with spaces
 * 
 * @author <a href="mailto://matthew@code42.com">Matthew Dornquast</a>, <a href="http://www.code42.com">Code 42 Software
 *         Inc.</a>
 */
public class Columizer {

	private final static int TABSTOP = 60;
	private static char[] BLANKS = new char[TABSTOP];

	static { // build up an array of spaces for speedy concatination
		for (int i = 0; i < TABSTOP; i++) {
			BLANKS[i] = ' ';
		}
	}

	/**
	 * Remove duplicate tolkens from start.
	 * 
	 * @param line one
	 * @param line two
	 * @return a new stringbuffer that has duplicate colum removed
	 */
	static public StringBuffer stripDups(final StringBuffer one, final StringBuffer two, final char[] delims) {
		final StringBuffer r = new StringBuffer(two.length());
		final ArrayList<String> t1 = tokenize(one, delims);
		final ArrayList<String> t2 = tokenize(two, delims);
		final Iterator<String> iter1 = t1.iterator();
		final Iterator<String> iter2 = t2.iterator();
		int len = 0; // how many spaces to pad
		while (iter2.hasNext() && iter1.hasNext()) {
			final String s1 = (String) iter1.next();
			final String s2 = (String) iter2.next();
			if (s2.equals(s1)) {// they are the same
				// add padding to
				len += s2.length() + 1;
			} else { // different
				// ok, then concat the rest of the data to end and stop
				break;
			}
		}
		// now add rest of data
		if (len < two.length()) {
			final String rest = two.substring(len);
			// System.out.println("Rest="+rest);
			r.append(rest);
		}
		return r;

	}

	/**
	 * @param line one
	 * @param line two
	 * @return a new stringbuffer that has duplicate colum data replaced by spaces
	 */
	static public StringBuffer columize(final StringBuffer one, final StringBuffer two, final char[] delims) {
		final StringBuffer r = new StringBuffer(two.length());
		final ArrayList<String> t1 = tokenize(one, delims);
		final ArrayList<String> t2 = tokenize(two, delims);
		final Iterator<String> iter1 = t1.iterator();
		final Iterator<String> iter2 = t2.iterator();
		int len = 0; // how many spaces to pad
		while (iter2.hasNext() && iter1.hasNext()) {
			final String s1 = (String) iter1.next();
			final String s2 = (String) iter2.next();
			if (s2.equals(s1)) {// they are the same
				// add padding to
				len += s2.length() + 1;
			} else { // different
				// ok, then concat the rest of the data to end and stop
				break;
			}
		}
		// add a bunch of spaces for duplicates
		r.append(BLANKS, 0, len);
		// now add rest of data
		if (len < two.length()) {
			final String rest = two.substring(len);
			// System.out.println("Rest="+rest);
			r.append(rest);
		} else { // too many blanks, take one off
			r.deleteCharAt(len - 1);
		}
		return r;
	}

	/**
	 * @param string buffer to tokenize
	 * @param delims to utilize for breaking tokens up
	 * @return an ArrayList of strings (tokens), delims are not included
	 */
	static public ArrayList<String> tokenize(final StringBuffer sb, final char[] delims) {
		final ArrayList<String> tokens = new ArrayList<>();
		int fs = 0;
		int fe = 0; // end of field
		final int oneLen = sb.length();
		while (fs < oneLen) {
			boolean foundField = false;
			while (!foundField && (fe < oneLen)) {
				final char c = sb.charAt(fe++);
				for (final char element : delims) {
					if (c == element) { // field marker?
						foundField = true;
						break;
					}
				}
			}
			final int len = fe - fs;
			if (len > 1) {// more data than just field delim

				final String token = sb.substring(fs, foundField ? fe - 1 : fe);
				tokens.add(token);
			}

			// catch up our start pos to where we are now.
			fs = fe;
			foundField = false;
		} // end while fe<oneLen
		return tokens;
	}

	public static void main(final String[] args) {
		// StringBuffer one=new StringBuffer(
		// "15 10:08:20.022@INFO:life.test.TestDB$1.testRoot()");
		// StringBuffer two=new StringBuffer(
		// "15 10:08:20.032@INFO:life.database.LifeNode.getAddress()");

		final StringBuffer one = new StringBuffer("life.licenseclient.LicenseClientService.startTimer:");
		final StringBuffer two = new StringBuffer("life.licenseclient.LicenseClientService.init:");
		final StringBuffer r = Columizer.columize(one, two, new char[] { ' ', '.', ':', '@' });
		System.out.println("line 1=[" + one + "]");
		System.out.println("line 2=[" + two + "]");
		System.out.println("Result=[" + r + "]");
		// StringTokenizer st=new StringTokenizer(one.toString(),":. ");
		// while (st.hasMoreTokens()) {
		// System.out.println("Token=["+st.nextToken()+"]");
		// }
	}

	// 15 10:08:20.022@INFO:life.test.TestDB$1.testRoot(): root node=Node GUID=0, PID=-1, Name=root, Addr=root, Fields={}
	// 15 10:08:20.022@INFO:life.test.TestDB$1.testNode(): Testing basic Node creation/retrieval
	// 15 10:08:20.032@INFO:life.database.LifeNode.getAddress(): address=testnode
}
