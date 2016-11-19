package com.citycreek.of.order;

public class ShipOrderDetail extends OrderDetail {

	public ShipOrderDetail(String method, String amount) {
		super("SHIPPING", amount);
		this.add("ProductName", method);
		this.add("ProductPrice", amount);
		this.add("Quantity", "1");
	}

}
