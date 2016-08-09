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
        this(sPathToCSV,getSavePath(sPathToCSV), cSeparator, MAKE_ALL_CSV);
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
     * @return 
     * @throws Exception 
     */
    @Override    
    protected Object call() throws Exception {
            File f = new File(sPathToCreateCSV);
            cwSubjectsFound = new CSVWriter(new FileWriter(f), cSeparator);
            cwSubjectsNotFound = new CSVWriter(new FileWriter(new File(f.getParent() + "\\NotFound.csv")), cSeparator);
        try {
            alAllSubjects = new ISIRCSVLoader(sPathToCSV).getAllSubjects();
            iNumOfAllSubjects = alAllSubjects.size();
            alAllSubjects.stream().forEach((String[] sf) -> {
                SOAPMessage response = null;
                if (sf.length > 1) {
                    iNumOfRequestedSubjects += 1;
                    updateProgress((iNumOfRequestedSubjects*100)/iNumOfAllSubjects,100);
                    response = new ISIRSOAP(getTypeOf(sf[1]), sf[1]).getResponse();
                } if (response != null){
                try {
                    ArrayList<HashMap> alAllData = new ISIRXMLParser(response).getAlAllData();
                    isirLines = new ISIRCSVLinesMaker(alAllData);
                    ArrayList<String[]> alLinesToCSV = isirLines.getAlCSVLines();
                    if (isirLines.isFound()) {
                        alLinesToCSV.stream().forEach((sfToCSV) -> {
                            sfToCSV[0] = sf[0];
                            sfToCSV[1] = sf[1];
                            cwSubjectsFound.writeNext(sfToCSV);
                            alRecords.add(sfToCSV);
                        });
                        
                    } else {
                        alLinesToCSV.stream().forEach((sfToCSV) -> {
                            sfToCSV[0] = sf[0];
                            sfToCSV[1] = sf[1];
                            cwSubjectsNotFound.writeNext(sfToCSV);
                        });
                    }
                    //new CSVWriter, put lines from lines maker
                } catch (SOAPException ex) {
                    Logger.getLogger(ISIROperator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ISIROperator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(ISIROperator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(ISIROperator.class.getName()).log(Level.SEVERE, null, ex);
                }} else {
                    String s[] = {sf[0],sf[1],"Request wasn't executed. Please try again or request the subject manually."};
                    cwSubjectsFound.writeNext(s);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(ISIROperator.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            cwSubjectsFound.close();
            cwSubjectsNotFound.close();
        } catch (IOException ex) {
            Logger.getLogger(ISIROperator.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateMessage("Done");
        return "Done";
    }
    
    public static final String getSavePath(String sLoadPath){
        return (new File(sLoadPath).getParent())+"\\Found.csv";
    }
    
     public static final String getTypeOf(String s) {
        Pattern pIC = Pattern.compile("[0-9]{6,8}");
        Pattern pRC = Pattern.compile("[0-9]{10}");
        Matcher mField = pIC.matcher(s);
        if (mField.matches()) return "ic";
        mField = pRC.matcher(s);
        if (mField.matches()) return "rc";
        return "wrong parameter";
    }
     
     public ArrayList<String[]> getAlAllFoundSubjects(){
         return alRecords;
     }
}
