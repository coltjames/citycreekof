package com.citycreek.of;

public interface AppProperties {

	String LOGGING_CONFIG_FILENAME = "logging.config.filename";
	String CONFIG_FILENAME = "conf";
	String FILENAME_TIME_FORMAT = "global.filenameDateTimeFormat";

	String VOLUSION_USERNAME = "volusion.username";
	String VOLUSION_PASSWORD = "volusion.password";

	String XML_DIR = "xml.dir";

	String XML_ORDER_FILE = "xml.order.file";
	String XML_ORDER_URL = "xml.order.url";
	String XML_ORDER_URL_COLUMNS = "xml.order.url.columns";

	String XML_CUSTOMER_FILE = "xml.customer.file";
	String XML_CUSTOMER_URL = "xml.customer.url";
	String XML_CUSTOMER_URL_COLUMNS = "xml.customer.url.columns";

	String CSV_COLUMNS = "csv.columns";
	String CSV_SHIPNAME_FIRST = "csv.ShipName.first";
	String CSV_SHIPNAME_LAST = "csv.ShipName.last";
	String CSV_SHIP_METHOD = "csv.ShipMethod";
	String CSV_SHIP_METHOD_MAP = "csv.ShipMethodMap";
	String CSV_DIR = "csv.dir";
	String FTP_ENABLED = "ftp.enabled";
	String FTP_URL = "ftp.url";
	String EXCLUDE_FILTER_COLUMN = "csv.excludeFilterColumn";
	String EXCLUDE_FILTER_VALUES = "csv.excludeFilterValues";
	String XML_DUPLICATE_COLUMNS = "xml.duplicate.columns";
}
