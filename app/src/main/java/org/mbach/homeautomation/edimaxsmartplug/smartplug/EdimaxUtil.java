package org.mbach.homeautomation.edimaxsmartplug.smartplug;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class EdimaxUtil {

	static Document getDocumentFromString(String xml) throws ParserConfigurationException, SAXException, IOException {

		InputSource source = new InputSource(new StringReader(xml));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(source);
		document.normalize();

		return document;
	}

	static int numberFromCharCode(int charCode) throws Exception {
		if (charCode >= Character.codePointAt("0", 0) && charCode <= Character.codePointAt("9", 0)) {
			return charCode - Character.codePointAt("0", 0);
		}
		if (charCode >= Character.codePointAt("a", 0) && charCode <= Character.codePointAt("z", 0)) {
			return charCode - Character.codePointAt("a", 0) + 10;
		}
		if (charCode >= Character.codePointAt("A", 0) && charCode <= Character.codePointAt("Z", 0)) {
			return charCode - Character.codePointAt("A", 0) + 36;
		}
		if (charCode == Character.codePointAt("+", 0)) {
			return 62;
		}
		if (charCode == Character.codePointAt("/", 0)) {
			return 63;
		}
		throw new Exception("Unknown character code: '" + charCode + "'");
	}
}