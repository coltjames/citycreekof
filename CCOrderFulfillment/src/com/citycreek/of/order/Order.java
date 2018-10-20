package com.citycreek.of.order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.citycreek.of.AppProperties;
import com.citycreek.of.customer.Customer;
import com.cjc.util.LangUtil;
import com.cjc.util.PropertiesUtil;

public class Order {

	private static final Logger log = Logger.getLogger(Order.class.getName());

	public static final String XML_COLUMNS = //
			"o.OrderID,o.CustomerID,o.PONum,o.OrderNotes,o.OrderDate,o.OrderStatus,o.PaymentAmount,o.PaymentMethodID,o.SalesTax1," //
					+ "o.ShipFirstName,o.ShipLastName,o.ShipCompanyName,o.ShippingMethodID,o.TotalShippingCost," //
					+ "o.ShipAddress1,o.ShipAddress2,o.ShipCity,o.ShipState,o.ShipPostalCode,o.ShipCountry," //
					+ "o.BillingFirstName,o.BillingLastName,o.BillingCompanyName,o.BillingPhoneNumber," //
					+ "o.BillingAddress1,o.BillingAddress2,o.BillingCity,o.BillingState,o.BillingPostalCode,o.BillingCountry";

	private static Properties SHIPPING_METHODS = new Properties();

	private final PropertiesUtil order = new PropertiesUtil(new Properties());
	private final List<OrderDetail> details = new ArrayList<OrderDetail>();
	private Customer customer;

	public static void loadShippingMethodMap(PropertiesUtil props) {
		// Load the exclude column and values
		final String mapAsString = props.getRequired(AppProperties.SHIPPING_METHOD_MAP);
		SHIPPING_METHODS = LangUtil.fromString(mapAsString);
	}

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

	/**
	 * Generate a report with orders of a particular status type (Not Cancelled, Open, Shipped, Returned, or Cancelled).
	 */
	public boolean isCancelled() {
		String status = this.order.getOptional("OrderStatus");
		boolean cancelled = Objects.equals("cancelled", status.toLowerCase());
		return cancelled;
	}

	public String getOrderNotes() {
		return this.order.getOptional("OrderNotes", "");
	}

	public String getOrderDate() {
		return this.order.getOptional("OrderDate", "");
	}

	public double getPaymentAmount() {
		return this.order.getOptionalDouble("PaymentAmount");
	}

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
		} else if (Objects.equals("12", method)) {
			return "PayPal Express Upgrade";
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
		if (this.customer != null) {
			return this.customer.getCustomerName();
		}
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
			method = SHIPPING_METHODS.getProperty(methodId);
			if (!LangUtil.hasValue(method)) {
				method = methodId;
				System.out.println("WARNING: Unknown shipping method: " + methodId);
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

	public double getTotalShippingCost() {
		return this.order.getOptionalDouble("TotalShippingCost");
	}

	/**
	 * Taxable if any detail lines are taxable.
	 */
	public boolean isTaxable() {
		double salesTax = this.order.getOptionalDouble("SalesTax1");
		return salesTax > 0;
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
		double totalShippingCost = this.getTotalShippingCost();
		if (totalShippingCost > 0.0) {
			ShipOrderDetail detail = new ShipOrderDetail(this.getShipMethod(), totalShippingCost);
			this.details.add(detail);
		}
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
