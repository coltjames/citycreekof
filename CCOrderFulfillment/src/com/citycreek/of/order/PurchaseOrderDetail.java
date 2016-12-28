package com.citycreek.of.order;

public class PurchaseOrderDetail extends OrderDetail {

	public PurchaseOrderDetail(String poNumber) {
		super("PONUM", 0.0);
		this.add("ProductName", poNumber);
	}

}
