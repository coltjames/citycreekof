package com.citycreek.of.exporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Logger;

import com.citycreek.of.order.Order;

public abstract class Exporter {

	private static final Logger log = Logger.getLogger(Exporter.class.getName());

	private int count = 0;
	private char delimiter;
	protected final StringBuilder data = new StringBuilder();
	private final Path parentPath;

	public Exporter(char delimiter, Path parentPath) {
		this.delimiter = delimiter;
		this.parentPath = parentPath;
	}

	public int getCount() {
		return this.count;
	}

	public abstract Exporter exportOrders(List<Order> orders);

	protected abstract String getFilename();

	protected Path getFilePath() {
		Path file = this.parentPath.resolve(this.getFilename());
		return file;
	}

	public Path write() throws IOException {
		Path file = this.getFilePath();
		Files.write(file, this.data.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.APPEND);
		System.out.println("  Data written to file - " + this.getFilePath().toAbsolutePath());
		log.info("Data written to file - " + this.getFilePath().toAbsolutePath());
		return file;
	}

	protected void column(String value, boolean quotes) {
		if (quotes) {
			this.data.append("\"").append(value).append("\"").append(this.delimiter);
		} else {
			this.data.append(value).append(this.delimiter);
		}
	}

	protected void column(String value) {
		this.column(value, true);
	}

	protected void column(Number value) {
		this.column(value.toString(), false);
	}

	protected void rowEnd() {
		this.data.append("\r\n");
		this.count++;
	}

	protected void headerRow(String header) {
		this.data.append(header).append("\n");
	}

	protected void skip() {
		this.data.append(this.delimiter);
	}
}
