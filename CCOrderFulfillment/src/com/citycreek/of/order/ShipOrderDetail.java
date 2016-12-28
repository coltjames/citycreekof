package com.citycreek.of.order;

import com.cjc.util.LangUtil;

public class ShipOrderDetail extends OrderDetail {

	public ShipOrderDetail(String method, double amount) {
		super("Shipping", amount);
		if (LangUtil.hasValue(method)) {
			this.add("ProductName", method);
		}
		if (amount > 0.0) {
			this.add("ProductPrice", Double.toString(amount));
		}
		this.add("Quantity", "1");
	}

}
