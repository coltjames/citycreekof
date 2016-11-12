package com.citycreek.of;

public class PaymentOrderDetail extends OrderDetail {

	public PaymentOrderDetail(String paymentType) {
		this.add("ProductCode", paymentType);
	}

	@Override
	public boolean isPayment() {
		return true;
	}
}
