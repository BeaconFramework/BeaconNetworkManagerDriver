/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package API.EASTAPI.Clients;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Exception.WSException;

/**
 *
 * @author apanarello
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JSONParser parser= new JSONParser();
        JSONObject tables=null;
        JSONObject epoint=null;
        String endpoint = "[http://10.9.1.169:4567, http://10.9.1.159:4567]";
        String content="{\"table\": {\"a3848805-c0f9-4123-a478-001719644ea6\":[{\"tenant_id\": \"ba63f0acf9424d4b93c541e9df0d9392\", \"site_name\": \"site1\", \"name\": \"public\", \"vnid\": \"9c41da58-267e-4e92-9c0d-4a45f77d2053\"}, \n"
                + "{\"tenant_id\": \"447ae0a88049421e98b9ec83dc2c8540\", \"site_name\": \"site2\", \"name\": \"public\", \"vnid\": \"a3848805-c0f9-4123-a478-001719644ea6\"}]}";
                    
       
        try {
            tables=(JSONObject) parser.parse(content);
            epoint=(JSONObject) parser.parse(endpoint);
        } catch (ParseException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        try {
            JSONObject js= new JSONObject();
            String user = "root", pass = "fedsdn" ,
                    
            /*VERIFICARE USER AND PASS*/
              
            baseBBURL=baseBBURL="http://localhost:8084/BeaconBroker/";
            String type = "FullMesh",tok = "tokentemp";
            Links link= new Links(user,pass); /*  ??  */
            js.put("type", type);
            js.put("token", tok);
            js.put("fa_endpoints", epoint); /*  FA ???  */
            js.put("network_tables",tables);
            
            
            link.makeLink(js, baseBBURL);
            // TODO code application logic here
        } catch (WSException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
