package com.citycreek.of;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class QuickBooksExporter extends Exporter {

	public QuickBooksExporter(Path parentPath) {
		super('\t', parentPath);
	}

	@Override
	public QuickBooksExporter exportOrders(List<Order> orders) {
		orders.forEach(this::writeOrder);
		return this;
	}

	@Override
	protected String getFilename() {
		return "citycreek_orders.iif";
	}

	/**
	 * Create QuickBooks IIF file if it doesn't exist and append header.
	 * 
	 * @throws IOException
	 */
	public void ensureFileExistsWithHeader() throws IOException {
		if (Files.exists(this.getFilePath())) {
			return; // Skip appending header, we assume it already has a header.
		}
		String header = "!CUST	NAME	BADDR1	BADDR2	BADDR3	BADDR4	BADDR5	SADDR1	SADDR2	SADDR3	SADDR4	SADDR5	PHONE1	PHONE2	FAXNUM	EMAIL	NOTE	CONT1	CONT2	CTYPE	TERMS	TAXABLE	LIMIT	RESALENUM	REP	TAXITEM	NOTEPAD	SALUTATION	COMPANYNAME	FIRSTNAME	MIDINIT	LASTNAME"
				+ "!TRNS	TRNSID	TRNSTYPE	DATE	ACCNT	NAME	CLASS	AMOUNT	DOCNUM	MEMO	CLEAR	TOPRINT	PONUM	ADDR1	ADDR2	ADDR3	ADDR4	ADDR5	SADDR1	SADDR2	SADDR3	SADDR4	SADDR5	TERMS	SHIPVIA"
				+ "!SPL	SPLID	TRNSTYPE	DATE	ACCNT	NAME	CLASS	AMOUNT	DOCNUM	MEMO	CLEAR	QNTY	PRICE	INVITEM	TAXABLE	EXTRA";
		Files.write(this.getFilePath(), header.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.APPEND);
	}

	private void writeOrder(Order order) {
		this.writeCustomerLine(order);
		this.writeTransactionLine(order);
		order.getDetails().forEach(detail -> this.writeDetailLine(order, detail));
		this.data.append("!ENDTRNS");
		this.addOrderId(order.getOrderID());
	}

	private void writeCustomerLine(Order order) {
		final String firstName = order.getFirstName();
		final String lastName = order.getLastName();

		// CUST
		this.column("CUST", false);

		// NAME
		// "Bentley, Alycia K"
		this.column(lastName + ", " + firstName);

		// BADDR1
		// "Alycia Bentley"
		this.column(order.getBillingName());

		// BADDR2
		// "5520 Sedalia Trl Apt 513"
		this.column(order.getBillingAddress1());

		// BADDR3
		this.column(order.getBillingAddress2());

		// BADDR4
		// "Benbrook, TX 76126"
		this.column(order.getBillingAddressCityStateZip());

		// BADDR5
		this.column(order.getBillingCountry());

		// SADDR1
		// "Alycia Bentley"
		this.column(order.getShipName());

		// SADDR2
		// "5520 Sedalia Trl Apt 513"
		this.column(order.getShipAddress1());

		// SADDR3
		this.column(order.getShipAddress2());

		// SADDR4
		// "Benbrook, TX 76126"
		this.column(order.getShipAddressCityStateZip());

		// SADDR5
		this.column(order.getShipCountry());

		// PHONE1
		// 8172444749
		this.column(order.getCustomer().getPhoneNumber());

		// PHONE2
		this.skip();

		// FAXNUM
		this.column(order.getCustomer().getFaxNumber());

		// EMAIL
		// alyciabentley@sbcglobal.net
		this.column(order.getCustomer().getEmailAddress());

		// NOTE
		this.skip();

		// CONT1
		// Alycia Bentley
		this.column(firstName + " " + lastName);

		// CONT2
		this.skip();

		// CTYPE
		this.column(order.getCustomer().getCustomerType());

		// TERMS
		this.skip();

		// TAXABLE
		// N
		this.column("N", false); // TODO Judy?

		// LIMIT RESALENUM REP TAXITEM NOTEPAD SALUTATION
		this.skip();
		this.skip();
		this.skip();
		this.skip();
		this.skip();
		this.skip();

		// COMPANYNAME
		this.column(order.getCompanyName());

		// FIRSTNAME
		// Alycia
		this.column(firstName);

		// MIDINIT
		this.skip();

		// LASTNAME
		// Bentley
		this.column(lastName);

		this.rowEnd();
	}

	private void writeTransactionLine(Order order) {
		final String firstName = order.getFirstName();
		final String lastName = order.getLastName();

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
		// "Bentley, Alycia K"
		this.column(lastName + ", " + firstName);

		// CLASS
		this.column("website");

		// AMOUNT
		this.column(order.getPaymentAmount());

		// DOCNUM
		this.column(order.getOrderID());

		// MEMO CLEAR TOPRINT PONUM
		this.data.append("N\tY\t\t\t");

		// ADDR1
		// "Alycia Bentley"
		this.column(order.getShipName());

		// ADDR2
		// "5520 Sedalia Trl Apt 513"
		this.column(order.getShipAddress1());

		// ADDR3
		this.column(order.getShipAddress2());

		// ADDR4
		// "Benbrook, TX 76126"
		this.column(order.getShipAddressCityStateZip());

		// ADDR5
		this.column(order.getShipCountry());

		// SADDR1
		// "Alycia Bentley"
		this.column(order.getShipName());

		// SADDR2
		// "5520 Sedalia Trl Apt 513"
		this.column(order.getShipAddress1());

		// SADDR3
		this.column(order.getShipAddress2());

		// SADDR4
		// "Benbrook, TX 76126"
		this.column(order.getShipAddressCityStateZip());

		// SADDR5
		this.column(order.getShipCountry());

		// TERMS
		// Creditcard
		// PaymentMethodID
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

		// AMOUNT - od.ProductPrice
		this.column(detail.getProductPrice());

		// DOCNUM
		this.skip();

		// MEMO
		this.skip();

		// CLEAR
		this.skip();

		// QNTY
		this.column(detail.getQuantity());

		// PRICE - od.TotalPrice
		this.column(detail.getTotalPrice(), false);

		// INVITEM
		this.column(detail.getProductCode());

		// TAXABLE - od.TaxableProduct
		this.column(detail.getTaxableProduct(), false);

		// EXTRA
		this.skip();

		this.rowEnd();
	}
}