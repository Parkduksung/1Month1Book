
package com.rsupport.mobile.agent.utils;


import com.rsupport.util.log.RLog;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class XMLParser extends DefaultHandler {

    private SAXParserFactory factory;
    private SAXParser parser;
    private HashMap<String, String> map;
    private StringBuffer buffer;


    public XMLParser() {
        super();
        buffer = new StringBuffer();
        map = new HashMap<String, String>();
        factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> parse(String fileName) {
        map.clear();
        try {
            parser.parse(fileName, this);
        } catch (Exception e) {
            RLog.e(e);
        }
        return map;
    }

    public HashMap<String, String> parse(InputStream is) {
        map.clear();
        try {
            parser.parse(is, this);
        } catch (Exception e) {
            RLog.e(e);
        }
        return map;
    }

    public void startDocument() throws SAXException {
//    	RLog.v("parsing start");
    }

    public void endDocument() throws SAXException {
//    	RLog.v("parsing end");
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        buffer.setLength(0);
//    	RLog.v("s : " + qName);
//    	RLog.v("s : " + uri);
//    	RLog.v("s : " + localName);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
//    	map.put(qName, buffer.toString().trim());
        if (localName != null && !localName.equals("")) {
            map.put(localName, buffer.toString().trim());
        }
//    	RLog.v("c : " + buffer.toString().trim());
//    	RLog.v("e qName : " + qName);
//    	RLog.v("e localName : " + localName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        buffer.append(ch, start, length);
    }

//    public static void main(String[] args) {
//    	XMLParser parser = new XMLParser();
//    	HashMap<String, String> map = parser.parse("test.xml");
//
//		Set<String> keys = map.keySet();
//		Iterator<String> it = keys.iterator();
//		while (it.hasNext()) {
//			String key = (String) it.next();
//			String value = (String) map.get(key);
//
//			RLog.v(key + "=" + value);
//		}
//    }
}
