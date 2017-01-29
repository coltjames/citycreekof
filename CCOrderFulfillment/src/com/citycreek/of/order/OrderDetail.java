package com.citycreek.of.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import com.citycreek.of.AppProperties;
import com.cjc.util.LangUtil;
import com.cjc.util.PropertiesUtil;

public class OrderDetail {

	public static final String XML_COLUMNS = //
			"od.ProductCode,od.ProductName,od.ProductPrice,od.Quantity,od.TotalPrice";

	public static final List<String> EXCLUDED_PRODUCTS = new ArrayList<>();

	private final PropertiesUtil detail = new PropertiesUtil(new Properties());

	private String productCode = "";
	private String quantity = "";

	public OrderDetail() {
	}

	public OrderDetail(String productCode, double paymentAmount) {
		this.add("ProductCode", productCode);
		if (paymentAmount > 0.0) {
			this.add("TotalPrice", Double.toString(paymentAmount));
		}
	}

	public static void loadShippingExcludedProducts(PropertiesUtil props) {
		// Load the exclude column and values
		final String[] products = props.getRequired(AppProperties.SHIPPING_EXCLUDED_PRODUCTS).split(",");
		for (String exclude : products) {
			if (LangUtil.hasValue(exclude)) {
				EXCLUDED_PRODUCTS.add(exclude.trim().toUpperCase());
			}
		}
	}

	public String getProductCode() {
		return this.productCode;
	}

	public String getProductName() {
		return this.detail.getOptional("ProductName", "");
	}

	public double getProductPrice() {
		return this.detail.getOptionalDouble("ProductPrice");
	}

	public String getQuantity() {
		return this.quantity;
	}

	public double getTotalPrice() {
		return this.detail.getOptionalDouble("TotalPrice");
	}

	public void add(String key, String value) {
		if (Objects.equals(key, "ProductCode")) {
			this.productCode = value;
		} else if (Objects.equals(key, "Quantity")) {
			this.quantity = value;
		}
		this.detail.setProperty(key, value);
	}

	public boolean isExcludedFromShipping() {
		boolean excluded = EXCLUDED_PRODUCTS.contains(this.getProductCode().trim().toUpperCase());
		excluded |= (this.detail.getOptionalInt("Quantity", 0) < 1);
		return excluded;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.productCode == null) ? 0 : this.productCode.hashCode());
		result = (prime * result) + ((this.quantity == null) ? 0 : this.quantity.hashCode());
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
		OrderDetail other = (OrderDetail) obj;
		if (this.productCode == null) {
			if (other.productCode != null) {
				return false;
			}
		} else if (!this.productCode.equals(other.productCode)) {
			return false;
		}
		if (this.quantity == null) {
			if (other.quantity != null) {
				return false;
			}
		} else if (!this.quantity.equals(other.quantity)) {
			return false;
		}
		return true;
	}
}
