/**Copyright 2016, University of Messina.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
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
 * REST Web Service
 *
 * @author gtricomi, apanarello
 * 
 */
@Path("/fednet/eastBr/FA_Management")
public class LinksResource {

    @Context
    private UriInfo context;
    static final Logger LOGGER = Logger.getLogger(LinksResource.class);
    /**
     * Creates a new instance of LinksResource
     */
    public LinksResource() {
    }

    /**
     * Retrieves representation of an instance of EASTTAPI.LinksResource
     * @return an instance of java.lang.String
     */
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public String LinkFunctions(String content) {
        
        JSONObject reply=new JSONObject();
        JSONParser parser= new JSONParser();
        JSONObject input=null;
        LinkInfoContainers lic=new LinkInfoContainers();
        String baseBBURL=""; /////* Ricavare url BB*//////////    
        try 
        {
            input=(JSONObject) parser.parse(content);
            lic.setType((String)input.get("type"));
            lic.setToken((String) input.get("token"));//utilizzerò questo elemento per identificare federation tenant
            lic.setCommand((String) input.get("Command"));
            lic.setFa_endpoints(((JSONArray) input.get("fa_endpoints")));
            lic.setNetwork_tables(((JSONArray) input.get("network_table")));//not used for this moment the tables are recalculated
        } catch (ParseException pe) {
            reply.put("returncode", 1);
            reply.put("errormesg", "INPUT_JSON_UNPARSABLE: OPERATION ABORTED");
            return reply.toJSONString();
        }

        try {
            DBMongo m = new DBMongo();
            String federationUser = m.getTenantName("token", lic.getToken());
            Integer id = m.getfedsdnFednetID(federationUser);
            String result="";
            ArrayList<JSONObject> netTables;
            ArrayList<JSONObject> fa_endPoints;
/* //>>>BEACON: CREARE SERVIZIO SUL BEACON BROKER PER INVOCARE QUESTA FUNZIONALITÀ
            OrchestrationManager om = new OrchestrationManager();
            String result = om.makeLink(id.longValue(), federationUser, null, m);// null will be substituted with an ArrayList<JSONObject> netTables that correspond at lic.getNetwork_tables()
               
*/         
            netTables= lic.getNetwork_tables();
            JSONObject job = new JSONObject();
            JSONObject job_table = new JSONObject();
           // JSONObject j_map = new JSONObject(params);
           JSONArray jArr =new JSONArray();
            Iterator itr = netTables.iterator();
            while (itr.hasNext()) {
                
                Object element = itr.next().toString();
                jArr.add(element);
                
                //System.out.print(element + " ");
            }
            job.put("network_tables",jArr);
            //fa_endPoints =lic.getFa_endpoints();
            //HashMap params = new HashMap();
            
            
            job.put("fedId", id);
            job.put("user", federationUser);
            /*
            job_in.put("type",lic.getType());
            
            job_in.put("token",lic.getToken());
            
            job_in.put("command",lic.getCommand());*/
            
            //job_in.put("arrayPoint", fa_endPoints);
            
            //job.put("network_tables", job_table);
            
            
           // job.put("infoLink", job_in);
           
            //job.put("mongoDb", m.toString());
            Links ln =new Links("","");
            Response r;
            r =ln.makeLink(job,baseBBURL);
            if (!result.equals("ok")) {
                reply.put("returncode", 1);
                reply.put("errormesg", "Generic Exception: OPERATION ABORTED");
                return reply.toJSONString();
            }
            org.json.simple.parser.JSONParser p = new org.json.simple.parser.JSONParser();
                Object obj = null;
                try {
                    obj = p.parse(r.readEntity(String.class));
                } catch (ParseException ex) {
                    LOGGER.error("Exception occurred in Parsing JSON returned from FEDSDN \n" + ex.getMessage());
                }
            //operation needed to complete link requests!
            ////LA FUNZIONE DELL'ORCHESTRATOR DOVRA': ritrovare la lista di tutte le cloud in federazione per il tenant
            ////Per ogni Cloud:
            //////>>richiamare funzione che richiede network table da neutron
            //////[questo perchè il flow prevede che sia inviata la network table al FEDSDN attraverso una chiamata PUT /fednet/ID_FEDNET con action=link
            //////(probabilemente queste informazioni verranno poi restituite in formato non corretto per
            ////// il FA quindi dovranno essere rielaborate prima di rimandarle al FA
            //////)]
            //////>>a questo punto il FEDSDN attraverso l'adapter invoca questo WebService
        } catch (Exception eg) {
            reply.put("returncode", 1);
            reply.put("errormesg", "Generic Exception: OPERATION ABORTED");
            return reply.toJSONString();
        }
        reply.put("returncode", 0);
        reply.put("errormesg", "None");
        return reply.toJSONString();
    }

    /**
     * Sub-resource locator method for {name}
     */
    @Path("{name}")
    public Link getLink(@PathParam("name") String name) {
        return Link.getInstance(name);
    }
    
    
    /**
     * @param token
     * @param site
     * @author caromeo 
     * Builds the tenant table for the BNAs
     */
    
    public void createTenantTab(String token, String site){
        DBMongo m = new DBMongo();
        m.init("/home/carmelo/BeaconProject/newBBP/BB/web/WEB-INF/configuration_bigDataPlugin.xml");
        m.connectLocale("10.9.240.1");
        
        JSONObject tenantTables = new JSONObject();
        
        
        //use ManagementDB
        //db.Federation_Credential.find()
        //OUT: {"federationTenant" : "review", "token" : "3efb8c19-92a9-43bc-75d2-e4ff6f53cd2a", "password" : "reviewPass" }
        String tenant_name = m.getTenantName("token", token);
        
        
        //use review
        //db.TenantTables.find()
        //OUT:
        //"referenceSite" : "CETIC", "version" : 114, "entryTenantTab" : { "tenant_id" : "3029a98f60c24ac1b4ef4636c4ee3006", "name" : "review" } }
        //"referenceSite" : "UME", "version" : 114, "entryTenantTab" : { "tenant_id" : "3029a98f60c24ac1b4ef4636c4ee3006", "name" : "review" } }
        String result = m.getTenantTablesFromFedTenant("review", tenant_name, site);
        System.out.println("RES: "+result);
    }
    
    
    
    /**
     * @author apanarello 
     * MongoDB retrieve info
     */
    
    public void createNetSegTab(String token,ArrayList<String> site) {
        String federation_nets="";
        String tenant="";
        String fedNet=null;
        String version="";
        DBMongo db= new DBMongo();
        JSONObject fedNetsObj=null;
        JSONArray fedNetsArray=null;
        JSONParser fedNetsObjParser =new JSONParser();
        ArrayList<String> netsegs = new ArrayList();
        JSONObject netTables = new JSONObject();
        JSONParser jparser = new JSONParser();
                JSONArray netSegs=null;

        db.init("/home/apanarello/BeaconProject/newBBP/BB/web/WEB-INF/configuration_bigDataPlugin.xml");

        db.connectLocale("10.9.240.1");
        // TODO code application logic here
        tenant = db.getTenantDBName("token", token);
        for (String refSite : site) {
            
            
            try {
                federation_nets=db.retrieveFedNet(tenant, refSite);
                try {
                    fedNetsObj=(JSONObject) fedNetsObjParser.parse(federation_nets);
                    fedNetsArray= (JSONArray) fedNetsObj.get("fednets");
                    version =(String) fedNetsObj.get("version");
                    Iterator <String> it = fedNetsArray.iterator();
                    //Iterator<DBObject> it = cursor.iterator();
        //ArrayList<String> net = new ArrayList();
                    while (it.hasNext()) {
                        fedNet = it.next();
                        netsegs=db.retrieveBNANetSegFromFednet(tenant, refSite, version, fedNet );
                        Iterator <String> ite = netsegs.iterator();
                        while (it.hasNext()) {
                            
                           netSegs.add(ite.next());
                        /*MODIFHCE PARZIALI DA COMMENTAREEEEE - - - - - - - - - - - -- - - - - - - - - - - - -*/
                        }
                    }
                    netTables.put("version",version );
                    netTables.put("table", netSegs);
                    
                } catch (ParseException ex) {
                   LOGGER.error("Impossible parse from string to JSON object");
                }
            } catch (MDBIException ex) {
                LOGGER.error("Impossible to retrieve fedNet arreys from fednetsinSite collection");
            }
            
            
            
            
        }
        /*
        
        DBCollection collezione = db.getCollection(tenant, "provaa");
        System.out.println("DOPO COLLEZIONE"+collezione.toString());
        if(collezione==null){
            DBObject options;
            collezione=dataBase.createCollection("provaa",null);
           
    
    }
        else{
                
    }
       */ 
        
    }
}
