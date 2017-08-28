/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package API.EASTAPI;

import API.EASTAPI.Clients.EastBrRESTClient;
import API.EASTAPI.Clients.Links;
//import static API.EASTAPI.NetworksegmentResource.LOGGER;
import API.EASTAPI.utils_containers.LinkInfoContainers;
//import OSFFM_ORC.OrchestrationManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import MDBInt.DBMongo;
import MDBInt.MDBIException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import javax.ws.rs.core.Response;
/**
 *
 * @author caromeo
 */
public class meloSiteTenantTablesTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
      ArrayList<String> a = new ArrayList();
      a.add("UME");
      a.add("CETIC");
      String token="3efb8c19-92a9-43bc-75d2-e4ff6f53cd2a"; 
      LinksResource ln =new LinksResource();
      //ln.createNetSegTab(token,a);
      //ln.createSiteTab(token, a);
      ln.createTenantTab(token, a);
      
      
      
        
    }
    
}
