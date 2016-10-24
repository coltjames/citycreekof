package com.citycreek.of;

import java.util.Arrays;
import java.util.List;

import com.cjc.util.LangUtil;
import com.cjc.util.PropertiesUtil;

public class OrderDetail {

	public static final String XML_COLUMNS = //
			"od.ProductCode,od.ProductName,od.ProductPrice,od.Quantity,od.TotalPrice,od.TaxableProduct";

	public static final List<String> EXCLUDED_PRODUCTS = Arrays.asList();

	private final PropertiesUtil detail = new PropertiesUtil();

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
		return this.detail.getOptional("ProductCode", "");
	}

	public String getProductName() {
		return this.detail.getOptional("ProductName", "");
	}

	public String getProductPrice() {
		return this.detail.getOptional("ProductPrice", "");
	}

	public int getQuantity() {
		return this.detail.getOptionalInt("Quantity", 0);
	}

	public String getTotalPrice() {
		return this.detail.getOptional("TotalPrice", "");
	}

	public String getTaxableProduct() {
		return this.detail.getOptional("TaxableProduct", "");
	}

	public void add(String key, String value) {
		this.detail.setProperty(key, value);
	}

	public boolean isExcludedFromShipping() {
		boolean excluded = EXCLUDED_PRODUCTS.contains(this.getProductCode());
		excluded |= (this.getQuantity() < 1);
		return excluded;
	}
}