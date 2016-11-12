package com.citycreek.of;

public class PurchaseOrderDetail extends OrderDetail {

	public PurchaseOrderDetail() {
		this.add("ProductCode", "PONUM");
	}

	@Override
	public boolean isPurchaseOrder() {
		return true;
	}

}
