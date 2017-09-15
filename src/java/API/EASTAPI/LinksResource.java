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
import java.util.Map.Entry;
import java.util.Set;
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
        JSONObject inputSiteList=null;
        String token="";
        String fednetid=null;
        HashMap SiteTables = new HashMap();
        HashMap TenantTables = new HashMap();
        HashMap MapSegTables = new HashMap();
        
        ArrayList <String> arraySites = null;
        try 
        {
            input=(JSONObject) parser.parse(content);
            lic.setType((String)input.get("type"));
            lic.setToken((String) input.get("token"));//utilizzerò questo elemento per identificare federation tenant
            lic.setCommand((String) input.get("Command"));
            lic.setFa_endpoints(((JSONArray) input.get("fa_endpoints")));
            lic.setNetwork_tables(((JSONArray) input.get("network_table")));//not used for this moment the tables are recalculated
            if(input.containsKey("fednetID"))
                fednetid=(String) input.get("fednetID");
            else
                fednetid=null;
        } catch (ParseException pe) {
            reply.put("returncode", 1);
            reply.put("errormesg", "INPUT_JSON_UNPARSABLE: OPERATION ABORTED");
            return reply.toJSONString();
        }

        try {
            DBMongo m = new DBMongo();
            String federationUser = m.getTenantName("token", lic.getToken());
            ArrayList<JSONObject> netTables;
            ArrayList<JSONObject> fa_endPoints;
            if (fednetid == null) {
                ArrayList<Integer> ids = m.getfedsdnFednetIDs(federationUser);
                Iterator it = ids.iterator();
                while (it.hasNext()) {
                    Integer tmp = ((Integer) it.next());
                    /* //>>>BEACON: CREARE SERVIZIO SUL BEACON BROKER PER INVOCARE QUESTA FUNZIONALITÀ
                OrchestrationManager om = new OrchestrationManager();
                String result = om.makeLink(id.longValue(), federationUser, null, m);// null will be substituted with an ArrayList<JSONObject> netTables that correspond at lic.getNetwork_tables()

                     */
                }
                String result = "";

            } else {
                Integer tmp = new Integer(fednetid);
                /* //>>>BEACON: CREARE SERVIZIO SUL BEACON BROKER PER INVOCARE QUESTA FUNZIONALITÀ
                OrchestrationManager om = new OrchestrationManager();
                String result = om.makeLink(id.longValue(), federationUser, null, m);// null will be substituted with an ArrayList<JSONObject> netTables that correspond at lic.getNetwork_tables()

                 */
            }
            netTables = lic.getNetwork_tables();
            JSONObject job = new JSONObject();
            JSONObject job_table = new JSONObject();
            // JSONObject j_map = new JSONObject(params);
            JSONArray jArr = new JSONArray();
            Iterator itr = netTables.iterator();
            while (itr.hasNext()) {

                Object element = itr.next().toString();
                jArr.add(element);

                //System.out.print(element + " ");
            }
            job.put("network_tables", jArr);
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
            Links ln = new Links("", "");
            Response r;
            r = ln.makeLink(job, baseBBURL);
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
            ////////////////////////////////////////////////////////////ALFO
            ////LA FUNZIONE DELL'ORCHESTRATOR DOVRA': ritrovare la lista di tutte le cloud in federazione per il tenant

            JSONArray sites = null;
            sites = (JSONArray) inputSiteList.get("site"); // bisogna valutare e verificare come sono strutturate le cloud nel json
            //suppongo nel JSON OBJECT CI SIA UN ARRAY DI SITI
            Iterator<String> it = sites.iterator();
            while (it.hasNext()) {
                arraySites.add(it.next()); //Creo l'array list per inviarla al metodo di creazione tabelle che restitiesce hashMapTable per ogni sito
            }
            MapSegTables = this.createNetSegTab(token, arraySites); //net segement tables

            //CARMELO 28/07/17
            //this.createSiteTab //creare il metodo per recuperare le info a partire da lista di siti (arraySites) e token. deve restituire un hashtable
            //iterare per i siti (vedi sotto) per inserire nella tab (come JSONObject)
            SiteTables = this.createSiteTab(token, arraySites); //site tables
            TenantTables = this.createTenantTab(token, arraySites); //site tables

            //per ogni sito invoco il web service e invio le tabelle
            Set setEntry = MapSegTables.keySet();
            Iterator setIte = setEntry.iterator(); //siti nella hashtable

            Set setEntrySite = SiteTables.keySet();
            Iterator setIte2 = setEntrySite.iterator(); //siti nella hashtable

            Set setEntryTenant = TenantTables.keySet();
            Iterator setIte3 = setEntryTenant.iterator(); //siti nella hashtable 

            for (String site_ : arraySites) {  //siti nella lista di input

                //FEDNET
                while (setIte.hasNext()) {
                    String extrectedSite = setIte.next().toString();
                    if (extrectedSite.equals(site_)) { //controllo se il sito nella lista è presente nella hashtable ottenuta , se si estra la tabella per quel sito
                        JSONObject tab = (JSONObject) MapSegTables.get(extrectedSite); //tabella segment estratta dal sito
                        //inviare tabella a "tab" al sito corrispondente invocando webservice
                    } else {
                        System.out.println("Sito " + site_ + "non presente in HashMaps");
                        LOGGER.error("Sito " + site_ + "non presente in HashMaps");

                        //sito non presnete nell'hashTable restituita
                    }
                }

                //SITI
                while (setIte2.hasNext()) {
                    String extrectedSite = setIte2.next().toString();
                    if (extrectedSite.equals(site_)) { //controllo se il sito nella lista è presente nella hashtable ottenuta , se si estra la tabella per quel sito
                        JSONObject tab2 = (JSONObject) SiteTables.get(extrectedSite); //tabella segment estratta dal sito
                        //inviare tabella a "tab" al sito corrispondente invocando webservice
                    } else {
                        System.out.println("Sito " + site_ + "non presente in HashMaps");
                        LOGGER.error("Sito " + site_ + "non presente in HashMaps");

                        //sito non presnete nell'hashTable restituita
                    }
                }

                //TENANT
                while (setIte3.hasNext()) {
                    String extrectedSite = setIte3.next().toString();
                    if (extrectedSite.equals(site_)) { //controllo se il sito nella lista è presente nella hashtable ottenuta , se si estra la tabella per quel sito
                        JSONObject tab3 = (JSONObject) TenantTables.get(extrectedSite); //tabella segment estratta dal sito
                        //inviare tabella a "tab" al sito corrispondente invocando webservice
                    } else {
                        System.out.println("Sito " + site_ + "non presente in HashMaps");
                        LOGGER.error("Sito " + site_ + "non presente in HashMaps");

                        //sito non presnete nell'hashTable restituita
                    }
                }
                //send 

            }

            ///////////////////////////////////////////////////////////ALFO
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
     * TESTING functions
     * @param site
     * @author caromeo 
     * Builds the temporary table for the BNAs with the last updated information
     */
    
    public void createFednetsInSiteTab(String site){
        DBMongo m = new DBMongo();
        m.init("/home/carmelo/BeaconProject/newBBP/BB/web/WEB-INF/configuration_bigDataPlugin.xml");
        m.connectLocale("10.9.240.1");

        String result = m.getFednetsInSiteTablesFromFedTenant("review", site, "fednets", null);
        System.out.println("RES: "+result);
    }    
    
    /**
     * TESTING functions
     * @param site
     * @param version
     * @author caromeo 
     * Builds the temporary table for the BNAs with a selected version of the information
     */
    
    public void createFednetsInSiteTabVersion(String site, Integer version){
        DBMongo m = new DBMongo();
        m.init("/home/carmelo/BeaconProject/newBBP/BB/web/WEB-INF/configuration_bigDataPlugin.xml");
        m.connectLocale("10.9.240.1");

        String result = m.getFednetsInSiteTablesFromFedTenant("review", site, "fednets", version);
        System.out.println("RES: "+result);
    }
    
    
    /**
     * TESTING functions
     * @param token
     * @param site
     * @author caromeo 
     * Builds the tenant table for the BNAs with the last updated information
     */
    
    public void createTenantTab(String token, String site){
        DBMongo m = new DBMongo();
        m.init("/home/carmelo/BeaconProject/newBBP/BB/web/WEB-INF/configuration_bigDataPlugin.xml");
        m.connectLocale("10.9.240.1");

        String tenant_name = m.getTenantName("token", token);
        String result = m.getTenantTablesFromFedTenant("review", tenant_name, site, "entryTenantTab", null);
        System.out.println("RES: "+result);
    }
    
        /**
     * TESTING functions
     * @param token
     * @param site
     * @param version
     * @author caromeo 
     * Builds the tenant table for the BNAs with a selected version of the information
     */
    
    public void createTenantTabVersion(String token, String site, Integer version){
        DBMongo m = new DBMongo();
        m.init("/home/carmelo/BeaconProject/newBBP/BB/web/WEB-INF/configuration_bigDataPlugin.xml");
        m.connectLocale("10.9.240.1");

        String tenant_name = m.getTenantName("token", token);
        String result = m.getTenantTablesFromFedTenant("review", tenant_name, site, "entryTenantTab", version);
        System.out.println("RES: "+result);
    }
    
    
    /**
     * TESTING functions
     * @param site
     * @author caromeo 
     * Builds the site table for the BNAs with the last updated information
     */
    
    public void createSiteTab(String site){
        DBMongo m = new DBMongo();
        m.init("/home/carmelo/BeaconProject/newBBP/BB/web/WEB-INF/configuration_bigDataPlugin.xml");
        m.connectLocale("10.9.240.1");

        String result = m.getSiteTablesFromFedTenant("review", site, "entrySiteTab", null);
        System.out.println("RES: "+result);
    }    
    
    /**
     * TESTING functions
     * @param site
     * @param version
     * @author caromeo 
     * Builds the site table for the BNAs with a selected version of the information 
     */
    
    public void createSiteTabVersion(String site, Integer version){
        DBMongo m = new DBMongo();
        m.init("/home/carmelo/BeaconProject/newBBP/BB/web/WEB-INF/configuration_bigDataPlugin.xml");
        m.connectLocale("10.9.240.1");

        String result = m.getSiteTablesFromFedTenant("review", site, "entrySiteTab", version);
        System.out.println("RES: "+result);
    } 
    
    
    /**
     * param token
     * param Arraylist site
     * @author apanarello 
     * LinksResource create netsegment tble
     */
    
    public HashMap createNetSegTab(String token, ArrayList<String> site) {
        String federation_nets = "";
        String tenant = "";
        String fedNet = null;
        Integer version = null;
        DBMongo db = new DBMongo();
        JSONObject fedNetsObj = null;
        JSONArray fedNetsArray = null;
        JSONParser fedNetsObjParser = new JSONParser();
        ArrayList<String> netsegs = new ArrayList();
        JSONObject netTables = new JSONObject();
        JSONParser jparser = new JSONParser();
        JSONArray netSegs = new JSONArray();
        JSONArray netTable = new JSONArray();
        HashMap site_seg = new HashMap();

        db.init("/home/apanarello/BeaconProject/newBBP/BB/web/WEB-INF/configuration_bigDataPlugin.xml");

        db.connectLocale("10.9.240.1");
        // TODO code application logic here
        tenant = db.getTenantDBName("token", token);
        for (String refSite : site) {
            netTable.clear();
            netTables.clear();
            netSegs.clear();// per ogni sito nella lista dei siti
            System.out.println("bbb"+refSite);
            try {
                federation_nets = db.retrieveFedNet(tenant, refSite); // lista delle fednet
                if (federation_nets == null) {
                    LOGGER.error("the selected SITE " + "-" + refSite + "-" + " is not present in the DB " + tenant + " I'm going to go ahead with the next site");
                    continue;
                }
                try {
                    fedNetsObj = (JSONObject) fedNetsObjParser.parse(federation_nets);
                    fedNetsArray = (JSONArray) fedNetsObj.get("fednets"); //array fednet ((PR, PU , QU)
                    version = ((Double) fedNetsObj.get("version")).intValue(); //Current version
                    Iterator<String> it = fedNetsArray.iterator();
                    //Iterator<DBObject> it = cursor.iterator();
                    //ArrayList<String> net = new ArrayList();
                    while (it.hasNext()) { //per ogni fednet nell'array
                        netSegs.clear();
                        fedNet = it.next();
                        netsegs = db.retrieveBNANetSegFromFednet(tenant, refSite, version, fedNet); //ottiene lista netsegment relativi a sito fednet e version
                        //System.out.println("NETSEGS " +netsegs.toString());
                        if (netsegs == null) {
                            continue; // DA VERIFICARE CORRETTEZZA LOGICA
                        }
                        Iterator<String> ite = netsegs.iterator();
                        while (ite.hasNext()) { //per ogni net segment
                            String next=ite.next();
                            //System.out.println("ite :-" +next);
                            
                            netSegs.add(next); //crea una JSON ARRAY DI NETSEGMENTs PER IL LA FEDNET "it"
                            
//MODIFHCE PARZIALI DA COMMENTAREEEEE - - - - - - - - - - - -- - - - - - - - - - - - -
                        }
                           netTable.add(netSegs.clone());
                           //System.out.println("netTable " +netTable.toString());
                           netTables.put("table", netTable.clone());
                           
                        //crea un array di netSegment arrays per 
                    }

                } catch (ParseException ex) {
                    LOGGER.error("Impossible parse from string to JSON object");
                }
            } catch (MDBIException ex) {
                LOGGER.error("Impossible to retrieve fedNet arreys from fednetsinSite collection");
            }
            //if ((netsegs == null) && netSegs.isEmpty())
            if (netSegs.isEmpty())
            {
                continue;
                //System.out.println("aaaa");
            }
            
            //netTable.add(netSegs);
            netTables.put("version", version);
            //netTables.put("table", netTable);
            site_seg.put(refSite, netTables.clone());
            System.out.println("contenuto hashMap per sito "+refSite+" "+site_seg.get(refSite).toString());
            //System.out.println(netTables.toString());
            

            //*/
        }
        return site_seg;
    }
    
    
    /**
     * @param token
     * @param site
     * @author caromeo
     * Create SiteTab
     */
    
    public HashMap createSiteTab(String token, ArrayList<String> site) {
        String federation_nets = "";
        String tenant = "";
        String site_table = "";

        Integer version = null;
        
        JSONObject fedNetsObj = new JSONObject();
        JSONParser fedNetsObjParser = new JSONParser();
        JSONParser sitesObjParser = new JSONParser();
        JSONObject sitesObj = new JSONObject();
        JSONArray site_array = new JSONArray();
        
        
        DBMongo db = new DBMongo();
        HashMap site_hm = new HashMap();
        
        db.init("/home/carmelo/NetBeansProjects/BB/web/WEB-INF/configuration_bigDataPlugin.xml");
        db.connectLocale("10.9.240.1");
        tenant = db.getTenantDBName("token", token);

        for (String refSite : site) {
            System.out.println("REFSITE: "+refSite);
            sitesObj.clear();
            
            try {
                federation_nets = db.retrieveFedNet(tenant, refSite); // lista delle fednet
                if (federation_nets == null) {
                    LOGGER.error("the selected SITE " + "-" + refSite + "-" + " is not present in the DB " + tenant + " I'm going to go ahead with the next site");
                    continue;
                }
                
                try {
                    fedNetsObj = (JSONObject) fedNetsObjParser.parse(federation_nets);
                } catch (ParseException ex) {
                    LOGGER.error("Error while parsing Fednets "+ ex.getMessage());
                }
                version = ((Double) fedNetsObj.get("version")).intValue(); //Current version

            
                site_table = db.getSiteTables(tenant, refSite, version);
                if (site_table == null) {
                    LOGGER.error("the selected SITE " + "-" + refSite + "-" + " is not present in the collection siteTables.");
                    continue;
                }
                try {
                    sitesObj = (JSONObject) sitesObjParser.parse(site_table);
                    site_array.add(sitesObj.clone());

                } catch (ParseException ex) {
                    LOGGER.error("Error while parsing Sites "+ ex.getMessage());
                }
            } catch (MDBIException ex) {
                LOGGER.error("Error while contacting Database "+ ex.getMessage());
            }
        }
        for (String refSite : site) {
            site_hm.put(refSite, site_array.clone());
            System.out.println("contenuto hashMap per sito "+refSite+" "+site_hm.get(refSite).toString());
        }
        System.out.println("SITE: "+ site_hm.toString());
        return site_hm;
    }
    
    
    /**
     * @param token
     * @param site
     * @author caromeo
     * Create TenantTab
     */
    
    public HashMap createTenantTab(String token, ArrayList<String> site) {
        String federation_nets = "";
        String tenant = "";
        String tenant_table = "";
        
        Integer version = null;
        
        JSONObject fedNetsObj = new JSONObject();
        JSONParser fedNetsObjParser = new JSONParser();
        JSONParser tenantsObjParser = new JSONParser();
        JSONObject tenantsObj = new JSONObject();
        
        DBMongo db = new DBMongo();
        HashMap tenant_hm = new HashMap();
        
        db.init("/home/carmelo/NetBeansProjects/BB/web/WEB-INF/configuration_bigDataPlugin.xml");
        db.connectLocale("10.9.240.1");
        tenant = db.getTenantDBName("token", token);

        for (String refSite : site) {
            tenantsObj.clear();
            
            try {
                federation_nets = db.retrieveFedNet(tenant, refSite); // lista delle fednet
                if (federation_nets == null) {
                    LOGGER.error("the selected SITE " + "-" + refSite + "-" + " is not present in the DB " + tenant + " I'm going to go ahead with the next site");
                    continue;
                }
                
                try {
                    fedNetsObj = (JSONObject) fedNetsObjParser.parse(federation_nets);
                } catch (ParseException ex) {
                    LOGGER.error("Error while parsing Fednets "+ ex.getMessage());
                }
                version = ((Double) fedNetsObj.get("version")).intValue(); //Current version

                tenant_table = db.getTenantTables(tenant, refSite, version);
                if (tenant_table == null) {
                    LOGGER.error("the selected SITE " + "-" + refSite + "-" + " is not present in the collection siteTables.");
                    continue;
                }
                try {
                    tenantsObj = (JSONObject) tenantsObjParser.parse(tenant_table);

                    tenant_hm.put(refSite, tenantsObj.clone());
                    System.out.println("contenuto hashMap per sito "+refSite+" "+tenant_hm.get(refSite).toString());

                } catch (ParseException ex) {
                    LOGGER.error("Error while parsing Tenants "+ ex.getMessage());
                }
            } catch (MDBIException ex) {
                LOGGER.error("Error while contacting Database "+ ex.getMessage());
            }
        }
        System.out.println("TENANT: "+ tenant_hm.toString());
        return tenant_hm;
    }
}
