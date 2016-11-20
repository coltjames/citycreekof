package com.citycreek.of.exporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Logger;

import com.citycreek.of.order.Order;
import com.citycreek.of.order.OrderDetail;
import com.citycreek.of.order.PaymentOrderDetail;
import com.citycreek.of.order.PurchaseOrderDetail;

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
		System.out.println("  CSV file created with header, file=" + filePath);
		log.info("CSV:: File created with header, file=" + filePath);
	}

	private void prepareOrder(Order order) {
		final OrderDetail detail;
		if (order.isPurchaseOrder()) {
			detail = new PurchaseOrderDetail(order.getPONum());
		} else {
			detail = new PaymentOrderDetail(order.getPaymentMethod(), order.getPaymentAmount());
		}
		order.add(detail);
	}

	private void writeLine(Order order, OrderDetail detail) {
		// AR Account
		this.column("Accounts Receivable");

		// Customer: Job
		// The NAME in the customer IIF file has to be the same as the Customer: Job in the CSV file.
		this.column(order.getCustomer().getCustomerName());

		// Date
		this.column(order.getOrderDate());

		// Sales Tax
		// The sales tax column can say Minnesota on all lines of all invoices.or the column can be blank on non taxable
		// invoices whichever is easier but must say Minnesota on all lines of a taxable invoice.
		this.column("Minnesota");

		// Number
		this.column(order.getOrderID());

		// Class
		this.skip();

		// Item
		this.column(detail.getProductCode());

		// Description
		this.column(detail.getProductName());

		// Quantity
		this.column(detail.getQuantity());

		// Rate - always positive even for discounts - QuickBooks negates discount items by product code.
		this.column(Math.abs(detail.getProductPrice()));

		// Amount - always positive even for discounts - QuickBooks negates discount items by product code.
		this.column(Math.abs(detail.getTotalPrice()));

		// Taxable
		// The taxable column can be blank for all items.
		this.skip();

		this.rowEnd();
	}

}
