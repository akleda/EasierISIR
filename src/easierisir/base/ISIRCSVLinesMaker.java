/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easierisir.base;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Constructor which creates lines to be written into a csv from the parsed response 
 * @author Adelka
 */
public class ISIRCSVLinesMaker {
    private final ArrayList<String[]> alCSVLines;
    private boolean bFound=false;
    
    /**
     * Constructor with parameter of parsed response
     * @param alAllData 
     */
    public ISIRCSVLinesMaker(ArrayList<HashMap> alAllData){
        this.alCSVLines = new ArrayList<>();
        getFromResponse(alAllData);
    }

    /**
     * Private method wich creates the lines accordingly to the keys in the provided hash map data
     * @param alAllData 
     */
    private void getFromResponse(ArrayList<HashMap> alAllData) {
        alAllData.stream().forEach((h) -> {
            if (h.containsKey("stav") && h.containsKey("textChyby")){
                String[] sf = {"","",(String)h.get("kodChyby")};
                alCSVLines.add(sf);
                bFound = false;
            } else
                if (h.containsKey("data")){
                    String[] sf = {"","",(String)h.get("druhStavKonkursu"), (String)h.get("bcVec")};
                    alCSVLines.add(sf);
                    bFound = true;
                } else
                {
                    String[] sf = {"","","Dotaz se nezdařil, prosím zkuste vyhledat ručně."};
                    getAlCSVLines().add(sf);
                    bFound = false;
                }
        });
            
                
    }

    /**
     * @return the alCSVLines REturns all the csv lines for the requested subject
     */
    public ArrayList<String[]> getAlCSVLines() {
        return alCSVLines;
    }

    /**
     * Method which indicates if there was found the requested subject by the web service or if not
     * @return the bFound
     */
    public boolean isFound() {
        return bFound;
    }
    
}
