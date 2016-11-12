package com.citycreek.of;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Logger;

public class QuickBooksTableImportExporter extends Exporter {

	private static final Logger log = Logger.getLogger(QuickBooksTableImportExporter.class.getName());

	public QuickBooksTableImportExporter(Path parentPath) {
		super(',', parentPath);
	}

	@Override
	public QuickBooksTableImportExporter exportOrders(List<Order> orders) {
		orders.forEach(order -> { //
			this.prepareOrder(order);
			order.getDetails().forEach(detail -> this.writeLine(order, detail));
			this.addOrderId(order.getOrderID());
		});
		return this;
	}

	@Override
	protected String getFilename() {
		return "citycreek_orders.csv";
	}

	/**
	 * Create QuickBooks CSV file if it doesn't exist and append header.
	 */
	public void ensureFileExistsWithHeader() throws IOException {
		Path filePath = this.getFilePath().toAbsolutePath();
		if (Files.exists(filePath)) {
			log.fine("CSV:: File already exists, skip creating and writting header.");
			return; // Skip appending header, we assume it already has a header.
		}
		if (!Files.exists(filePath.getParent())) {
			log.fine("CSV:: Parent missing, creating parent - " + filePath.getParent().toAbsolutePath());
			Files.createDirectories(filePath.getParent());
		}
		log.info("CSV:: Creating file with header...  file=" + filePath);
		String header = "AR Account,Customer: Job,Date,Sales Tax,Number,Class,Item,Description,Quantity,Rate,Amount,Taxable\r\n";
		Files.write(filePath, header.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		System.out.println("  IIF file created with header, file=" + filePath);
		log.info("CSV:: File created with header, file=" + filePath);
	}

	private void prepareOrder(Order order) {
		final OrderDetail detail;
		if (order.isPurchaseOrder()) {
			detail = new PurchaseOrderDetail();
		} else {
			detail = new PaymentOrderDetail(order.getPaymentMethod());
		}
		order.add(detail);
	}

	private void writeLine(Order order, OrderDetail detail) {
		// AR Account
		this.column("Accounts Receivable");

		// Customer: Job
		this.column(order.getCustomerName());

		// Date
		this.column(order.getOrderDate());

		// Sales Tax
		this.column("Minnesota");

		// Number
		this.column(order.getOrderID());

		// Class
		this.skip();

		// Item
		this.column(detail.getProductCode());

		// Description
		if (detail.isPurchaseOrder()) {
			this.column(order.getPONum());
		} else {
			this.skip();
		}

		// Quantity
		this.column(detail.getQuantity());

		// Rate
		this.skip();

		// Amount
		if (detail.isPayment()) {
			this.column(order.getPaymentAmount());
		} else {
			this.skip();
		}

		// Taxable
		this.column(detail.getTaxableProduct());

		this.rowEnd();
	}

}
