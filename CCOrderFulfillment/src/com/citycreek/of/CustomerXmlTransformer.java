package com.citycreek.of;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cjc.util.LangUtil;
import com.cjc.util.PropertiesUtil;
import com.cjc.util.xml.IXmlTransformer;
import com.cjc.util.xml.XmlTool;

public class CustomerXmlTransformer implements IXmlTransformer {

	private interface Xml {

//		String ROOT = "xmldata";
		String CUSTOMERS = "Customers";
	}

	public List<PropertiesUtil> fromXml(List<PropertiesUtil> orders, Node parent) throws Exception {
		final NodeList rootNodes = parent.getChildNodes();
		final Element e = (Element) rootNodes.item(0);
		if (e != null) {
			// for each 'property' element
			for (final Element customer : XmlTool.getElements(e, Xml.CUSTOMERS)) {
				final NodeList children = customer.getChildNodes();
				final PropertiesUtil customerDetails = new PropertiesUtil(new Properties());
				String customerId = null;
				for (int i = 0; i < children.getLength(); i++) {
					final Node child = children.item(i);
					if (child.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					final String oKey = child.getNodeName();
					final String v = child.getTextContent();
					if (Objects.equals(v, "anonymous_user")) {
						// skip anonymous users
						customerId = null;
						break;
					}
					customerDetails.setProperty(oKey, v);
					if (oKey.equals("CustomerID")) {
						customerId = v;
					}
				}
				if (LangUtil.hasValue(customerId)) {
					this.merge(customerId, customerDetails, orders);
				}
			}
		}
		return orders;
	}

	private void merge(String customerId, PropertiesUtil customerDetails, List<PropertiesUtil> orders) {
		for (PropertiesUtil order : orders) {
			final String cid = order.getOptional("CustomerID");
			if (cid.equals(customerId)) {
				order.setFromAnother(customerDetails.getProperties());
			}
		}
	}

	public void toXml(Object source, Document doc, Node parent) throws Exception {
		// We only read, never write.
	}

}
