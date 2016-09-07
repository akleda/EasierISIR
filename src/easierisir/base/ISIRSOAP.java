/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easierisir.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * ISIRSOAP requests the SOAP webservice which runs on the WS_URL if the subject
 * specified by IC or RC can be found
 *
 * @author Adelka
 */
public class ISIRSOAP {

    private static final String WS_URL = "https://isir.justice.cz:8443/isir_cuzk_ws/IsirWsCuzkService";
    private String sSOAPXml;
    private String sElementName, sElementValue;
    private SOAPMessage smResponse = null;

    /**
     * Constructor which allows to set the request directly
     *
     * @param xmlSOAPRequest
     */
    public ISIRSOAP(String xmlSOAPRequest) {
        this.sSOAPXml = xmlSOAPRequest;
        smResponse = getResponse();
    }

    /**
     * Constructor which creates the request from the parameters itself
     *
     * @param elementName Element to be requested, e.g. ic
     * @param elementValue Value of the element to be requested, e.g. "00123456"
     */
    public ISIRSOAP(String elementName, String elementValue) {
        this.sElementName = elementName;
        this.sElementValue = elementValue;
        this.sSOAPXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://isirws.cca.cz/types/\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<typ:getIsirWsCuzkDataRequest>"
                + "<" + elementName + ">" + elementValue + "</" + elementName + ">"
                + "</typ:getIsirWsCuzkDataRequest>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
        smResponse = getResponse();
    }

    /**
     * Method to get response from the web service
     *
     * @return
     */
    public SOAPMessage getResponse() {
        try {
            MessageFactory messageFactory = MessageFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(sSOAPXml.getBytes());
            SOAPMessage soapMessage = messageFactory.createMessage(null, bais);
            sendSoapRequest(WS_URL, soapMessage);
        } catch (SOAPException | IOException ex) {
            smResponse = null;
            Logger.getLogger(ISIRSOAP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return smResponse;
    }

    /**
     * Private method wich sends the request to the web service
     *
     * @param WS_URL
     * @param soapMessage
     */
    private void sendSoapRequest(String WS_URL, SOAPMessage soapMessage) {
        final boolean isHttps = WS_URL.toLowerCase().startsWith("https");
        HttpsURLConnection httpsConnection = null;
        // Open HTTPS connection
        if (isHttps) {
            // Open HTTPS connection
            URL url;
            try {
                url = new URL(WS_URL);
                httpsConnection = (HttpsURLConnection) url.openConnection();
                //using only one trustful host
                httpsConnection.setHostnameVerifier(new TrustAllHosts());
                //connect
                httpsConnection.connect();
                SOAPConnection soapConnection;
                soapConnection = SOAPConnectionFactory.newInstance().createConnection();
                smResponse = soapConnection.call(soapMessage, WS_URL);
                soapConnection.close();
            } catch (IOException|SOAPException ex) {
                smResponse = null;
                Logger.getLogger(ISIRSOAP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Send HTTP SOAP request and get response
        
        
        // Close HTTPS connection
        if (isHttps) {
            httpsConnection.disconnect();
        }
    }

    /**
     * Private class which helps to verify that the host is trustfull. In this
     * case there is only one host specified by the application therefor we know
     * it is trustfull.
     */
    private static class TrustAllHosts implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

}
