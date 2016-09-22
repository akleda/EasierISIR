/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easierisir.base;

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
public class ISIRHttpRequestorTest {
    
    public ISIRHttpRequestorTest() {
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
     * Test of getResponse method, of class ISIRHttpRequestor.
     */
    @Test
    public void testGetResponse() {
        System.out.println("getResponse - Naděžda Vyhlídalová, 8.9.1965");
        ISIRHttpRequestor instance = new ISIRHttpRequestor("Naděžda Vyhlídalová", "8.9.1965");
        String[] expResult = {"2","Naděžda Vyhlídalová"};
        String[] result = instance.getResponse();
        System.out.println(result[0]+";"+result[1]);
        assertArrayEquals(expResult, result);
        System.out.println("getResponse1 - Naděžda Skálová, 05.04.1952");
        instance = new ISIRHttpRequestor("Naděžda Skálová", "05.04.1962");
        expResult[0]="1";
        expResult[1]="WS";
        result = instance.getResponse();
        System.out.println(result[0]+";"+result[1]);
        assertArrayEquals(expResult, result);
        System.out.println("getResponse2 - 5.4.1962");
        instance = new ISIRHttpRequestor("", "5.4.1962");
        expResult[0]="2";
        expResult[1]="Helena Hronová, Jana Tomandlová, Milan Sailer, Petr David, Miroslav Holub, Marie Stehlíčková, Josef Těhan";
        result = instance.getResponse();
        System.out.println(result[0]+";"+result[1]);
        assertArrayEquals(expResult, result); 
        System.out.println("getResponse3 - , 18.2.1981");
        instance = new ISIRHttpRequestor("", "18.2.1981");
        expResult[0]="2";
        expResult[1]="Lucie Hrbáčová, Veronika Stanislavová, Andrea Morongová, Jana Halbichová, Kateřina Brabcová, Miroslava Bílá Kubíčková, Marcela Opluštilová, Lenka Nováková, Lukáš Opluštil, Marcela Opluštilová, Kamil Huňař, Marcela Huňařová, Patrik Šimko, Jana Zikmundová, Tomáš Zikmunda, Lukáš Puc, Lenka Pucová, Jana Zikmundová, Tomáš Zikmunda";
        result = instance.getResponse();
        System.out.println(result[0]+";"+result[1]);
        assertArrayEquals(expResult, result); 
        System.out.println("getResponse4 - ");
        instance = new ISIRHttpRequestor("", "");
        expResult[0]="0";
        expResult[1]="";
        result = instance.getResponse();
        System.out.println(result[0]+";"+result[1]);
        assertArrayEquals(expResult, result); 
        
    }
    
}
