package com.citycreek.of;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.citycreek.of.customer.Customer;
import com.citycreek.of.customer.CustomerXmlTransformer;
import com.citycreek.of.exporter.OrderFulfillmentExporter;
import com.citycreek.of.exporter.QuickBooksIIFExporter;
import com.citycreek.of.exporter.QuickBooksTableImportExporter;
import com.citycreek.of.order.Order;
import com.citycreek.of.order.OrderDetail;
import com.citycreek.of.order.OrderXmlTransformer;
import com.cjc.util.AppUtil;
import com.cjc.util.Format42;
import com.cjc.util.LangUtil;
import com.cjc.util.PropertiesUtil;

public class VolusionExtractor {

	private static final String XML_DIR = "xml";

	private static final String QUICKBOOKS_DIR = "quickbooks";

	private static final Logger log = Logger.getLogger(VolusionExtractor.class.getName());

	private static final String VERSION = "v2.0.2 - 29 January 2017";
	public static final String DATETIME = LangUtil.getDateTimeString(new Date(), "yyyyMMddHHmm");

	private static final String ORDER_FULFILLMENT_DIR = "order_fulfillment";

	private static String password;
	private static PropertiesUtil props = new PropertiesUtil();
	private static List<Order> orders = new ArrayList<Order>();
	private static Map<String, Customer> customers = new HashMap<>();

	public static void main(String[] args) {
		try {
			setup(args);
			readCustomerXml();
			readOrderXml();
			if (!orders.isEmpty()) {
				orders.forEach(VolusionExtractor::associateCustomerToOrder);
				orders.forEach(Order::addShipDetail);
				final List<String> removeDuplicates = removeDuplicatesOrCancelledOrders();

				// writeQuickBooksIFF(true);
				writeQuickBooksIFF(false);
				writeQuickBooksCSV();
				writeOrderFulfillment();

				// Print dups at the end.
				if (!removeDuplicates.isEmpty()) {
					info("*************************************************************************");
					info("Ignored Orders");
					for (String dup : removeDuplicates) {
						info("  " + dup);
					}
				} else {
					info("No orders ignored");
				}
			} else {
				info("No orders found - no files written.");
			}
		} catch (Throwable e) {
			info("Unknown serious error", e);
		}
		info("*************************************************************************");
		info(orders.size() + " order(s) processed - " + //
				orders.stream().map(Order::getOrderID).sorted().collect(Collectors.toList()));
		info("DONE!");
		info("*************************************************************************");
	}

	private static void info(String msg) {
		info(msg, null);
	}

	private static void info(String msg, Throwable e) {
		if (e != null) {
			log.log(Level.WARNING, msg, e);
			System.out.println("ERROR: " + msg + "; " + e);
		} else {
			log.info(msg);
			System.out.println(msg);
		}
	}

	private static void warn(String msg) {
		warn(msg, null);
	}

	private static void warn(String msg, Throwable e) {
		if (e != null) {
			log.log(Level.WARNING, msg, e);
			System.out.println("ERROR: " + msg + "; " + e);
		} else {
			log.warning(msg);
			System.out.println("WARNING: " + msg);
		}
	}

	/**
	 * Setup the application; properties, logging, etc.
	 *
	 * @throws IOException
	 */
	private static void setup(String[] args) throws IOException {
		// Command-line arguments
		final Properties argProps = AppUtil.getCommandLineProperties(args);
		props.setFromAnother(argProps);

		// Load application properties from file
		props.load("config.properties");

		// Logging
		if (LangUtil.hasValue("logging.properties")) {
			try {
				AppUtil.readConfig("logging.properties");
			} catch (IOException e) {
				info("Unable to read logging properties, using VM defaults.");
			}
		} else {
			info("Unable to read logging properties, using VM defaults.");
		}
		Format42.use(props.getProperties());

		// Read password from file
		password = new String(Files.readAllBytes(Paths.get("password.txt")));

		Order.loadShippingMethodMap(props);
		OrderDetail.loadShippingExcludedProducts(props);

		info("*************************************************************************");
		info("** CityCreek Order Fulfillment");
		info("** " + VERSION);
		info("*************************************************************************");
		log.config("currentTimeMillis=" + System.currentTimeMillis());
		props.dumpProperties(log, Level.CONFIG);
		log.config("*************************************************************************");
	}

	public static void associateCustomerToOrder(Order o) {
		String customerId = o.getCustomerID();
		if (LangUtil.hasValue(customerId) && customers.containsKey(customerId)) {
			o.setCustomer(customers.get(customerId));
		} else {
			warn("No customer found for order=" + customerId + ". Order will be skipped. Manually process order.");
		}
	}

	public static void readOrderXml() throws Exception {
		info("Retrieving ORDERS XML file...");
		String xmlFile = props.getOptional(AppProperties.XML_ORDER_FILE);
		if (LangUtil.hasValue(xmlFile)) {
			xmlFile = props.getOptional(AppProperties.XML_DIR, XML_DIR) + "/" + xmlFile;
			info("  Read from file, " + xmlFile);
			final File file = new File(xmlFile);
			if (file.length() > 0) {
				new OrderXmlTransformer().fromXml(file, orders);
			}
		} else {
			readOrdersFromUrl();
		}
	}

	public static void readCustomerXml() throws Exception {
		info("Retrieving CUSTOMERS XML file...");
		String xmlFile = props.getOptional(AppProperties.XML_CUSTOMER_FILE);
		if (LangUtil.hasValue(xmlFile)) {
			xmlFile = props.getOptional(AppProperties.XML_DIR, XML_DIR) + "/" + xmlFile;
			info("  Read from file, " + xmlFile);
			final File file = new File(xmlFile);
			if (file.length() > 0) {
				new CustomerXmlTransformer().fromXml(file, customers);
			}
		} else {
			readCustomersFromUrl();
		}
		if (customers.isEmpty()) {
			throw new RuntimeException("NO CUSTOMERS - check the API password file.");
		}
	}

	private static void readOrdersFromUrl() throws Exception {
		final String username = props.getRequired(AppProperties.VOLUSION_USERNAME);

		// Determine the XML file URL from properties.
		final String xmlUrl;
		{
			final String urlProperty = props.getRequired(AppProperties.XML_ORDER_URL);
			final String columns = Order.XML_COLUMNS + "," + OrderDetail.XML_COLUMNS;
			xmlUrl = MessageFormat.format(urlProperty, username, password, columns);
			info("  Read ORDERS from URL, " + urlProperty);
		}
		// Output filename
		final Path xmlFile = Paths.get(//
				props.getOptional(AppProperties.XML_DIR, XML_DIR) + "/" + DATETIME + "_order.xml");
		info("  Write to file, " + xmlFile);
		Files.createDirectories(xmlFile.getParent());

		// Read in the orders from Volution.
		try (InputStream in = new URL(xmlUrl).openStream()) {
			Files.copy(in, xmlFile, StandardCopyOption.REPLACE_EXISTING);
		}

		log.fine("  Parsing XML file...");
		if (xmlFile.toFile().length() > 0) {
			new OrderXmlTransformer().fromXml(xmlFile.toFile(), orders);
		}
	}

	private static void readCustomersFromUrl() throws Exception {
		final String username = props.getRequired(AppProperties.VOLUSION_USERNAME);

		// Determine the XML file URL from properties.
		String xmlUrl;
		{
			final String urlProperty = props.getRequired(AppProperties.XML_CUSTOMER_URL);
			xmlUrl = MessageFormat.format(urlProperty, username, password, Customer.XML_COLUMNS);
			info("  Read CUSTOMERS from URL, " + urlProperty);
		}
		// Output filename
		final Path xmlFile = Paths.get(//
				props.getOptional(AppProperties.XML_DIR, XML_DIR) + "/customers.xml");
		info("  Write to file, " + xmlFile);
		Files.createDirectories(xmlFile.getParent());

		// Read in the orders from Volution.
		try (InputStream in = new URL(xmlUrl).openStream()) {
			Files.copy(in, xmlFile, StandardCopyOption.REPLACE_EXISTING);
		}
		log.fine("  Parsing XML file...");
		if (xmlFile.toFile().length() > 0) {
			new CustomerXmlTransformer().fromXml(xmlFile.toFile(), customers);
		}
	}

	private static List<String> removeDuplicatesOrCancelledOrders() {
		// For each order, look to see if the current order is the same as the previous.
		final List<Long> removals = new ArrayList<>();
		final List<String> removalLogs = new ArrayList<>();

		Order prevOrder = null;
		for (Order order : orders) {
			if (order.isCancelled()) {
				removals.add(order.getOrderID());
				removalLogs.add(order.getCustomerName() //
						+ " :: " + order.getOrderID() + " - cancelled");
				continue;
			}
			if (order.getCustomer() == null) {
				removals.add(order.getOrderID());
				removalLogs.add(order.getCustomerID() //
						+ " :: " + order.getOrderID() + " - unknown customer");
				continue;
			}
			if (prevOrder == null) {
				prevOrder = order;
				continue; // skip first one
			}

			if (prevOrder.isDuplicate(order)) {
				removals.add(order.getOrderID());
				removalLogs.add(prevOrder.getCustomerName() //
						+ " :: " + prevOrder.getOrderID() + " = " + order.getOrderID() + " - duplicate");
			}

			prevOrder = order;
		} // for each order

		// Remove orders
		orders.removeIf(order -> removals.contains(order.getOrderID()));

		return removalLogs;
	}

	private static Path writeQuickBooksIFF(boolean writeOrders) throws IOException {
		info("Creating QuickBooks IIF file...");
		Path parentPath = Paths.get(props.getOptional(AppProperties.QUICKBOOKS_IIF_DIR, QUICKBOOKS_DIR));

		QuickBooksIIFExporter exporter = new QuickBooksIIFExporter(parentPath, writeOrders).exportOrders(orders);
		exporter.ensureFileExistsWithHeader();
		Path file = exporter.write();
		info("  Wrote " + exporter.getCount() + " lines to " + file);
		return file;
	}

	private static Path writeQuickBooksCSV() throws IOException {
		info("Creating QuickBooks CSV file...");
		Path parentPath = Paths.get(props.getOptional(AppProperties.QUICKBOOKS_CSV_DIR, QUICKBOOKS_DIR));
		QuickBooksTableImportExporter exporter = new QuickBooksTableImportExporter(parentPath).exportOrders(orders);

		exporter.ensureFileExistsWithHeader();

		// write to disk
		Path file = exporter.write();
		info("  Wrote " + exporter.getCount() + " lines to " + file);
		return file;
	}

	private static Path writeOrderFulfillment() throws IOException {
		info("Creating order fulfillment CSV file...");
		Path parentPath = Paths.get(props.getOptional(AppProperties.ORDER_FULFILLMENT_DIR, ORDER_FULFILLMENT_DIR));

		if (!Files.exists(parentPath)) {
			log.fine("FUL:: Parent missing, creating parent - " + parentPath.toAbsolutePath());
			Files.createDirectories(parentPath);
		}

		OrderFulfillmentExporter exporter = new OrderFulfillmentExporter(parentPath).exportOrders(orders);

		// write to disk
		Path file = exporter.write();
		info("  Excluded: " + OrderDetail.EXCLUDED_PRODUCTS);
		info("  Wrote " + exporter.getCount() + " lines to " + file);
		return file;
	}
}
