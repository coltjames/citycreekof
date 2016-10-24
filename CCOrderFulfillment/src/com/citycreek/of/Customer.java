package com.citycreek.of;

import com.cjc.util.PropertiesUtil;

public class Customer {

	public static final String XML_COLUMNS = //
			"CustomerID,AccessKey,EmailAddress,FirstName,LastName,PhoneNumber,FaxNumber,CustomerType,CompanyName";

	private static PropertiesUtil customer = new PropertiesUtil();

	public String getCustomerId() {
		return customer.getRequired("CustomerID");
	}

	public String getAccessKey() {
		return customer.getOptional("AccessKey", "");
	}

	public String getEmailAddress() {
		return customer.getRequired("EmailAddress");
	}

	public String getFirstName() {
		return customer.getOptional("FirstName", "");
	}

	public String getLastName() {
		return customer.getOptional("LastName", "");
	}

	public String getPhoneNumber() {
		return customer.getOptional("PhoneNumber", "");
	}

	public String getFaxNumber() {
		return customer.getOptional("FaxNumber", "");
	}

	public String getCustomerType() {
		return customer.getOptional("CustomerType", "");
	}

	public String getCompanyName() {
		return customer.getOptional("CompanyName", "");
	}

	public void add(String key, String value) {
		customer.setProperty(key, value);
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
