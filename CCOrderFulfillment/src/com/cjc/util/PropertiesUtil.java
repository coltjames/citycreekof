/*
 * AppProperties.java
 * Created on Mar 10, 2006 by Colt Covington
 * <a href="http://www.code42.com">(c)2006 Code 42 Software, Inc.</a>
 * $Id: PropertiesUtil.java,v 1.14 2008/08/08 23:14:33 ccovingt Exp $
 */
package com.cjc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application properties.
 * 
 * @author <a href="mailto:ccovingt@code42.com">ccovingt</a>
 */
public class PropertiesUtil {

	private static final Logger log = Logger.getLogger(PropertiesUtil.class.getName());

	private final Properties props;

	public PropertiesUtil() {
		this.props = System.getProperties();
	}

	public PropertiesUtil(Properties props) {
		this.props = props;
	}

	public Properties getProperties() {
		return this.props;
	}

	public static void load(String filename, Properties props) {
		load(filename, props, true);// default to required
	}

	public static void load(File file, Properties props) {
		load(file, props, true);// default to required
	}

	/**
	 * Reads the properties from the given configuration file into the given properties object.
	 */
	public static void load(String filename, Properties props, boolean required) {
		if (!LangUtil.hasValue(filename)) {
			return;
		}

		final File file = new File(filename);
		load(file, props, required);
	}

	/**
	 * Load the given Properties from the given file.
	 * 
	 * @param file
	 * @param props
	 * @param required
	 */
	public static void load(File file, Properties props, boolean required) {
		InputStream in = null;
		try {
			// ok to load if it is an existing file that can be read
			final boolean okToLoad = file.isFile() && file.canRead();
			if (okToLoad) {
				in = new FileInputStream(file);
				props.load(in);
			} else if (required) {
				// required file so throw FNF
				throw new FileNotFoundException();
			}
		} catch (final IOException e) {
			throw new RuntimeException("Unable to load Properties, " + file, e);
		} finally {
			IOUtil.close(in);
		}
	}

	/**
	 * Save the properties to the given file (overwrite)
	 * 
	 * @param file
	 * @param props
	 * @param comments identifying string to be included as the top line of the properties file; can be null
	 */
	public static void save(String filename, Properties props, String comments) {
		if (!LangUtil.hasValue(filename)) {
			return;
		}

		final File file = new File(filename);
		save(file, props, comments);
	}

	/**
	 * Save the properties to the given file (overwrite)
	 * 
	 * @param file
	 * @param props
	 * @param comments identifying string to be included as the top line of the properties file; can be null
	 */
	public static void save(File file, Properties props, String comments) {
		if (file == null) {
			return;
		}

		boolean okToSave = !file.exists() || file.canWrite();
		OutputStream out = null;
		try {
			if (okToSave) {
				if (!file.exists()) {
					file.mkdirs();
					file.createNewFile();
				}
				out = new FileOutputStream(file);
				props.store(out, comments);
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "Unable to save properties, " + file, e);
		} finally {
			IOUtil.close(out);
		}
	}

	/**
	 * Set the properites from another instance
	 * 
	 * @param another
	 */
	public static void setFromAnother(Properties props, Properties another) {
		if (another == null) {
			return;
		}
		for (final Object element : another.keySet()) {
			final String key = (String) element;
			final String value = another.getProperty(key);
			props.setProperty(key, value);
		}
	}

	public void load(String filename) {
		load(filename, this.props, true);// default to required
	}

	public void load(File file) {
		load(file, this.props, true);// default to required
	}

	/**
	 * Reads the properties from the given configuration file into the given properties object.
	 */
	public void load(String filename, boolean required) {
		if (!LangUtil.hasValue(filename)) {
			return;
		}

		final File file = new File(filename);
		load(file, this.props, required);
	}

	/**
	 * Load the given Properties from the given file.
	 * 
	 * @param file
	 * @param props
	 * @param required
	 */
	public void load(File file, boolean required) {
		InputStream in = null;
		try {
			// ok to load if it is an existing file that can be read
			final boolean okToLoad = file.isFile() && file.canRead();
			if (okToLoad) {
				in = new FileInputStream(file);
				this.props.load(in);
			} else if (required) {
				// required file so throw FNF
				throw new FileNotFoundException();
			}
		} catch (final IOException e) {
			throw new RuntimeException("Unable to load properties, " + file, e);
		} finally {
			IOUtil.close(in);
		}
	}

	/**
	 * Set the properites from another instance
	 * 
	 * @param another
	 */
	public void setFromAnother(Properties another) {
		if (another == null) {
			return;
		}
		for (final Object element : another.keySet()) {
			final String key = (String) element;
			final String value = another.getProperty(key);
			this.props.setProperty(key, value);
		}
	}

	/**
	 * Log all the properties at a default CONFIG level.
	 */
	public static void log(Properties props) {
		log(props, Level.CONFIG);
	}

	/**
	 * Log all the properties at the given level.
	 */
	public static void log(Properties props, Level level) {
		log.config("**** PROPERTIES ****");
		final List<String> names = new ArrayList<String>();
		for (final Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
			final String name = (String) e.nextElement();
			names.add(name);
		}
		Collections.sort(names);
		for (final String name : names) {
			final String value = props.getProperty(name);
			log.log(level, name + "=" + value);
		}
	}

	public Object setProperty(String key, String value) {
		return this.props.setProperty(key, value);
	}

	/**
	 * Helper to get a required system property. This method saves you from having to provide a default for a property
	 * that <b>needs</b> to exist.
	 * 
	 * @param propertyName name of the property you want
	 * @throws RuntimeException if the property is not available
	 */
	public String getRequired(String propertyName) {
		final String propertyValue = this.props.getProperty(propertyName);
		if (propertyValue != null) {
			return propertyValue.trim();
		} else {
			throw new RuntimeException("\n\n**********Required Property MISSING!!! - paramName=" + propertyName);
		}
	}

	/**
	 * Helper to get an optional system property.
	 * 
	 * @param propertyName name of the property you want
	 */
	public String getOptional(String propertyName) {
		final String propertyValue = this.props.getProperty(propertyName);
		if (propertyValue != null) {
			return propertyValue.trim();
		} else {
			return null;
		}
	}

	/**
	 * Helper to get an optional system property.
	 * 
	 * @param propertyName name of the property you want
	 * @param defaultValue the default that applies if propertyName doesn't exist
	 */
	public String getOptional(String propertyName, String defaultValue) {
		final String result = this.props.getProperty(propertyName);
		return (result == null) ? defaultValue : result.trim();
	}

	/**
	 * Gets the optional property as an Long if it exists. Otherwise returns null.
	 * 
	 * @param propertyName
	 * @return the Integer or NULL of not found
	 */
	public Long getOptionalLong(String propertyName) {
		final String value = this.getOptional(propertyName);
		try {
			return (LangUtil.hasValue(value) ? Long.valueOf(value.trim()) : null);
		} catch (final NumberFormatException e) {
			log.log(Level.WARNING, "Unable to parse property to a Long, property=" + propertyName);
			return null;
		}
	}

	/**
	 * Get an optional long property that will use the provided default if the property doesn't exist. If the property is
	 * present but fails to parse, a RuntimeException will be thrown.
	 * 
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	public long getOptionalLong(String propertyName, long defaultValue) {
		long value = defaultValue;

		String propertyValue = null;
		try {
			propertyValue = this.props.getProperty(propertyName);
			if (propertyValue != null) {
				value = Long.parseLong(propertyValue.trim());
			}
		} catch (final Exception e) {
			log.log(Level.WARNING, "Unable to parse property to a Long, property=" + propertyName);
		}

		return value;
	}

	/**
	 * Gets the required property value as an int.
	 * 
	 * @param propertyName
	 * @return
	 * 
	 * @see SystemProperties.getRequired(String)
	 */
	public int getRequiredInt(String propertyName) {
		final String value = this.getRequired(propertyName);
		return Integer.parseInt(value.trim());
	}

	/**
	 * Gets the optional property as an Integer if it exists. Otherwise returns null.
	 * 
	 * @param propertyName
	 * @return the Integer or NULL of not found
	 */
	public Integer getOptionalInt(String propertyName) {
		final String value = this.getOptional(propertyName);
		try {
			return (LangUtil.hasValue(value) ? Integer.valueOf(value.trim()) : null);
		} catch (final NumberFormatException e) {
			log.log(Level.WARNING, "Unable to parse property to an Integer, property=" + propertyName);
			return null;
		}
	}

	/**
	 * Gets the optional property as an Integer if it exists. Otherwise returns null.
	 * 
	 * @param propertyName
	 * @return
	 */
	public int getOptionalInt(String propertyName, int defaultValue) {
		final String value = this.getOptional(propertyName);
		try {
			return (LangUtil.hasValue(value) ? Integer.valueOf(value.trim()) : defaultValue);
		} catch (final NumberFormatException e) {
			log.log(Level.WARNING, "Unable to parse property to an Integer, property=" + propertyName);
			return defaultValue;
		}
	}

	/**
	 * Gets the required property value as a long.
	 * 
	 * @param propertyName
	 * @return
	 * 
	 * @see SystemProperties.getRequired(String)
	 */
	public long getRequiredLong(String propertyName) {
		final String value = this.getRequired(propertyName);
		return Long.parseLong(value.trim());
	}

	/**
	 * Gets the required property value as a boolean.
	 * 
	 * @param propertyName
	 * @return boolean
	 * 
	 * @see SystemProperties.getRequired(String)
	 */
	public boolean getRequiredBoolean(String propertyName) {
		final String value = this.getRequired(propertyName);
		return new Boolean(value).booleanValue();
	}

	/**
	 * Retrieve an optional Boolean value. Null if not found.
	 * 
	 * @param propertyName
	 * @return
	 */
	public Boolean getOptionalBoolean(String propertyName) {
		Boolean booleanValue = null;
		final String value = this.getOptional(propertyName);
		if (value != null) {
			booleanValue = new Boolean(value);
		}

		return booleanValue;
	}

	/**
	 * Evaluate the optional boolean parameter. If parameter is not present the default will be returned.
	 * 
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	public boolean getOptionalBoolean(String propertyName, boolean defaultValue) {
		final Boolean param = this.getOptionalBoolean(propertyName);
		return ((param != null) ? param.booleanValue() : defaultValue);
	}

	public boolean containsKey(String key) {
		return this.props.containsKey(key);
	}

	/**
	 * A handy method for dumping all SystemProperties. The possibilities for debugging should be obvious.
	 * 
	 */
	public void dumpProperties() {
		final Set<Object> keys = new TreeSet<Object>(this.props.keySet());
		for (Object o : keys) {
			System.out.println(o.toString() + "=" + this.props.get(o).toString());
		}
	}

	/**
	 * A handy method for dumping all SystemProperties. The possibilities for debugging should be obvious.
	 * 
	 */
	public void dumpProperties(Logger lLogger, Level lvl) {
		if (lLogger.isLoggable(lvl)) {
			final Set<Object> keys = new TreeSet<Object>(this.props.keySet());
			for (Object o : keys) {
				lLogger.log(lvl, o.toString() + "=" + this.props.get(o));
			}
		}
	}
}
