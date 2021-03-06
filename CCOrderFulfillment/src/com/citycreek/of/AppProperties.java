package com.citycreek.of;

public interface AppProperties {

	// VOLUSION
	String VOLUSION_USERNAME = "volusion.username";

	// XML Import
	String XML_DIR = "xml.dir";
	String XML_CUSTOMER_FILE = "xml.customer.file";
	String XML_ORDER_FILE = "xml.order.file";
	String XML_CUSTOMER_URL = "xml.customer.url";
	String XML_ORDER_URL = "xml.order.url";

	// SHIPPING CSV
	String ORDER_FULFILLMENT_DIR = "order.fulfillment.dir";
	String SHIPPING_EXCLUDED_PRODUCTS = "excluded.products";
	String SHIPPING_METHOD_MAP = "csv.ShipMethodMap";

	// QUICKBOOKS
	String QUICKBOOKS_IIF_DIR = "quickbooks.iif.dir";
	String QUICKBOOKS_CSV_DIR = "quickbooks.csv.dir";
}
