package relish.verify;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CBMCXmlOutput {

  private Document doc;

  public CBMCXmlOutput(String filename) {
    doc = parseXMLFile(filename);
  }

  private Document parseXMLFile(String filename) {
    try {
      File xmlFile = new File(filename);
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(xmlFile);
      doc.getDocumentElement().normalize();
      return doc;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("exception in xml parser");
    }
  }

  public String getCProverStatus() {
    NodeList nodeList = doc.getElementsByTagName("cprover-status");
    assert nodeList.getLength() == 1 : nodeList.getLength();
    Node node = nodeList.item(0);
    assert node.getNodeType() == Node.ELEMENT_NODE : node.getNodeType();
    Element element = (Element) node;
    return element.getTextContent();
  }

  public CBMCTypedValue getVariableValue(String varName) {
    NodeList nodeList = doc.getElementsByTagName("assignment");
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        Element fullLhs = getSingletonChildren(element, "full_lhs");
        if (fullLhs.getTextContent().equals(varName)) {
          Element type = getSingletonChildren(element, "type");
          Element value = getSingletonChildren(element, "full_lhs_value");
          assert type != null && value != null : varName;
          return new CBMCTypedValue(type.getTextContent(), value.getTextContent());
        }
      }
    }
    throw new RuntimeException("Cannot find value for variable: " + varName);
  }

  // assume elem has only one child element with tag tagName
  // return that child element
  private Element getSingletonChildren(Element elem, String tagName) {
    NodeList nodeList = elem.getElementsByTagName(tagName);
    assert nodeList.getLength() == 1 : elem;
    assert nodeList.item(0).getNodeType() == Node.ELEMENT_NODE : elem;
    return (Element) nodeList.item(0);
  }

  public static class CBMCTypedValue {
    public final String type;
    public final String value;

    public CBMCTypedValue(String type, String value) {
      this.type = type;
      this.value = value;
    }

  }

}
