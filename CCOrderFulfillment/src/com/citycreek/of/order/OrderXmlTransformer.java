package com.citycreek.of.order;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cjc.util.xml.XmlTool;

/**
 *
 */
public class OrderXmlTransformer {

	private interface Xml {

		// String ROOT = "xmldata";
		String ORDERS = "Orders";
		String ORDER_DETAILS = "OrderDetails";
	}

	public void fromXml(File xmlFile, List<Order> orders) throws Exception {
		try (FileInputStream in = new FileInputStream(xmlFile)) {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document doc = builder.parse(in);

			final NodeList rootNodes = doc.getChildNodes();
			final Element e = (Element) rootNodes.item(0);
			if (e != null) {
				// for each 'property' element
				for (final Element order : XmlTool.getElements(e, Xml.ORDERS)) {
					final Order newOrder = this.fromXmlOrder(order);
					if (newOrder.isValid()) {
						orders.add(newOrder);
					}
				}
			}
		}
	}

	private Order fromXmlOrder(Element orderElement) {
		final Order order = new Order();
		final NodeList oChildren = orderElement.getChildNodes();
		for (int i = 0; i < oChildren.getLength(); i++) {
			final Node oChild = oChildren.item(i);
			if (oChild.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			final String oKey = oChild.getNodeName();
			if (Xml.ORDER_DETAILS.equals(oKey)) {
				final OrderDetail detail = new OrderDetail();
				final NodeList odChildren = oChild.getChildNodes();
				for (int j = 0; j < odChildren.getLength(); j++) {
					final Node odChild = odChildren.item(j);
					final String odKey = odChild.getNodeName();
					if (odChild.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					final String v = odChild.getTextContent();
					detail.add(odKey, v);
				}
				order.add(detail);
			} else {
				final String v = oChild.getTextContent();
				order.add(oKey, v);
			}
		}
		return order;
	}
}
