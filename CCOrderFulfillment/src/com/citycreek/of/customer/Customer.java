package com.citycreek.of.customer;

import java.util.Properties;

import com.cjc.util.LangUtil;
import com.cjc.util.PropertiesUtil;

public class Customer {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.getCustomerId().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Customer other = (Customer) obj;
		if (!this.getCustomerId().equals(other.getCustomerId())) {
			return false;
		}
		return true;
	}

	public static final String XML_COLUMNS = "CustomerID,FirstName,LastName,CompanyName,BillingAddress1,BillingAddress2,City,State,PostalCode,EmailAddress";

	private final PropertiesUtil customer = new PropertiesUtil(new Properties());

	public String getCustomerId() {
		return this.customer.getRequired("CustomerID");
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
		if (LangUtil.hasValue(this.getCompanyName())) {
			return this.getCompanyName() + " " + this.getCustomerId();
		} else {
			return this.getFirstName() + " " + this.getLastName() + " " + this.getCustomerId();
		}
	}

	public String getFirstName() {
		return this.customer.getOptional("FirstName");
	}

	public String getLastName() {
		return this.customer.getOptional("LastName");
	}

	public String getFirstLastName() {
		return (this.getFirstName() + " " + this.getLastName()).trim();
	}

	public String getCompanyName() {
		return this.customer.getOptional("CompanyName");
	}

	public String getBillingAddress1() {
		return this.customer.getOptional("BillingAddress1");
	}

	public String getBillingAddress2() {
		return this.customer.getOptional("BillingAddress2");
	}

	public String getCity() {
		return this.customer.getOptional("City");
	}

	public String getState() {
		return this.customer.getOptional("State");
	}

	public String getPostalCode() {
		return this.customer.getOptional("PostalCode");
	}

	public String getEmailAddress() {
		return this.customer.getOptional("EmailAddress");
	}

	public void add(String key, String value) {
		this.customer.setProperty(key, value);
	}

	public boolean isValid() {
		try {
			this.getCustomerId();
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}
}
