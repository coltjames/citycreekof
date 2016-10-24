package com.citycreek.of;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import com.cjc.util.LangUtil;
import com.cjc.util.PropertiesUtil;

public class Order {

	private static final Logger log = Logger.getLogger(Order.class.getName());

	public static final String XML_COLUMNS = //
			"o.OrderID,o.CustomerID,o.PONum,o.OrderNotes,o.OrderDate,o.PaymentAmount,o.PaymentMethodID," //
					+ "o.ShipFirstName,o.ShipLastName,o.ShipCompanyName,o.ShippingMethodID," //
					+ "o.ShipAddress1,o.ShipAddress2,o.ShipCity,o.ShipState,o.ShipPostalCode,o.ShipCountry," //
					+ "o.BillingFirstName,o.BillingLastName," //
					+ "o.BillingAddress1,o.BillingAddress2,o.BillingCity,o.BillingState,o.BillingPostalCode,o.BillingCountry";

	private static final Map<String, String> SHIPPING_METHODS = LangUtil.mapOf(//
			"105", "UPS 2nd Day Air A.M.", "106", "UPS 2nd Day Air", //
			"107", "UPS 3 Day Select", //
			"108", "UPS Ground", //
			"104", "UPS Next Day Air Saver", "102", "UPS Next Day Air", "101", "UPS Next Day Air Early A.M.", //
			"109", "UPS Standard", //
			"113", "UPS World Wide Saver", "112", "UPS Worldwide Expedited", //
			"111", "UPS Worldwide Express Plus", "110", "UPS Worldwide Express", //
			"204", "USPS Parcel", "205", "USPS Priority Box", //
			"211", "PMI", "214", "EMI", //
			"501", "US Mail", "900", "US Mail", "901", "US Mail", //
			"502", "UPS", "902", "UPS", "903", "UPS", "904", "UPS", "905", "UPS", "906", "UPS", "907", "UPS", //
			"908", "UPS", "909", "UPS", "910", "UPS", "911", "UPS", "912", "UPS", "913", "UPS", "914", "UPS", //
			"915", "UPS", "9012", "UPS", "916", "UPS", //
			"917", "US Mail Intl", "918", "US Mail Intl", "919", "US Mail Intl", "920", "US Mail Intl", //
			"921", "US Mail Intl", "922", "US Mail Intl", "923", "US Mail Intl", "924", "US Mail Intl", //
			"925", "US Mail Intl", "926", "US Mail Intl", "927", "US Mail Intl", //
			"9001", "Priority Mail", "9011", "Priority Mail");

	private PropertiesUtil order = new PropertiesUtil();
	private List<OrderDetail> details = new ArrayList<OrderDetail>();
	private Customer customer;

	// ORDER

	public long getOrderID() {
		Long oid = this.order.getRequiredLong("OrderID");
		return oid;
	}

	public String getPONum() {
		return this.order.getOptional("PONum", "");
	}

	public String getOrderNotes() {
		return this.order.getOptional("OrderNotes", "");
	}

	public String getOrderDate() {
		return this.order.getOptional("OrderDate", "");
	}

	public String getPaymentAmount() {
		return this.order.getOptional("PaymentAmount", "");
	}

	/**
	 * 5 Visa <br>
	 * 6 MasterCard <br>
	 * 7 American Express <br>
	 * 8 Discover <br>
	 * 13 Purchase order number <br>
	 */
	public String getPaymentMethod() {
		if (Objects.equals("13", this.order.getRequired("PaymentMethodID"))) {
			return "PURCHORD";
		} else {
			return "Creditcard";
		}
	}

	// CUSTOMER

	public String getCustomerID() {
		return this.order.getRequired("CustomerID");
	}

	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getFirstName() {
		String firstName = this.customer.getFirstName();
		if (!LangUtil.hasValue(firstName)) {
			firstName = this.getShipFirstName();
		}
		return firstName;
	}

	public String getLastName() {
		String lastName = this.customer.getLastName();
		if (!LangUtil.hasValue(lastName)) {
			lastName = this.getShipLastName();
		}
		return lastName;
	}

	public String getCompanyName() {
		String company = this.getCustomer().getCompanyName();
		if (!LangUtil.hasValue(company)) {
			company = this.getShipCompanyName();
		}
		return company;
	}

	// BILLING

	public String getBillingName() {
		return this.order.getRequired("BillingFirstName") + " " + this.order.getRequired("BillingLastName");
	}

	public String getBillingAddressCityStateZip() {
		return this.order.getRequired("BillingCity") + ", " + //
				this.order.getRequired("BillingState") + " " + //
				this.order.getRequired("BillingPostalCode");
	}

	public String getBillingFirstName() {
		return this.order.getOptional("BillingFirstName", "");
	}

	public String getBillingLastName() {
		return this.order.getOptional("BillingLastName", "");
	}

	public String getBillingAddress1() {
		return this.order.getOptional("BillingAddress1", "");
	}

	public String getBillingAddress2() {
		return this.order.getOptional("BillingAddress2", "");
	}

	public String getBillingCity() {
		return this.order.getOptional("BillingCity", "");
	}

	public String getBillingState() {
		return this.order.getOptional("BillingState", "");
	}

	public String getBillingPostalCode() {
		return this.order.getOptional("BillingPostalCode", "");
	}

	public String getBillingCountry() {
		return this.order.getOptional("BillingCountry", "");
	}

	// SHIPPING

	public String getShipFirstName() {
		return this.order.getOptional("ShipFirstName", "");
	}

	public String getShipLastName() {
		return this.order.getOptional("ShipLastName", "");
	}

	public String getShipCompanyName() {
		return this.order.getOptional("ShipCompanyName", "");
	}

	public String getShipAddress1() {
		return this.order.getOptional("ShipAddress1", "");
	}

	public String getShipAddress2() {
		return this.order.getOptional("ShipAddress2", "");
	}

	public String getShipCity() {
		return this.order.getOptional("ShipCity", "");
	}

	public String getShipState() {
		return this.order.getOptional("ShipState", "");
	}

	public String getShipPostalCode() {
		return this.order.getOptional("ShipPostalCode", "");
	}

	public String getShipCountry() {
		return this.order.getOptional("ShipCountry", "");
	}

	public String getShipName() {
		// First and last name
		final String first = this.order.getRequired("ShipFirstName");
		final String last = this.order.getRequired("ShipLastName");
		return first + " " + last;
	}

	public String getShipMethod() {
		String method = null;
		final String methodId = this.order.getOptional("ShippingMethodID");
		if (LangUtil.hasValue(methodId)) {
			method = SHIPPING_METHODS.get(methodId);
			if (!LangUtil.hasValue(method)) {
				method = methodId;
				log.warning("Missing shipping method id: " + methodId);
			}
		}
		return method;
	}

	public String getShipAddressCityStateZip() {
		return this.order.getRequired("ShipCity") + ", " + //
				this.order.getRequired("ShipState") + " " + //
				this.order.getRequired("ShipPostalCode");
	}

	// DETAIL

	public List<OrderDetail> getDetails() {
		return this.details;
	}

	// OTHER HELPERS

	public boolean isValid() {
		try {
			this.getOrderID();
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	public void add(String key, String value) {
		this.order.setProperty(key, value);
	}

	public void add(OrderDetail detail) {
		this.details.add(detail);
	}

	public String getColumnByName(String colName) {
		return this.order.getOptional(colName);
	}
}
