package com.cjc.util.xml;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.cjc.util.PropertiesUtil;

/**
 * A Object xml transformer. This interface must be implemented if a custom xml transformer is required.
 * 
 * @author <a href="mailto:brian@code42.com">Brian Bispala </a>
 */
public interface IXmlTransformer {

	/**
	 * Populate a target Object from xml.
	 * 
	 * @param target
	 * @param parent
	 * @return the populated target (possibly a new instance)
	 * @throws Exception
	 */
	public List<PropertiesUtil> fromXml(List<PropertiesUtil> target, Node parent) throws Exception;

	/**
	 * Convert the source Object to xml.
	 * 
	 * @param Object
	 * @param doc
	 * @param parent
	 * @throws Exception
	 */
	public void toXml(Object source, Document doc, Node parent) throws Exception;
}
