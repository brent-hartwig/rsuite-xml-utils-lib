package com.rsicms.rsuite.utils.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.extensions.ExecutionContext;

public class DomUtils {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(DomUtils.class);

	/**
	 * Get the qualified name of the given element.
	 * 
	 * @param elem
	 * @return QName of given element.
	 */
	public static QName getQName(Element elem) {
		String nsUri = StringUtils.EMPTY;
		String localName = StringUtils.EMPTY;
		if (elem != null) {
			nsUri = elem.getNamespaceURI();
			localName = elem.getLocalName();
		}
		return new QName(nsUri, localName);
	}

	/**
	 * Get an instance of <code>Document</code>, starting from an
	 * <code>InputStream</code>.
	 * <p>
	 * Additional parameters may be added later, if future callers need more
	 * control.
	 * 
	 * @param context
	 * @param inputStream
	 * @return Document
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document getDocument(ExecutionContext context, InputStream inputStream)
			throws SAXException, IOException {
		return context.getXmlApiManager().constructNonValidatingDocumentBuilder().parse(inputStream);
	}

	/**
	 * Construct a new Document.
	 * <p>
	 * An alternative implementation of
	 * com.reallysi.tools.DomUtils.newDocument().
	 * 
	 * @return a new Document
	 * @throws ParserConfigurationException
	 */
	public static Document newDocument() throws ParserConfigurationException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	}

	/**
	 * Get the node value of the first child having the specified name.
	 * <p>
	 * An alternative implementation of
	 * com.reallysi.tools.DomUtils.findFirstChildString().
	 * 
	 * @param elem
	 * @param childElemName
	 * @return the qualifying child's node value, or null when there is no such
	 *         child.
	 */
	public static String findFirstChildString(Element elem, String childElemName) {
		NodeList children = elem.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().equals(childElemName)) {
				return child.getNodeValue();
			}
		}
		return null;
	}

	/**
	 * Convert a
	 * <code>Document<code> to a string, which can be helpful for debugging purposes.
	 * <p>
	 * The XML declaration is included in the response by default.  To override, submit true
	 * into {@link #toString(Document, boolean)}.
	 * <p>
	 * Credit: http://stackoverflow.com/questions/2567416/document-to-string
	 * 
	 * &#64;param doc
	 * @return String representation of <code>Document</code>
	 */
	public static String toString(Document doc) {
		return toString(doc, false);
	}

	/**
	 * Convert a
	 * <code>Document<code> to a string, which can be helpful for debugging purposes.
	 * <p>
	 * Credit: http://stackoverflow.com/questions/2567416/document-to-string
	 * 
	 * &#64;param doc
	 * &#64;param omitXmlDeclaration Submit true to exclude the XML declaration.
	 * @return String representation of <code>Document</code>
	 */
	public static String toString(Document doc, boolean omitXmlDeclaration) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}

	/**
	 * Serialize a node the same way ManagedObject#getInputStream does, but
	 * allow caller to specify some options.
	 * 
	 * @param context
	 * @param node
	 * @param includeXMLDeclaration
	 * @param includeDoctypeDeclaration
	 * @param encoding
	 * @since RSuite 3.6.2.2
	 * @return A string representation of the given Node, after applying
	 *         options.
	 * @throws RSuiteException
	 * @throws TransformerException
	 */
	public static String serializeToString(ExecutionContext context, Node node, boolean includeXMLDeclaration,
			boolean includeDoctypeDeclaration, String encoding) throws RSuiteException, TransformerException {
		if (node == null)
			return null;

		DOMSource ds = new DOMSource(node);
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult(sw);
		Transformer tf = context.getXmlApiManager().getTransformer((File) null);
		tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, includeXMLDeclaration ? "no" : "yes");
		Document doc = node.getOwnerDocument();

		if (null != doc) {
			String docxmlenc = doc.getXmlEncoding();
			if (null != docxmlenc) {
				encoding = docxmlenc;
			}
		}

		tf.setOutputProperty(OutputKeys.ENCODING, encoding);
		if (includeDoctypeDeclaration && doc.getDoctype() != null) {
			DocumentType doctype = doc.getDoctype();
			if (doctype.getPublicId() != null)
				tf.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
			if (doctype.getSystemId() != null)
				tf.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
		}

		tf.transform(ds, sr);

		return sw.toString();
	}

}
