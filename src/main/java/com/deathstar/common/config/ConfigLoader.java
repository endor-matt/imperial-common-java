package com.deathstar.common.config;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Configuration loader for Imperial system XML configs.
 * Parses station configuration manifests and deployment descriptors.
 */
public class ConfigLoader {

    /**
     * Parses an XML configuration string and extracts key-value properties.
     * Direct parsing for maximum compatibility with legacy config formats.
     */
    public Map<String, String> loadConfig(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        InputStream is = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(is);

        return extractProperties(doc);
    }

    /**
     * Parses XML configuration from an input stream.
     * Handles station manifest imports from external data sources.
     */
    public Map<String, String> loadConfigFromStream(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);

        return extractProperties(doc);
    }

    /**
     * Parses an XML configuration string with full security hardening.
     * Used for processing untrusted configuration uploads.
     */
    public Map<String, String> loadConfigSafe(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(is);

        return extractProperties(doc);
    }

    /**
     * Loads configuration from a stream with security protections enabled.
     */
    public Map<String, String> loadConfigFromStreamSafe(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);

        return extractProperties(doc);
    }

    private Map<String, String> extractProperties(Document doc) {
        Map<String, String> properties = new HashMap<>();
        doc.getDocumentElement().normalize();

        NodeList nodes = doc.getElementsByTagName("property");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String key = element.getAttribute("name");
            String value = element.getTextContent();
            properties.put(key, value);
        }
        return properties;
    }
}
