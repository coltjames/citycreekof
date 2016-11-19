package com.citycreek.of.exporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.citycreek.of.order.Order;
import com.citycreek.of.order.OrderDetail;

public class QuickBooksIIFExporter extends Exporter {

	private static final Logger log = Logger.getLogger(QuickBooksIIFExporter.class.getName());

	private Set<String> customers = new HashSet<>();

	private final boolean exportOrders;

	public QuickBooksIIFExporter(Path parentPath, boolean exportOrders) {
		super('\t', parentPath);
		this.exportOrders = exportOrders;
	}

	@Override
	public QuickBooksIIFExporter exportOrders(List<Order> orders) {
		orders.forEach(this::writeOrder);
		return this;
	}

	@Override
	protected String getFilename() {
		return this.exportOrders ? "citycreek_orders.iif" : "citycreek_customers.iif";
	}

	/**
	 * Create QuickBooks IIF file if it doesn't exist and append header.
	 *
	 * @throws IOException
	 */
	public void ensureFileExistsWithHeader() throws IOException {
		Path filePath = this.getFilePath().toAbsolutePath();
		if (Files.exists(filePath)) {
			log.fine("IIF:: File already exists, skip creating and writting header.");
			return; // Skip appending header, we assume it already has a header.
		}
		if (!Files.exists(filePath.getParent())) {
			log.fine("IIF:: Parent missing, creating parent - " + filePath.getParent().toAbsolutePath());
			Files.createDirectories(filePath.getParent());
		}
		log.info("IIF:: Creating file with header...  file=" + filePath);
		String header = "!CUST	NAME	BADDR1	BADDR2	BADDR3	BADDR4	BADDR5	SADDR1	SADDR2	SADDR3	SADDR4	SADDR5	PHONE1	PHONE2	FAXNUM	EMAIL	NOTE	CONT1	CONT2	CTYPE	TERMS	TAXABLE	LIMIT	RESALENUM	REP	TAXITEM	NOTEPAD	SALUTATION	COMPANYNAME	FIRSTNAME	MIDINIT	LASTNAME\r\n";
		if (this.exportOrders) {
			header += "!TRNS TRNSID TRNSTYPE DATE ACCNT NAME CLASS AMOUNT DOCNUM MEMO CLEAR TOPRINT PONUM ADDR1 ADDR2 ADDR3 ADDR4 ADDR5 SADDR1 SADDR2 SADDR3 SADDR4 SADDR5 TERMS SHIPVIA\r\n"
					+ "!SPL SPLID TRNSTYPE DATE ACCNT NAME CLASS AMOUNT DOCNUM MEMO CLEAR QNTY PRICE INVITEM TAXABLE EXTRA\r\n"
					+ "!ENDTRNS\r\n";

		}

		Files.write(filePath, header.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		System.out.println("  IIF file created with header, file=" + filePath);
		log.info("IIF:: File created with header, file=" + filePath);
	}

	private void writeOrder(Order order) {
		// We are now only writing out customers so let's avoid writing out duplicate customers.
		if (this.customers.contains(order.getCustomerID())) {
			log.info("IIF:: Skip already existing customer: " + order.getCustomerName());
			return; // skip
		} else {
			this.customers.add(order.getCustomerID());
		}

		this.writeCustomerLine(order);

		if (this.exportOrders) {
			this.writeTransactionLine(order);
			order.getDetails().forEach(detail -> this.writeDetailLine(order, detail));
			this.data.append("ENDTRNS\n");
			this.addOrderId(order.getOrderID());
		}
	}

	/**
	 * Judy asked for: customer name, bill to and ship to, and email address and phone
	 */
	private void writeCustomerLine(Order order) {
		log.info("IIF:: Write customer: " + order.getCustomerName());

		// CUST
		this.column("CUST", false);

		// NAME
		// The NAME in the customer IIF file has to be the same as the Customer: Job in the CSV file.
		this.column(order.getCustomer().getCustomerName());

		// BADDR1 - 5
		for (int line = 0; line < 5; line++) {
			this.column(order.getBillingAddressLineX(line));
		}

		// SADDR1 - 5
		for (int line = 0; line < 5; line++) {
			this.column(order.getShipAddressLineX(line));
		}

		// PHONE1
		// 8172444749
		this.column(order.getBillingPhoneNumber());

		// PHONE2
		this.skip();

		// FAXNUM
		this.skip();

		// EMAIL
		// alyciabentley@sbcglobal.net
		this.column(order.getCustomer().getEmailAddress());

		// NOTE
		this.skip();

		// CONT1
		// Alycia Bentley
		this.column(order.getCustomer().getFirstLastName());

		// CONT2 - Alternate contact
		this.skip();

		// CTYPE - Customer Type
		this.skip();

		// TERMS
		this.skip();

		// TAXABLE
		// N or Y
		this.column(order.isTaxable() ? "Y" : "N", false);

		// LIMIT RESALENUM REP TAXITEM NOTEPAD SALUTATION
		this.skip();
		this.skip();
		this.skip();
		this.skip();
		this.skip();
		this.skip();

		// COMPANYNAME
		this.column(order.getCustomer().getCompanyName());

		// FIRSTNAME
		// Alycia
		this.column(order.getCustomer().getFirstName());

		// MIDINIT
		this.skip();

		// LASTNAME
		// Bentley
		this.column(order.getCustomer().getLastName());

		this.rowEnd();
	}

	private void writeTransactionLine(Order order) {
		// TRNS
		this.column("TRNS", false);

		// TRNSID
		this.column(order.getOrderID());

		// TRNSTYPE
		this.column("INVOICE");

		// DATE
		this.column(order.getOrderDate());

		// ACCNT
		this.column("Accounts Receivable");

		// NAME
		this.column(order.getOrderName());

		// CLASS
		this.column("website");

		// AMOUNT
		this.column(order.getPaymentAmount());

		// DOCNUM
		this.column(order.getOrderID());

		// MEMO CLEAR TOPRINT PONUM
		this.skip();
		this.column("N");
		this.column("N");
		this.column(order.getPONum());

		// ADDR1 - 5
		for (int line = 0; line < 5; line++) {
			this.column(order.getBillingAddressLineX(line));
		}

		// SADDR1 - 5
		for (int line = 0; line < 5; line++) {
			this.column(order.getShipAddressLineX(line));
		}

		// TERMS
		this.column(order.getPaymentMethod());

		// SHIPVIA
		// USPS
		this.column(order.getShipMethod());

		this.rowEnd();
	}

	private void writeDetailLine(Order order, OrderDetail detail) {
		// !SPL
		this.column("SPL", false);

		// SPLID
		this.skip();

		// TRNSTYPE
		this.column("INVOICE", false);

		// DATE
		this.column(order.getOrderDate());

		// ACCNT - Sales, Discounts
		this.skip();

		// NAME - od.ProductName
		this.column(detail.getProductName());

		// CLASS
		this.skip();

		// AMOUNT - od.ProductPrice negated
		this.column(-detail.getProductPrice());

		// DOCNUM
		this.skip();

		// MEMO
		this.column(detail.getProductName());

		// CLEAR
		this.column("N");

		// QNTY
		this.column(detail.getQuantity());

		// PRICE - od.TotalPrice
		this.column(detail.getTotalPrice());

		// INVITEM
		this.column(detail.getProductCode());

		// TAXABLE - od.TaxableProduct
		this.column(detail.getTaxableProduct(), false);

		// EXTRA
		this.skip();

		this.rowEnd();
	}
}
