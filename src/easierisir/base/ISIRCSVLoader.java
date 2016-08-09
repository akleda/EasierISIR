/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easierisir.base;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISIRCSVLoader uses OpenCSV jar to load CSV file in format:
 * nazev; ič/rč
 * into a list of arrays with strings.
 * The file to load should be saved in UTF-8 encoding.
 * @author Adelka
 */
public class ISIRCSVLoader {
    private CSVReader csvReader;
    private List<String[]> alsAllSubjects;
    private static final char DEFAULT_SEPARATOR = ';';
    
    /**
     * Constuctor with one parameter 
     * @param sPath - The path to the CSV file to be loaded, the 
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public ISIRCSVLoader(String sPath) throws FileNotFoundException, IOException{
        this(sPath, DEFAULT_SEPARATOR);
    }
    
    /**
     * Constructor with two parameters
     * @param sPath - The path to the CSV file to be loaded, the 
     * @param cSeparator - Allows to set field separator in the loaded CSV file
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public ISIRCSVLoader(String sPath, char cSeparator) throws FileNotFoundException, IOException{
        this.csvReader = new CSVReader(new FileReader(sPath),cSeparator);
        alsAllSubjects = csvReader.readAll();
    }
    
    /**
     * 
     * @return Returns the List of String objects - nazev and rc/ic to be requested
     */
    public List<String[]> getAllSubjects(){
        return alsAllSubjects;
    }
    
    /**
     * Method to find out if the type of the string parameter is IC or RC
     * @param s String to be checked
     * @return 
     */
   
}
