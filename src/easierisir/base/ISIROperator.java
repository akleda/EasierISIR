/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easierisir.base;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.xml.sax.SAXException;

/**
 * ISIROperator work calls the ISIR classes to get the final csv which is made
 * by OpenCSV jar
 *
 * @author Adelka
 */
public class ISIROperator extends Task {

    public static final boolean MAKE_ALL_CSV = false;
    public static final char CSV_SEPARATOR = ';';

    private String sPathToCSV;
    private String sPathToCreateCSV;
    private boolean makeAllCSV;
    private char cSeparator;

    private ISIRCSVLinesMaker isirLines;

    private List<String[]> alAllSubjects;
    private CSVWriter cwSubjectsFound, cwSubjectsNotFound;
    private ArrayList<String[]> alRecords = new ArrayList<>();

    private int iNumOfAllSubjects = 0, iNumOfRequestedSubjects = 0;
    private String sType;
    String[] httpResponse;
    
    SOAPMessage response;
    ArrayList<SOAPMessage> alResponses;

    /**
     * Constructor with one parameter
     *
     * @param sPathToCSV Path to the CSV with subjects to be requested
     */
    public ISIROperator(String sPathToCSV) {
        this(sPathToCSV, getSavePath(sPathToCSV), CSV_SEPARATOR, MAKE_ALL_CSV);
    }

    /**
     * Constructor with two parameters
     *
     * @param sPathToCSV Path to the CSV with subjects to be requested
     * @param cSeparator Field separator to be used in case it is different from
     * the default CSV_SEPARATOR
     */
    public ISIROperator(String sPathToCSV, char cSeparator) {
        this(sPathToCSV, getSavePath(sPathToCSV), cSeparator, MAKE_ALL_CSV);
    }

    /**
     * Constructor with three parameters
     *
     * @param sPathToCSV Path to the CSV with subjects to be requested
     * @param cSeparator Field separator to be used in case it is different from
     * the default CSV_SEPARATOR
     * @param makeAllCSV Specifies if all CSV files are to be created or only
     * for the found subjects
     */
    public ISIROperator(String sPathToCSV, char cSeparator, boolean makeAllCSV) {
        this(sPathToCSV, getSavePath(sPathToCSV), cSeparator, makeAllCSV);
    }

    /**
     * Constructor which loads specified csv and creates a new one from data get
     * by SOAP request from the webservice
     *
     * @param sPathToCSV Path to the CSV with subjects to be requested
     * @param sPathToCreateCSV Path where the result CSV file/files should be
     * saved
     * @param cSeparator Field separator to be used in case it is different from
     * the default CSV_SEPARATOR
     * @param makeAllCSV Specifies if all CSV files are to be created or only
     * for the found subjects
     */
    public ISIROperator(String sPathToCSV, String sPathToCreateCSV, char cSeparator, boolean makeAllCSV) {
        this.sPathToCSV = sPathToCSV;
        this.sPathToCreateCSV = sPathToCreateCSV;
        this.cSeparator = cSeparator;
        this.makeAllCSV = makeAllCSV;
    }

    /**
     * The inherited method which is called whenever the task starts
     *
     * @return
     * @throws java.io.IOException
     * @throws Exception
     */
    @Override
    protected Object call() {
        prepareFilesAndReadSubjects();
        //for each subject in the list
        alAllSubjects.stream().forEach((String[] sf) -> {
            
            response = null;
            alResponses = new ArrayList<>();
            // if a record is not empty
            if (sf.length > 1) {
                // another subject will be requested
                iNumOfRequestedSubjects += 1;
                // white spaces will be reduced
                sf[1] = sf[1].replaceAll(" ", "");
                //type of the request is defined
                for (String sType : sf[1].split(",")){
                    switch (getTypeOf(sType)) {
                    case "rc":
                    case "ic":
                        sf[1] = sType;
                        doSOAPRequest(getTypeOf(sType), sType);
                        break;
                    case "bd":
                    {
                        try {
                            doHTTPRequest(sf[0],sType);
                        } catch (URISyntaxException ex) {
                            writeException(sf[0], sType, "Dotazování dle data "
                                    + "narození se nezdařilo. Prosím, zkuste "
                                    + "znovu nebo vyhledejte ručně.");
                            updateMessage(sf[0]+";"+sType+";"+"Dotazování dle data "
                                    + "narození se nezdařilo. Prosím, zkuste "
                                    + "znovu nebo vyhledejte ručně.");
                            Logger.getLogger(ISIROperator.class.getName()).
                                    log(Level.SEVERE, null, ex);
                        }
                    }
                        break;
                    case "name":
                         try {
                            doHTTPRequest(sf[0],"");
                        } catch (URISyntaxException ex) {
                            writeException(sf[0], sType, "Dotazování dle jména "
                                    + "se nezdařilo. Prosím, zkuste "
                                    + "znovu nebo vyhledejte ručně.");
                            updateMessage(sf[0]+";"+sType+";"+"Dotazování dle jména "
                                    + "se nezdařilo. Prosím, zkuste "
                                    + "znovu nebo vyhledejte ručně.");
                            Logger.getLogger(ISIROperator.class.getName()).
                                    log(Level.SEVERE, null, ex);
                        }
                        break;
                    default:
                        writeException(sf[0], sType, "Prosím, zkontrolujte zadané "
                                + "parametry. Pravděpodobně jsou špatně zadány. "
                                + "Dotaz nebyl vykonán, prosím zkuste znovu nebo vyhledejte ručně.");
                        updateMessage(sf[0]+";"+sType+";"+"Prosím, zkontrolujte zadané "
                                + "parametry. Pravděpodobně jsou špatně zadány. "
                                + "Dotaz nebyl vykonán, prosím zkuste znovu nebo vyhledejte ručně");
                }
                }                
            }
            processSOAPResponses(alResponses, sf);
            //progress is updated for the progress bar
            updateProgress((iNumOfRequestedSubjects * 100) / iNumOfAllSubjects, 100);
        });
        closeFiles();
       
        updateMessage("Done");
        return "Done";
    }
    /**
     * Returns new file path for saving result according to the specified source file
     * @param sLoadPath
     * @return 
     */
    public static final String getSavePath(String sLoadPath) {
        return (new File(sLoadPath).getParent()) + "\\Found.csv";
    }

    public static final String getTypeOf(String s) {
        if (s.matches("xxx") || s.matches("")){
            return "name";
        }
        Pattern pIC = Pattern.compile("[0-9]{6,8}");
        Pattern pRC = Pattern.compile("[0-9]{10}");
        Pattern pBD = Pattern.compile("[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{4}");
        Matcher mField = pIC.matcher(s);
        
        if (mField.matches()) {
            return "ic";
        }
        mField = pRC.matcher(s);
        if (mField.matches()) {
            return "rc";
        }
        mField = pBD.matcher(s);
        if (mField.matches()) { 
            return "bd";
        }
        else {
            return "wrong parameter";
        }
            
    }

    public ArrayList<String[]> getAlAllFoundSubjects() {
        return alRecords;
    }

    private void prepareFilesAndReadSubjects() {
        try {
            //file where the found subjects from the isir database are written
            File f = new File(sPathToCreateCSV);
            cwSubjectsFound = new CSVWriter(new FileWriter(f), cSeparator);
            //control file for subjects not found in the isir database
            cwSubjectsNotFound = new CSVWriter(new FileWriter(new File(f.getParent() +
                                                    "\\NotFound.csv")), CSV_SEPARATOR);
            //read all the subjects to be requested in the ISIRCSVLoader via the OpenCSV library
            alAllSubjects = new ISIRCSVLoader(sPathToCSV).getAllSubjects();
            //number of subjects
            iNumOfAllSubjects = alAllSubjects.size();
        } catch (IOException ex) {
            
            updateMessage("Alert - write protection.");
            Logger.getLogger(ISIROperator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void doHTTPRequest(String name, String bd) throws URISyntaxException {
        String[] sf = {name, bd, ""};
        ISIRHttpRequestor ihtp = new ISIRHttpRequestor(name, bd);
        httpResponse = ihtp.getResponse();
        
        //vracet odkaz do tableview na konkrétní vyhledávání
        //System.out.println(ihtp.getUrl());
        if (httpResponse[0] == "2") {
            sf[2] = httpResponse[1];
            cwSubjectsFound.writeNext(sf);
            
            updateMessage(name + ";" + bd + ";" + sf[2]);
        } else if (httpResponse[0] == "1") {
            sf[2]="WS";
            cwSubjectsNotFound.writeNext(sf);
            
            updateMessage(name+";"+bd+";WS");
        } else {
            cwSubjectsFound.writeNext(sf);
            
            updateMessage(name + ";" + bd + ";Něco je špatně, zkontrolujte "
                    + "zadané údaje a připojení k internetu.");
        }
    }

    private void doSOAPRequest(String sType, String rc_ic) {
        response = new ISIRSOAP(sType, rc_ic).getResponse();
                        alResponses.add(response);
    }

    private void writeException(String sf0, String sf1, String text) {
        String s[] = {sf0,sf1,text};
        cwSubjectsFound.writeNext(s);
    }

    private void processSOAPResponses(ArrayList<SOAPMessage> alResponses, String[] sf) {
        //for each request more responses can be returned
                for (SOAPMessage respMessage: alResponses){
                    if (respMessage != null) {
                        try {
                            ArrayList<HashMap> alAllData = new ISIRXMLParser(respMessage).
                                    getAlAllData();
                            isirLines = new ISIRCSVLinesMaker(alAllData);
                            ArrayList<String[]> alLinesToCSV = isirLines.getAlCSVLines();
                            if (isirLines.isFound()) {
                                alLinesToCSV.stream().forEach((sfToCSV) -> {
                                    sfToCSV[0] = sf[0];
                                    sfToCSV[1] = sf[1];
                                    cwSubjectsFound.writeNext(sfToCSV);
                                    alRecords.add(sfToCSV);
                                    
                                    updateMessage(sf[0]+";"+sf[1]+";"+sfToCSV[2]);
                                });
                                
                            } else {
                                alLinesToCSV.stream().forEach((sfToCSV) -> {
                                    sfToCSV[0] = sf[0];
                                    sfToCSV[1] = sf[1];
                                    cwSubjectsNotFound.writeNext(sfToCSV);
                                    
                                    updateMessage(sf[0]+";"+sf[1]+";WS");
                                });
                            }
                            //new CSVWriter, put lines from lines maker
                        } catch (SOAPException | IOException | ParserConfigurationException | SAXException ex) {
                            String s[] = {sf[0], sf[1], "An exception occured when request executed. "
                                    + "Please try again or request the subject manually."};
                            cwSubjectsFound.writeNext(s);
                           
                            updateMessage(sf[0]+";"+sf[1]+";"+"ZKUSTE VYHLEDAT ZNOVU NEBO RUČNĚ!");
                        }
                    } else {
                        String s[] = {sf[0], sf[1], "Request wasn't executed. "
                                + "Please try again or request the subject manually."};
                        cwSubjectsFound.writeNext(s);
                        
                        updateMessage(sf[0]+";"+sf[1]+";"+"ZKUSTE VYHLEDAT ZNOVU NEBO RUČNĚ!");
                    }
                }
    }

    private void closeFiles() {
        try {
            cwSubjectsFound.close();
            cwSubjectsNotFound.close();
        } catch (IOException ex) {
            Logger.getLogger(ISIROperator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

}
