package com.citycreek.of.order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.citycreek.of.customer.Customer;
import com.cjc.util.LangUtil;
import com.cjc.util.PropertiesUtil;

public class Order {

	private static final Logger log = Logger.getLogger(Order.class.getName());

	public static final String XML_COLUMNS = //
			"o.OrderID,o.CustomerID,o.PONum,o.OrderNotes,o.OrderDate,o.PaymentAmount,o.PaymentMethodID," //
					+ "o.ShipFirstName,o.ShipLastName,o.ShipCompanyName,o.ShippingMethodID,TotalShippingCost," //
					+ "o.ShipAddress1,o.ShipAddress2,o.ShipCity,o.ShipState,o.ShipPostalCode,o.ShipCountry," //
					+ "o.BillingFirstName,o.BillingLastName,o.BillingCompanyName,o.BillingPhoneNumber," //
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

	private final PropertiesUtil order = new PropertiesUtil(new Properties());
	private final List<OrderDetail> details = new ArrayList<OrderDetail>();
	private Customer customer;

	// ORDER

	public long getOrderID() {
		Long oid = this.order.getRequiredLong("OrderID");
		return oid;
	}

	public String getPONum() {
		return this.order.getOptional("PONum", "");
	}

	public String getOrderName() {
		if (LangUtil.hasValue(this.getBillingCompanyName())) {
			return this.getBillingCompanyName() + " " + this.getOrderID();
		} else {
			return this.getBillingFirstName() + " " + this.getBillingLastName() + " " + this.getOrderID();
		}
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
		String method = this.order.getRequired("PaymentMethodID");
		if (Objects.equals("5", method)) {
			return "Visa";
		} else if (Objects.equals("6", method)) {
			return "MasterCard";
		} else if (Objects.equals("7", method)) {
			return "American Express";
		} else if (Objects.equals("8", method)) {
			return "Discover";
		} else {
			return "";
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

	/**
	 * The NAME in IIF should be the Bill to: Company. If no company then, FirstName LastName in the Bill to address. Do
	 * not append with account number in volusion. This way, I won’t get duplicates because sometimes the customer makes a
	 * new account in volusion but it is the same person. I want quickbooks to combine these.
	 * <p>
	 * We should go back to appending the customer number on the end. I think this will ensure that the correct info is
	 * included in Quickbooks even if there are two accounts for the same person.
	 */
	public String getCustomerName() {
		if (LangUtil.hasValue(this.getBillingCompanyName())) {
			return this.getBillingCompanyName();
		} else {
			return this.getBillingFirstName() + " " + this.getBillingLastName();
		}
	}

	// BILLING

	public String getBillingFirstName() {
		return this.order.getOptional("BillingFirstName", "");
	}

	public String getBillingLastName() {
		return this.order.getOptional("BillingLastName", "");
	}

	private String getBillingAddress1() {
		return this.order.getOptional("BillingAddress1", "");
	}

	private String getBillingAddress2() {
		return this.order.getOptional("BillingAddress2", "");
	}

	private String getBillingCity() {
		return this.order.getOptional("BillingCity", "");
	}

	private String getBillingState() {
		return this.order.getOptional("BillingState", "");
	}

	private String getBillingPostalCode() {
		return this.order.getOptional("BillingPostalCode", "");
	}

	private String getBillingCountry() {
		String country = this.order.getOptional("BillingCountry", "");
		if (Objects.equals("United States", country)) {
			return "";
		}
		return country;
	}

	public String getBillingCompanyName() {
		return this.order.getOptional("BillingCompanyName", "");
	}

	public String getBillingPhoneNumber() {
		return this.order.getOptional("BillingPhoneNumber", "");
	}

	public String getBillingFirstLastName() {
		return (this.getBillingFirstName() + " " + this.getBillingLastName()).trim();
	}

	private String getBillingAddressCityStateZip() {
		return this.getBillingCity() + ", " + //
				this.getBillingState() + " " + //
				this.getBillingPostalCode();
	}

	public String getBillingAddressLineX(int x) {
		List<String> lines = Arrays.asList( //
				this.getBillingFirstLastName(), //
				this.getBillingCompanyName(), //
				this.getBillingAddress1(), //
				this.getBillingAddress2(), //
				this.getBillingAddressCityStateZip(), //
				this.getBillingCountry()).stream() //
				.filter(LangUtil::hasValue) // Remove empty lines
				.collect(Collectors.toList());
		if ((x == 4) && (lines.size() > 5)) {
			log.warning("IFF WARNING: More address lines than 5; order=" + this.getOrderID());
			System.out.println("IFF WARNING: More address lines than 5; order=" + this.getOrderID());
		}
		if (x < lines.size()) {
			return lines.get(x);
		}
		return "";
	}

	// SHIPPING

	private String getShipFirstName() {
		return this.order.getOptional("ShipFirstName", "");
	}

	private String getShipLastName() {
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
		String country = this.order.getOptional("ShipCountry", "");
		if (Objects.equals("United States", country)) {
			return "";
		}
		return country;

	}

	public String getShipFirstLastName() {
		return (this.getShipFirstName() + " " + this.getShipLastName()).trim();
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

	private String getShipAddressCityStateZip() {
		return this.getShipCity() + ", " + //
				this.getShipState() + " " + //
				this.getShipPostalCode();
	}

	public String getShipAddressLineX(int x) {
		List<String> lines = Arrays.asList( //
				this.getShipFirstLastName(), //
				this.getShipCompanyName(), //
				this.getShipAddress1(), //
				this.getShipAddress2(), //
				this.getShipAddressCityStateZip(), //
				this.getShipCountry()).stream() //
				.filter(LangUtil::hasValue) // Remove empty lines
				.collect(Collectors.toList());
		if (x < lines.size()) {
			return lines.get(x);
		}
		return "";
	}

	public String getTotalShippingCost() {
		return this.order.getOptional("TotalShippingCost");
	}

	/**
	 * Taxable if any detail lines are taxable.
	 */
	public boolean isTaxable() {
		return this.details.stream().anyMatch(OrderDetail::isTaxable);
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

	public void addShipDetail() {
		ShipOrderDetail detail = new ShipOrderDetail(this.getShipMethod(), this.getTotalShippingCost());
		this.details.add(detail);
	}

	public boolean isPurchaseOrder() {
		return LangUtil.hasValue(this.getPONum());
	}

	/**
	 * Duplicate if:
	 * <ul>
	 * <li>Sequential order ids</li>
	 * <li>Same customer</li>
	 * <li>Same number detail lines</li>
	 * <li>Each detail line matches ProductCode and Quantity</li>
	 * </ul>
	 * TODO What if the customer orders one more?
	 */
	public boolean isDuplicate(Order order2) {
		// Sequential order ids
		final long prevOrderId = this.getOrderID();
		final long currOrderId = order2.getOrderID();
		if (prevOrderId != (currOrderId - 1)) {
			return false;
		}

		if (!Objects.equals(this.getCustomerID(), order2.getCustomerID())) {
			return false;
		}

		// Same number detail lines and each
		// Each detail line matches productcode
		return Objects.equals(this.getDetails(), order2.getDetails());
	}
}
