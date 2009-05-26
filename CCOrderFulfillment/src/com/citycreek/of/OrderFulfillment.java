package com.citycreek.of;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cjc.util.AppUtil;
import com.cjc.util.Format42;
import com.cjc.util.IOUtil;
import com.cjc.util.LangUtil;
import com.cjc.util.PropertiesUtil;
import com.cjc.util.xml.XmlTool;

public class OrderFulfillment {

	private static final Logger log = Logger.getLogger(OrderFulfillment.class.getName());

	private static final String VERSION = new Date(1220927361826L).toString();

	public static final String ORDER_DETAILS = "OrderDetails";

	private static PropertiesUtil props = new PropertiesUtil();
	private static List<PropertiesUtil> orders = new ArrayList<PropertiesUtil>();
	private static PropertiesUtil shipMethodMap = new PropertiesUtil(new Properties());
	private static String csvExcludeFilterColumn = null;
	private static Set<String> csvExcludeFilterValues = new HashSet<String>();

	public static void main(String[] args) {
		try {
			setup(args);
			readOrderXml();
			readCustomerXml();
			removeDuplicates();
			final String csvFile = writeCsv();
			ftpCsv(csvFile);
		} catch (Throwable e) {
			info("ERROR:  " + e.getMessage());
			log.log(Level.SEVERE, e.toString(), e);
		}
		info("DONE!");
		info("*************************************************************************");
		System.exit(0);
	}

	private static void info(String msg) {
		log.info(msg);
		System.out.println(msg);
	}

	/**
	 * Setup the application; properties, logging, etc.
	 */
	private static void setup(String[] args) {
		// Command-line arguments
		final Properties argProps = AppUtil.getCommandLineProperties(args);
		props.setFromAnother(argProps);

		// Load application properties from file
		final String confFilename = props.getOptional(AppProperties.CONFIG_FILENAME, "config.properties");
		props.load(confFilename);

		// Logging
		final String loggingFilename = props.getOptional(AppProperties.LOGGING_CONFIG_FILENAME, "logging.properties");
		if (LangUtil.hasValue(loggingFilename)) {
			try {
				AppUtil.readConfig(loggingFilename);
			} catch (IOException e) {
				log.log(Level.WARNING, "Unable to read logging properties, using VM defaults.", e);
			}
		} else {
			log.config("Unable to read logging properties, using VM defaults.");
		}
		Format42.use(props.getProperties());

		// Load the shipping method id map
		final String shipMethodMapValue = props.getRequired(AppProperties.CSV_SHIP_METHOD_MAP);
		final Properties shipMapProps = LangUtil.fromString(shipMethodMapValue);
		shipMethodMap.setFromAnother(shipMapProps);

		// Load the exclude column and values
		csvExcludeFilterColumn = props.getOptional(AppProperties.EXCLUDE_FILTER_COLUMN);
		if (LangUtil.hasValue(csvExcludeFilterColumn)) {
			final String values = props.getRequired(AppProperties.EXCLUDE_FILTER_VALUES);
			final String split[] = values.split(",");
			for (String exclude : split) {
				if (LangUtil.hasValue(exclude)) {
					csvExcludeFilterValues.add(exclude.trim().toUpperCase());
				}
			}
		}

		info("*************************************************************************");
		info("** CityCreek Order Fulfillment");
		info("** " + new Date(1220927361826L).toString());
		info("*************************************************************************");
		log.config("VERSION=" + VERSION);
		log.config("currentTimeMillis=" + System.currentTimeMillis());
		props.dumpProperties(log, Level.CONFIG);
		log.config("shipMethodMap=" + shipMethodMap);
		log.config("csvExcludeFilterColumn=" + csvExcludeFilterColumn);
		log.config("csvExcludeFilterValues=" + csvExcludeFilterValues);
		log.config("*************************************************************************");
	}

	public static void readOrderXml() throws Exception {
		{
			info("Retrieving ORDERS XML file...");
			String xmlFile = props.getOptional(AppProperties.XML_ORDER_FILE);
			if (LangUtil.hasValue(xmlFile)) {
				xmlFile = props.getOptional(AppProperties.XML_DIR, "xml") + "/" + xmlFile;
				info("  Read from file, " + xmlFile);
				final File file = new File(xmlFile);
				if (file.length() > 0) {
					XmlTool.fromXml(file, orders, new OrderXmlTransformer());
				}
			} else {
				readOrdersFromUrl();
			}
		}
	}

	public static void readCustomerXml() throws Exception {

		{
			info("Retrieving CUSTOMERS XML file...");
			String xmlFile = props.getOptional(AppProperties.XML_CUSTOMER_FILE);
			if (LangUtil.hasValue(xmlFile)) {
				xmlFile = props.getOptional(AppProperties.XML_DIR, "xml") + "/" + xmlFile;
				info("  Read from file, " + xmlFile);
				final File file = new File(xmlFile);
				if (file.length() > 0) {
					XmlTool.fromXml(file, orders, new CustomerXmlTransformer());
				}
			} else {
				readCustomersFromUrl();
			}
		}
	}

	private static void readOrdersFromUrl() throws Exception {
		final String username = props.getRequired(AppProperties.VOLUSION_USERNAME);
		final String password = props.getRequired(AppProperties.VOLUSION_PASSWORD);

		// Determine the XML file URL from properties.
		String xmlUrl;
		{
			final String columns = props.getRequired(AppProperties.XML_ORDER_URL_COLUMNS);
			final String urlProperty = props.getRequired(AppProperties.XML_ORDER_URL);
			xmlUrl = MessageFormat.format(urlProperty, username, password, columns);
			info("  Read ORDERS from URL, " + urlProperty);
		}
		// Output filename
		final String format = props.getOptional(AppProperties.FILENAME_TIME_FORMAT, "yyyyMMddHHmm");
		final String filename = LangUtil.getDateTimeString(new Date(), format);
		final String xmlFile = props.getOptional(AppProperties.XML_DIR, "xml") + "/" + filename + "_order.xml";
		info("  Write to file, " + xmlFile);

		// Read in the orders from Volution.
		InputStream in = null;
		FileOutputStream out = null;
		try {
			final URL url = new URL(xmlUrl);
			out = new FileOutputStream(xmlFile);
			in = url.openStream();
			int read;
			while ((read = in.read()) > 0) {
				out.write(read);
			}
		} finally {
			IOUtil.close(in);
			IOUtil.close(out);
		}
		log.fine("  Parsing XML file...");
		final File file = new File(xmlFile);
		if (file.length() > 0) {
			XmlTool.fromXml(file, orders, new OrderXmlTransformer());
		}
	}

	private static void readCustomersFromUrl() throws Exception {
		final String username = props.getRequired(AppProperties.VOLUSION_USERNAME);
		final String password = props.getRequired(AppProperties.VOLUSION_PASSWORD);

		// Determine the XML file URL from properties.
		String xmlUrl;
		{
			final String columns = props.getRequired(AppProperties.XML_CUSTOMER_URL_COLUMNS);
			final String urlProperty = props.getRequired(AppProperties.XML_CUSTOMER_URL);
			xmlUrl = MessageFormat.format(urlProperty, username, password, columns);
			info("  Read CUSTOMERS from URL, " + xmlUrl);
		}
		// Output filename
		final String format = props.getOptional(AppProperties.FILENAME_TIME_FORMAT, "yyyyMMddHHmm");
		final String filename = LangUtil.getDateTimeString(new Date(), format);
		final String xmlFile = props.getOptional(AppProperties.XML_DIR, "xml") + "/" + filename + "_customer.xml";
		info("  Write to file, " + xmlFile);

		// Read in the orders from Volution.
		InputStream in = null;
		FileOutputStream out = null;
		try {
			final URL url = new URL(xmlUrl);
			out = new FileOutputStream(xmlFile);
			in = url.openStream();
			int read;
			while ((read = in.read()) > 0) {
				out.write(read);
			}
		} finally {
			IOUtil.close(in);
			IOUtil.close(out);
		}
		log.fine("  Parsing XML file...");
		final File file = new File(xmlFile);
		if (file.length() > 0) {
			XmlTool.fromXml(file, orders, new CustomerXmlTransformer());
		}
	}

	private static void removeDuplicates() {
		final String columnsProperty = props.getRequired(AppProperties.XML_DUPLICATE_COLUMNS);
		final String[] columns = columnsProperty.split(",");

		// For each order, look to see if the current order is the same as the previous.
		final List<String> removals = new ArrayList<String>();
		PropertiesUtil prevOrder = null;
		for (PropertiesUtil order : orders) {
			if (prevOrder == null) {
				prevOrder = order;
				continue; // skip first one
			}

			// For each column, check to see if they all match
			boolean eq = true;
			String prevId = null;
			String currId = null;
			for (String col : columns) {
				final String colName = col.trim();
				if (!LangUtil.hasValue(colName)) {
					continue; // skip these empty columns
				}
				final String prev = prevOrder.getOptional(colName);
				final String curr = order.getOptional(colName);
				// Duplicates have sequential order id's
				if (colName.equals("OrderID")) {
					prevId = prev;
					currId = curr;
					try {
						final long lPrev = Long.parseLong(prev);
						final long lCurr = Long.parseLong(curr);
						if (lPrev == lCurr) {
							eq = false;
							break; // same order just different order id
						}
						if (lPrev != (lCurr - 1)) {
							eq = false;
							break; // not sequential
						}
					} catch (NumberFormatException e) {
						log.log(Level.WARNING, "Unable to parse OrderId=" + prev + "," + curr + e.getMessage(), e);
					}
					continue; // sequential so possibly, lets check more fields
				}
				if (!LangUtil.equals(prev, curr)) {
					eq = false;
					break; // not equal so not duplicate
				}
			} // for each column

			// Must be equal
			if (eq && (prevId != null)) {
				removals.add(prevId);
				info("DUPLICATE: " + prevId + " equals " + currId);
			}

			prevOrder = order;
		} // for each order

		// Remove orders
		for (Iterator<PropertiesUtil> iter = orders.iterator(); iter.hasNext();) {
			final PropertiesUtil order = iter.next();
			final String orderId = order.getOptional("OrderID");
			if (removals.contains(orderId)) {
				iter.remove();
			}
		}
	}

	private static String writeCsv() throws IOException {
		info("Creating CSV file...");
		boolean first = true;
		final StringBuilder csv = new StringBuilder();
		final String columnsProperty = props.getRequired(AppProperties.CSV_COLUMNS);
		final String[] columns = columnsProperty.split(",");

		// Write Column Header
		for (String col : columns) {
			String colName = col.trim();
			if (!LangUtil.hasValue(colName)) {
				continue;
			}
			if (!first) {
				csv.append(",");
			} else {
				first = false;
			}
			colName = colName.replaceAll("\\[", "").replaceAll("\\]", "");
			csv.append("\"").append(colName).append("\"");
		}
		csv.append("\n");

		// Write Values
		int count = 0;
		first = true;
		final Set<String> orderIds = new LinkedHashSet<String>();
		for (PropertiesUtil order : orders) {
			if (excludeOrder(order)) {
				continue;
			}
			for (String col : columns) {
				final String colName = col.trim();
				if (!LangUtil.hasValue(colName)) {
					continue;
				}
				if (!first) {
					csv.append(",");
				}
				csv.append("\"");
				if (colName.equals(CsvSpecialColumns.SHIP_NAME)) {
					final String name = parseShipName(order);
					csv.append(name);
				} else if (colName.equals(CsvSpecialColumns.SHIP_METHOD)) {
					parseShipMethod(csv, order);
				} else {
					final String value = order.getOptional(colName);
					if (LangUtil.hasValue(value)) {
						if (colName.equals("OrderID")) {
							orderIds.add(value);
						}
						csv.append(value);
					}
				}
				csv.append("\"");
				first = false;
			}
			first = true;
			count++;
			csv.append("\n");
		}
		csv.append("\n");

		// Output filename
		final String format = props.getOptional(AppProperties.FILENAME_TIME_FORMAT, "yyyyMMddHHmm");
		final String filename = LangUtil.getDateTimeString(new Date(), format) + ".csv";
		final String csvFile = props.getOptional(AppProperties.CSV_DIR, "csv") + "/" + filename;
		// write to disk
		FileChannel fc = null;
		try {
			fc = new FileOutputStream(csvFile).getChannel();
			fc.write(ByteBuffer.wrap(csv.toString().getBytes()));
		} finally {
			IOUtil.close(fc);
		}
		info("  Excluded: " + csvExcludeFilterValues);
		info("  Orders:   " + orderIds);
		info("  Wrote " + count + " lines to " + csvFile);
		return filename;
	}

	private static boolean excludeOrder(PropertiesUtil order) {
		// Quantity more than 0!
		final int qty = order.getOptionalInt("Quantity", 0);
		if (qty == 0) {
			return true; // exclude the order, not enough quantity
		}
		if (!LangUtil.hasValue(csvExcludeFilterColumn)) {
			return false;
		}
		final String value = order.getOptional(csvExcludeFilterColumn);
		if (LangUtil.hasValue(value)) {
			final boolean exists = csvExcludeFilterValues.contains(value.trim().toUpperCase());
			if (exists) {
				return true;
			}
		}
		return false;
	}

	private static void parseShipMethod(StringBuilder csv, PropertiesUtil order) {
		final String methodColName = props.getRequired(AppProperties.CSV_SHIP_METHOD);
		final String methodId = order.getOptional(methodColName);
		if (LangUtil.hasValue(methodId)) {
			String method = shipMethodMap.getOptional(methodId);
			if (!LangUtil.hasValue(method)) {
				method = methodId;
				log.log(Level.WARNING, "Missing shipping method id: " + methodId);
			}
			csv.append(method);
		}
	}

	private static String parseShipName(PropertiesUtil order) {
		final String firstColName = props.getRequired(AppProperties.CSV_SHIPNAME_FIRST);
		final String lastColName = props.getRequired(AppProperties.CSV_SHIPNAME_LAST);

		// First and last name
		final String first = order.getRequired(firstColName);
		final String last = order.getRequired(lastColName);
		return first + " " + last;
	}

	private static void ftpCsv(String csvFile) throws IOException {
		final boolean enabled = props.getRequiredBoolean(AppProperties.FTP_ENABLED);
		if (!enabled) {
			info("FTP Disabled");
			return;
		}
		info("Sending file to FTP site...");
		final String urlString = props.getRequired(AppProperties.FTP_URL) + csvFile + ";type=i";
		log.fine("  Remote=" + urlString);
		final URL url = new URL(urlString);
		final URLConnection urlConnection = url.openConnection();
		OutputStream out = null;
		FileInputStream in = null;
		try {
			out = urlConnection.getOutputStream();
			final String localCsvFile = props.getOptional(AppProperties.CSV_DIR, "csv") + "/" + csvFile;
			log.fine("  Local=" + localCsvFile);
			in = new FileInputStream(localCsvFile);
			final byte[] buf = new byte[16384];
			int read;
			while ((read = in.read(buf)) > 0) {
				out.write(buf, 0, read);
			}
		} finally {
			IOUtil.close(out);
			IOUtil.close(in);
		}
	}
}
