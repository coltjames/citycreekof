package com.citycreek.of;

import java.nio.file.Path;
import java.util.List;

public class OrderFulfillmentExporter extends Exporter {

	public OrderFulfillmentExporter(Path parentPath) {
		super(',', parentPath);

		// Write Column Header
		this.column("OrderID");
		this.column("PONum");
		this.column("ShipName");
		this.column("ShipCompanyName");
		this.column("ShipAddress1");
		this.column("ShipAddress2");
		this.column("ShipCity");
		this.column("ShipState");
		this.column("ShipPostalCode");
		this.column("ShipCountry");
		this.column("ShipMethod");
		this.column("ProductCode");
		this.column("Quantity");
		this.column("EmailAddress");
		this.rowEnd();
	}

	@Override
	protected String getFilename() {
		String filename = VolusionExtractor.DATETIME + ".csv";
		return filename;
	}

	@Override
	public OrderFulfillmentExporter exportOrders(List<Order> orders) {
		orders.forEach(this::exportOrder);
		return this;
	}

	private void exportOrder(Order order) {
		order.getDetails().forEach(detail -> this.exportLine(order, detail));
	}

	private void exportLine(Order order, OrderDetail detail) {
		if (detail.isExcludedFromShipping()) {
			return;
		}

		// OrderID
		this.column(order.getOrderID());

		// PONum
		this.column(order.getPONum());

		// ShipName
		this.column(order.getShipFirstLastName());

		// ShipCompanyName
		this.column(order.getShipCompanyName());

		// ShipAddress1
		this.column(order.getShipAddress1());

		// ShipAddress2
		this.column(order.getShipAddress2());

		// ShipCity
		this.column(order.getShipCity());

		// ShipState
		this.column(order.getShipState());

		// ShipPostalCode
		this.column(order.getShipPostalCode());

		// ShipCountry
		this.column(order.getShipCountry());

		// ShipMethod
		this.column(order.getShipMethod());

		// ProductCode
		this.column(detail.getProductCode());

		// Quantity
		this.column(detail.getQuantity());

		// EmailAddress
		this.column(order.getCustomer().getEmailAddress());

		this.rowEnd();

		this.addOrderId(order.getOrderID());
	}
}
