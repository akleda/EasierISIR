/*
 * @Author Adéla Havlíčková
 * class ISIRHttpRequestor requests an ISIR subject search according to the specified name and birth day
 * it provides the response as name of the found subject and current state of the subject
 */
package easierisir.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Adelka
 */
class ISIRHttpRequestor {
    private String result;
    private URL url;
    
    public ISIRHttpRequestor(String name, String bd){
        name=name.trim();
        bd = bd.trim();
        result = "";
        try {
            
            String sName[] = name.split(" ");
            sName[0] = URLEncoder.encode(sName[0], "Windows-1250");
            
            if (sName.length == 2 ){ 
                
                sName[1] = URLEncoder.encode(sName[1], "Windows-1250");
                if(bd != ""){
                url = new URL("https://isir.justice.cz/isir/ueu/vysledek_lustrace.do?nazev_osoby="+sName[1]+"&jmeno_osoby="+sName[0]+"&ic=&datum_narozeni="+bd+"&rc=&mesto=&cislo_senatu=&bc_vec=&rocnik=&id_osoby_puvodce=&druh_stav_konkursu=&datum_stav_od=&datum_stav_do=&aktualnost=AKTUALNI_I_UKONCENA&druh_kod_udalost=&datum_akce_od=&datum_akce_do=&nazev_osoby_f=&cislo_senatu_vsns=&druh_vec_vsns=&bc_vec_vsns=&rocnik_vsns=&cislo_senatu_icm=&bc_vec_icm=&rocnik_icm=&rowsAtOnce=50&captcha_answer=&spis_znacky_datum=&spis_znacky_obdobi=14DNI");            
                }
                else {
                    url = new URL("https://isir.justice.cz/isir/ueu/vysledek_lustrace.do?nazev_osoby="+sName[1]+"&jmeno_osoby="+sName[0]+"&ic=&datum_narozeni=&rc=&mesto=&cislo_senatu=&bc_vec=&rocnik=&id_osoby_puvodce=&druh_stav_konkursu=&datum_stav_od=&datum_stav_do=&aktualnost=AKTUALNI_I_UKONCENA&druh_kod_udalost=&datum_akce_od=&datum_akce_do=&nazev_osoby_f=&cislo_senatu_vsns=&druh_vec_vsns=&bc_vec_vsns=&rocnik_vsns=&cislo_senatu_icm=&bc_vec_icm=&rocnik_icm=&rowsAtOnce=50&captcha_answer=&spis_znacky_datum=&spis_znacky_obdobi=14DNI");
                    }            
                
            } else if (sName.length == 1 && sName[0]!=""){
                url = new URL("https://isir.justice.cz/isir/ueu/vysledek_lustrace.do?nazev_osoby=&jmeno_osoby="+sName[0]+"&ic=&datum_narozeni="+bd+"&rc=&mesto=&cislo_senatu=&bc_vec=&rocnik=&id_osoby_puvodce=&druh_stav_konkursu=&datum_stav_od=&datum_stav_do=&aktualnost=AKTUALNI_I_UKONCENA&druh_kod_udalost=&datum_akce_od=&datum_akce_do=&nazev_osoby_f=&cislo_senatu_vsns=&druh_vec_vsns=&bc_vec_vsns=&rocnik_vsns=&cislo_senatu_icm=&bc_vec_icm=&rocnik_icm=&rowsAtOnce=50&captcha_answer=&spis_znacky_datum=&spis_znacky_obdobi=14DNI");            
            } else {
                url = new URL("https://isir.justice.cz/isir/ueu/vysledek_lustrace.do?nazev_osoby=&jmeno_osoby=&ic=&datum_narozeni="+bd+"&rc=&mesto=&cislo_senatu=&bc_vec=&rocnik=&id_osoby_puvodce=&druh_stav_konkursu=&datum_stav_od=&datum_stav_do=&aktualnost=AKTUALNI_I_UKONCENA&druh_kod_udalost=&datum_akce_od=&datum_akce_do=&nazev_osoby_f=&cislo_senatu_vsns=&druh_vec_vsns=&bc_vec_vsns=&rocnik_vsns=&cislo_senatu_icm=&bc_vec_icm=&rocnik_icm=&rowsAtOnce=50&captcha_answer=&spis_znacky_datum=&spis_znacky_obdobi=14DNI");
            }
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("POST");
            try (OutputStreamWriter out = new OutputStreamWriter(
                    httpCon.getOutputStream())) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(httpCon.getInputStream(),"Windows-1250"));
                String line="";
                int num=0;
                while ((line = rd.readLine()) != null) {
                    
                    if (line.trim().matches("Jm.no\\/n.zev:")) {   
                        for (int i = 0; i< 5; i++) rd.readLine();
                        if (num>0)result += ", ";
                        result += rd.readLine().trim();
                        
                        while (!line.contains("Stav řízení:"))
                            line=rd.readLine();
                        for (int j = 0; j< 3; j++) rd.readLine();
                        result += "("+rd.readLine().trim()+")";
                        num++;
                    } else if (line.trim().matches("<td align=\"left\">PO.ET NALEZEN.CH DLU.N.K.</td>")){
                        rd.readLine();
                        if (rd.readLine().trim().matches("<b>0</b>")){
                            result = "WS";
                            break;
                        }
                    }
                    
      }
      rd.close();
            }
        } catch (MalformedURLException ex) {
            result = "";
            //Logger.getLogger(ISIRHttpRequestor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            result = "";
            //Logger.getLogger(ISIRHttpRequestor.class.getName()).log(Level.SEVERE,null,ex);
        }
    }
    
    
    
    public String[] getResponse(){
      String param = "0"; //0 - vyjimka
      if (result == "WS"){ //WS - prohledano, nenalezeno
          param = "1";
      } else if (result.length()>1 && !result.matches("WS"))
          param = "2";  //nalezeno
    String s[] = {param,result};  
    return s;
    }

    /**
     * @return the url
     */
    public URL getUrl() {
        return url;
    }
}
