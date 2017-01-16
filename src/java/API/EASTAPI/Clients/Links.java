/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package API.EASTAPI.Clients;

/**
 *
 * @author Alfonso Panarello
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import API.EASTAPI.Clients.EastBrRESTClient;
import API.EASTAPI.Clients.NetworkSegment;
import static API.EASTAPI.Clients.NetworkSegment.LOGGER;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import utils.Exception.WSException;
import utils.Exception.WSException303;
import utils.Exception.WSException500;
/**
 *
 * @author dissennato
 */
public class Links extends EastBrRESTClient{
JSONObject body;
Response r;
    static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(NetworkSegment.class);

    public Links(String userName, String password) {
        super(userName, password);
    }
    
   
   public Response makeLink(JSONObject j,String baseBBURL)throws WSException{
   Response r;
   String fednetnorthBrnetwork="/fednet/northBr/network";
        String Path="/links";
        try {
            r=this.makeLinks(j, baseBBURL,  "/fednet/northBr/network" ,Path);
            //id = this.searchNetSegID(netseg, baseFEDSDNURL, fedId, siteId);
        } catch (Exception ex) {
            throw new WSException303("SEE_OTHER! The action can't be completed");
        }
       //Response r =this.makeSimpleRequest(baseFEDSDNURL+"/fednet/"++"/"+siteId+"/netsegment/"+id, "", "get");
        try{
                this.checkResponse(r);//as answer we expect a status code 200
            }
            catch(WSException wse){
                LOGGER.error("Exception occurred in createTenantFA method, the web service has answer with bad status!\n"+wse.getMessage());
                throw wse;
            }
   
   return r;
   
   }
    private Response makeLinks(org.json.simple.JSONObject j, String baseFEDSDNURL, String fednetnorthBrnetwork, String Path) throws WSException{
        
        body=j;
        Response r=this.makeSimpleRequest(baseFEDSDNURL+fednetnorthBrnetwork+Path, body.toString(), "post");
        
        try{
                this.checkResponse(r);//as answer we expect a status code 200
            }
            catch(WSException wse){
                System.out.println(r.readEntity(String.class));
                LOGGER.error("Exception occurred in createTenantFA method, the web service has answer with bad status!\n"+wse.getMessage());
                throw wse;
            }
        
        
        return r;
}
}
