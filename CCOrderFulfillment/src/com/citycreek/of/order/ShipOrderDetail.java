package com.citycreek.of.order;

import com.cjc.util.LangUtil;

public class ShipOrderDetail extends OrderDetail {

	public ShipOrderDetail(String method, String amount) {
		super("Shipping", amount);
		if (LangUtil.hasValue(method)) {
			this.add("ProductName", method);
		}
		if (LangUtil.hasValue(amount)) {
			this.add("ProductPrice", amount);
		}
		this.add("Quantity", "1");
	}

}
