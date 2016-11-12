package com.citycreek.of;

import java.util.Properties;

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

	public static final String XML_COLUMNS = "CustomerID,EmailAddress";

	private final PropertiesUtil customer = new PropertiesUtil(new Properties());

	public String getCustomerId() {
		return this.customer.getRequired("CustomerID");
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
