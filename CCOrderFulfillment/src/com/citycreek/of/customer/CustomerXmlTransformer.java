package com.citycreek.of.customer;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cjc.util.xml.XmlTool;

public class CustomerXmlTransformer {

	private interface Xml {

		// String ROOT = "xmldata";
		String CUSTOMERS = "Customers";
	}

	public void fromXml(File xmlFile, Map<String, Customer> customers) throws Exception {
		try (FileInputStream in = new FileInputStream(xmlFile)) {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document doc = builder.parse(in);

			final NodeList rootNodes = doc.getChildNodes();
			final Element e = (Element) rootNodes.item(0);
			if (e != null) {
				// for each 'property' element
				for (final Element customerElement : XmlTool.getElements(e, Xml.CUSTOMERS)) {
					final NodeList children = customerElement.getChildNodes();
					final Customer customer = new Customer();
					for (int i = 0; i < children.getLength(); i++) {
						final Node child = children.item(i);
						if (child.getNodeType() != Node.ELEMENT_NODE) {
							continue;
						}
						final String oKey = child.getNodeName();
						final String v = child.getTextContent();
						if (Objects.equals(v, "anonymous_user")) {
							// skip anonymous users
							break;
						}
						customer.add(oKey, v);
					}
					if (customer.isValid()) {
						customers.put(customer.getCustomerId(), customer);
					}
				}
			}
		}
	}
}
