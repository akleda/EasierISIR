/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easierisir.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Adelka
 */
public class ISIRCSVLoaderTest {
    
    public ISIRCSVLoaderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getAllSubjects method, of class ISIRCSVLoader.
     */
    @Test
    public void testGetAllSubjects() {
        System.out.println("getAllSubjects");
        ISIRCSVLoader instance = null;
        List result = null;
        try {
            instance = new ISIRCSVLoader("e:\\InsolvenceTest\\CSVKdohledáníViceKriterii\\MalyTestUTF8.csv");
        } catch (IOException ex) {
            Logger.getLogger(ISIRCSVLoaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        int expResult = 27;
        if (instance != null) {
           result  = instance.getAllSubjects();
        }
        assertEquals(expResult, ((ArrayList)result).size());
        
    }
    
}
