/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ah.easierisir_1.pkg1fxml;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Adelka
 */
public class Record {
    private SimpleStringProperty sspName = new SimpleStringProperty();
    private SimpleStringProperty sspIcrc = new SimpleStringProperty();
    private SimpleStringProperty sspFound = new SimpleStringProperty();
    
    public Record(){
        this("","","");
    }
    
    public Record(String sName, String sIcrc, String sFound){
        setName(sName);
        setIcrc(sIcrc);
        setFound(sFound);
    }

    /**
     * @return the sspName
     */
    public String getName() {
        return sspName.get();
    }

    /**
     * @param sspName the sspName to set
     */
    public void setName(String sName) {
        sspName.set(sName);
    }

    /**
     * @return the sspIcrc
     */
    public String getIcrc() {
        return sspIcrc.get();
    }

    /**
     * @param sspIC_RC the sspIcrc to set
     */
    public void setIcrc(String sIcrc) {
        sspIcrc.set(sIcrc);
    }

    /**
     * @return the sspFound
     */
    public String getFound() {
        return sspFound.get();
    }

    /**
     * @param sspFound the sspFound to set
     */
    public void setFound(String sFound) {
        sspFound.set(sFound);
    }
}
