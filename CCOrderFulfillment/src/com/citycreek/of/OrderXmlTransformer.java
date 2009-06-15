package com.citycreek.of;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cjc.util.PropertiesUtil;
import com.cjc.util.xml.IXmlTransformer;
import com.cjc.util.xml.XmlTool;

/**
 * 
 */
public class OrderXmlTransformer implements IXmlTransformer {

	private interface Xml {

		// String ROOT = "xmldata";
		String ORDERS = "Orders";
		String ORDER_DETAILS = OrderFulfillment.ORDER_DETAILS;
	}

	public Object fromXml(Object target, Node parent) throws Exception {
		final List<PropertiesUtil> orders = (List<PropertiesUtil>) target;
		final NodeList rootNodes = parent.getChildNodes();
		final Element e = (Element) rootNodes.item(0);
		if (e != null) {
			// for each 'property' element
			for (final Element order : XmlTool.getElements(e, Xml.ORDERS)) {
				final List<PropertiesUtil> newOrders = this.fromXmlOrder(order);
				orders.addAll(newOrders);
			}
		}
		return target;
	}

	private List<PropertiesUtil> fromXmlOrder(Element order) {
		final List<PropertiesUtil> orders = new ArrayList<PropertiesUtil>();
		final PropertiesUtil shared = new PropertiesUtil(new Properties());
		final NodeList oChildren = order.getChildNodes();
		for (int i = 0; i < oChildren.getLength(); i++) {
			final Node oChild = oChildren.item(i);
			if (oChild.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			final String oKey = oChild.getNodeName();
			if (Xml.ORDER_DETAILS.equals(oKey)) {
				final PropertiesUtil detail = new PropertiesUtil(new Properties());
				final NodeList odChildren = oChild.getChildNodes();
				for (int j = 0; j < odChildren.getLength(); j++) {
					final Node odChild = odChildren.item(j);
					final String odKey = odChild.getNodeName();
					if (odChild.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					final String v = odChild.getTextContent();
					detail.setProperty(odKey, v);
				}
				orders.add(detail);
			} else {
				final String v = oChild.getTextContent();
				shared.setProperty(oKey, v);
			}
		}
		// Add the shared to each detail
		for (PropertiesUtil orderDetail : orders) {
			orderDetail.setFromAnother(shared.getProperties());
		}
		return orders;
	}

	public void toXml(Object source, Document doc, Node parent) throws Exception {
		// We only read, never write.
	}

}
