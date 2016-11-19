package com.citycreek.of.order;

public class PurchaseOrderDetail extends OrderDetail {

	public PurchaseOrderDetail(String poNumber) {
		super("PONUM", null);
		this.add("ProductName", poNumber);
	}

}
