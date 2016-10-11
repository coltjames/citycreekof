package com.cjc.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.LogManager;

public class AppUtil {

	/**
	 * Read the log configuration from the given properties filename.
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public static void readConfig(final String filename) throws IOException {
		final Properties logProps = new Properties();
		PropertiesUtil.load(filename, logProps);

		// Convert props to InputStream so we can pass it to the logger
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		logProps.store(out, null);
		final BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));

		LogManager.getLogManager().readConfiguration(in);
	}

	/**
	 * Format is name=value per argument.
	 * 
	 * @return convert the command-line arguments into Properties
	 */
	public static Properties getCommandLineProperties(final String[] args) {
		final Properties props = new Properties();
		for (String arg : args) {
			if (LangUtil.hasValue(arg) && (arg.indexOf('=') > 0)) {
				// Strip quotes off front and back, this shouldn't really ever happen (just to be safe).
				if (arg.startsWith("\"") && arg.endsWith("\"")) {
					arg = arg.substring(1, arg.length() - 1);
				}
				final String[] split = arg.split("[=]");
				if (split.length > 1) {
					final String key = split[0];
					final String value = split[1];
					if (LangUtil.hasValue(key) && LangUtil.hasValue(value)) {
						props.put(key, value);
					}
				}
			}
		}
		return props;
	}
}
