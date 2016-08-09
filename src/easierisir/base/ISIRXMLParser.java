/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easierisir.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * ISIRXMLParser gets the response from the web service and parses it into an ArrayList with a HashMap
 * @author Adelka
 */
public class ISIRXMLParser {
    private ArrayList<HashMap> alAllData = new ArrayList<>();
    private int numOfData = 0;
    
    /**
     * Constructor with one parameter. It creates the ArrayList of HashMaps for every 'data' or 'stav' element of the response
     * @param smISIRResponse - response from the web service
     * @throws SOAPException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException 
     */
    public ISIRXMLParser(SOAPMessage smISIRResponse) throws SOAPException, IOException, ParserConfigurationException, SAXException{
        ByteArrayOutputStream baosISIRResponse = new ByteArrayOutputStream();
        DocumentBuilderFactory dbfISIRResponse = DocumentBuilderFactory.newInstance();
        DocumentBuilder dbISIRResponse;
        dbISIRResponse = dbfISIRResponse.newDocumentBuilder();
        smISIRResponse.writeTo(baosISIRResponse);
        InputStream is = new ByteArrayInputStream(baosISIRResponse.toByteArray());
        Document doc = dbISIRResponse.parse(is);
        if (doc != null){
            Node n = doc.getFirstChild();
            createDataArray(n);
        } else System.out.println("Nenacten dokument");
    }
    
    private void createDataArray(Node parentNode) {
        if(parentNode == null) {
            return;
        }
        String nodeName = parentNode.getNodeName();
        if (nodeName!=null && !"data".equals(nodeName) && !"stav".equals(nodeName)) createDataArray(parentNode.getFirstChild()); 
        else if ("data".equals(nodeName) || "stav".equals(nodeName)){
            if ("data".equals(nodeName) ) numOfData += 1;
            if ("stav".equals(nodeName) ) numOfData = 1;
            getAlAllData().add(new HashMap());
            getAlAllData().get(numOfData-1).put(nodeName, numOfData);
            createMap(parentNode.getFirstChild());
        }

        if(parentNode.getNextSibling()!=null && "data".equals(parentNode.getNextSibling().getNodeName())) {
            createDataArray(parentNode.getNextSibling());
        }
    }
    private void createMap(Node firstChild) {
            getAlAllData().get(numOfData-1).put(firstChild.getNodeName(),firstChild.getTextContent());
            if (firstChild.getNextSibling()!=null)
            createMap(firstChild.getNextSibling());
    }

    public static void printText(Object k, Object v) {
        System.out.println("<"+k+">"+v+"</"+k+">");
    }

    /**
     * @return the alAllData
     */
    public ArrayList<HashMap> getAlAllData() {
        return alAllData;
    }
    
 
}
