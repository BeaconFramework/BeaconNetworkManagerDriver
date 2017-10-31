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
import API.SOUTHBR.FA_ScriptInvoke;
import API.SOUTHBR.FA_client4Network;
import API.SOUTHBR.FA_client4Sites;
import API.SOUTHBR.FA_client4Tenant;
import JClouds_Adapter.KeystoneTest;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.ws.rs.core.Response;
import org.json.JSONException;
import utils.Exception.WSException;

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
    //String configFile="/home/giuseppe/NetBeansProjects/BeaconNetworkManagerDriver/web/WEB-INF/configuration_bigDataPlugin.xml";
    String configFile="/home/beacon/beaconConf/configuration_bigDataPlugin.xml";
    /**
     * Creates a new instance of LinksResource
     */
    public LinksResource() {
    }

    /**
     * Retrieves representation of an instance of EASTTAPI.LinksResource
     *
     * @return an instance of java.lang.String
     */
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public String LinkFunctions(String content) {
        ArrayList<String> arrayTables = new ArrayList<String>();
        arrayTables.add("BNATableData");
        arrayTables.add("TenantTables");
        arrayTables.add("siteTables");
        arrayTables.add("fednetsinSite");

        JSONObject reply = new JSONObject();
        JSONParser parser = new JSONParser();
        org.json.JSONObject input = null;
        LinkInfoContainers lic = new LinkInfoContainers();
        String baseBBURL = ""; /////* Ricavare url BB*//////////   
        JSONObject inputSiteList = null;
        String token = "";
        String fednetid = null;
        HashMap SiteTables = new HashMap();
        HashMap TenantTables = new HashMap();
        HashMap MapSegTables = new HashMap();
        String result = "";
        ArrayList<String> arraySites = null;
        boolean onePresent=false;
        try {
            //input = (JSONObject) parser.parse(content);
            String beta=content.replace("\"{", "{");
            beta=beta.replace("}\"", "}");
            beta=beta.replace("\\\"", "\"");
            beta=beta.replace("\\n", "");
            input=new org.json.JSONObject(beta);
            lic.setType((String) input.get("type"));
            lic.setToken((String) input.get("token"));//utilizzerò questo elemento per identificare federation tenant
            //lic.setCommand((String) input.get("Command"));
            lic.setFa_endpoints(((org.json.JSONArray) input.get("fa_endpoints")));
            lic.setNetwork_tables(((org.json.JSONArray) input.get("network_table")));//not used for this moment the tables are recalculated
            if (input.has("fednetID")) {
                fednetid = (String) input.get("fednetID");
            } else {
                fednetid = null;
            }
        } catch (JSONException pe) {
            reply.put("returncode", 1);
            reply.put("errormesg", "INPUT_JSON_UNPARSABLE: OPERATION ABORTED");
            return reply.toJSONString();
        }

        try {
            String errMSG="";
            boolean errorOccured=false;
            DBMongo m = new DBMongo();
            m.init(configFile);
             m.connectLocale("10.9.240.1");
  /////////////
             //lic.setToken("3efb8c19-92a9-43bc-75d2-e4ff6f53cd2a");
  //////////////7
             
            String federationUser = m.getTenantName("token", lic.getToken());
           //  federationUser ="review";
            ArrayList<org.json.JSONObject> netTables;
            ArrayList<org.json.JSONObject> fa_endPoints;
            //netTables = lic.getNetwork_tables();
            HashMap<String, org.json.JSONObject> hm = this.retrieveFednetsInvolved(federationUser, lic.getNetwork_tables(), m);
            HashMap<String, org.json.JSONObject> hmS_T = new HashMap<String, org.json.JSONObject>();
            Set<String> sites = hm.keySet();
            Integer bb_version = null;
            Integer bna_version = null;
            Integer onebna_version = null;
            try {
                FA_client4Network fantmp = new FA_client4Network("10.9.1.103:4567", federationUser, "admin", "review");
                org.json.JSONObject fa0obj = new org.json.JSONObject(m.getFAInfo(federationUser, "ONE"));
                String faurlone = fa0obj.getString("Ip") + ":" + fa0obj.getString("Port");
                org.json.JSONObject rrtmp = fantmp.getNetworkTableList("10.9.1.103:4567", "00000000000000000000000000000001");
                if (rrtmp.has("version")) {
                    onebna_version = (Integer) rrtmp.get("version");
                } else {
                    onebna_version = 0;
                }
            } catch (Exception e) {
                onebna_version = 0;
            }
            
            for (String s : sites) {
                if(s.equals("ONE"))
                    continue;
                else{
                try {
                    
                    //int fednetId = m.getfedsdnFednetIDFromBNMParams(federationUser,(hm.get(s)).getString("name"), s);//("review","subnetflex", "CETIC"));//questi dati vengono dal jsonproveniente dal BNM
                    int fednetId =(int)( hm.get(s)).get("fednetId");
                    bb_version = m.getVersionBNATables(federationUser, fednetId, s);//("review", 7, "CETIC");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                //testare la versione tra qeulla ottenuta e quella presente sul BNA
                //per andare avanti con il processo di creazione delle tabelle
                String endpoint = "";

                //recuperare da mongo le informazioni relative al BNA del sito ottenuto e costruire il client per il network del BNA
                try {
                    String cloudID = s;
                    
                    String tmp = m.getFederatedCredentialfromTok(federationUser, federationUser, lic.getToken(), cloudID);
                    org.json.JSONObject jc = null;
                    if (tmp == null) {
                        onePresent=true;
                        throw new Exception("Cannot Retrieve cloud Credentials! BNA table create aborted!");
                    } else {
                        jc = new org.json.JSONObject(tmp);
                    }
                    endpoint = (String) (m.getDatacenterFromId(federationUser, cloudID)).get("idmEndpoint");
                    FA_client4Network fan1 = new FA_client4Network(endpoint, federationUser, jc.getString("federatedUser"), jc.getString("federatedPassword"));
                    String faurl = "";
                    try {
                        org.json.JSONObject faobj = new org.json.JSONObject(m.getFAInfo(federationUser, cloudID));
                        faurl = faobj.getString("Ip") + ":" + faobj.getString("Port");
                    } catch (org.json.JSONException je) {
                        throw new org.json.JSONException("Cannot retrieve BNAInfo needed to interact with BNA of the site: " + cloudID);
                    }
                    KeystoneTest key = new KeystoneTest(federationUser, jc.getString("federatedUser"), jc.getString("federatedPassword"), endpoint);
                    try{
                    org.json.JSONObject rr = fan1.getNetworkTableList(faurl, key.getTenantId(federationUser));
                    //INSERIRE CONTROLLO PER TABELLA NON PRESENTE SUL BNA
                    hmS_T.put(s, rr);
                    if(rr.has("version"))
                        bna_version = (Integer) rr.get("version");
                    else 
                        bna_version=0;
                    }
                    catch(Exception e){
                        bna_version=0;
                    }
                    //Melo's            
                    HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
                    String fednet = (String)( hm.get(cloudID)).getString("fedname");
                    ArrayList<String> resultArr=null;
                    try {
                        resultArr = m.retrieveBNANetSegFromFednet(federationUser, cloudID, bb_version, fednet);
                        //map.put(s, resultArr);
                    } catch (MDBIException ex) {
                        LOGGER.error(ex.getMessage());
                        break;//verificare 
                    }
                    // Melo's End          
                    org.json.JSONObject tab = null;
                    if (bb_version < bna_version) {//BB<BNA: salvare la tabella ricavata dal BNa su Mongo, aumentare di uno la tabella estratta da mongo mandarla al bna e successivamente salvarla su mongo
                        //funzione di alfonso per split e storage tabella ricevuta da BNA
                        //Alfonso's bookmark.
                        //org.json.JSONObject table,String refSite, String ten, DBMongo m
                        this.storeIncomingBNANetTables(hmS_T.get(cloudID), cloudID, federationUser, m);
                        
                            this.updateVersionInTables(federationUser, cloudID, bb_version, bna_version, m, arrayTables);
                        int providedversion=1;
                       if(bna_version>onebna_version)
                           providedversion=bna_version;
                       else
                           providedversion=onebna_version;
                        tab = this.constructNetworkTableJSON(resultArr, providedversion + 1,sites,m,federationUser);
                        System.out.println(tab.toString());
                    } else if (bb_version == bna_version) {//BB=BNA: recuperare la tabella e mettere in append le entry ricevute
                        org.json.JSONObject tmpjotab =(org.json.JSONObject)hmS_T.get(cloudID);
                        int providedversion=1;
                       if(bb_version>onebna_version)
                           providedversion=bb_version;
                       else
                           providedversion=onebna_version;
                        tab=this.append_ConstructNetworkTableJSON(tmpjotab, resultArr, providedversion);
                        System.out.println(tab.toString());
                     } else {//BB>BNA: inviare direttamente la tabella al BNA
                       int providedversion=1;
                       if(bb_version>onebna_version)
                           providedversion=bb_version;
                       else
                           providedversion=onebna_version;
                       tab=this.constructNetworkTableJSON(resultArr, providedversion,sites,m,federationUser);
                       System.out.println("TAB---->"+tab.toString());
                    }
                    onePresent=sites.contains("ONE");
                    //INVOKE CREATENETTABLE ON BNA
                    String endpointone="";
                    String userone="";
                    String passone="";
                    org.json.JSONObject tenantEntry0=null;
                    //String endpoint_=(String)(new org.json.JSONObject(m.getDatacenter(federationUser, cloudID))).get("idmEndpoint");
                    org.json.JSONObject cred=new org.json.JSONObject(m.getFederatedCredentialfromTok(federationUser,federationUser,lic.getToken(), cloudID));
                    String user=cred.getString("federatedUser");
                    String pass=cred.getString("federatedPassword");
                    ////1 inserire tenant su BNA
                    if(onePresent){
                        String tmpONE=m.getONETenantTables(federationUser, "ONE");
                        tenantEntry0=new org.json.JSONObject(tmpONE);
                        endpointone=(String)(new org.json.JSONObject(m.getDatacenter(federationUser, "ONE"))).get("idmEndpoint");
                        org.json.JSONObject credone=new org.json.JSONObject(m.getFederatedCredentialfromTok(federationUser,federationUser,lic.getToken(), "ONE"));
                        userone=credone.getString("federatedUser");
                        passone=credone.getString("federatedPassword");
                        FA_client4Tenant fat0=new FA_client4Tenant(endpointone,federationUser,userone,passone);
                        System.out.println("\n\nTENANTONETABLE:\n"+tenantEntry0.toString(0));
                        org.json.JSONObject fa0obj = new org.json.JSONObject(m.getFAInfo(federationUser, "ONE"));
                        String faurlone = fa0obj.getString("Ip") + ":" + fa0obj.getString("Port");
                         if(!fat0.createTenantFA(tenantEntry0, faurlone)){
                       //gestione dell'errore 
                        }
                    }
                    String tmptmp=m.getTenantTables(federationUser,cloudID,bb_version);
                    org.json.JSONObject tenantEntry=new org.json.JSONObject(tmptmp);
                    FA_client4Tenant fat=new FA_client4Tenant(endpoint,federationUser,user,pass);
                    System.out.println("\n\nTENANTTABLE:\n"+tenantEntry.toString(0));
                    if(!fat.createTenantFA(tenantEntry, faurl)){
                       //gestione dell'errore 
                    }
                    ////2 Inserire site table su BNA
                    /*
                    [
                        {"tenant_id": "aa146d1022fe4dd1a29042c2f234d847", "name": "site1", "site_proxy": [{"ip": "192.168.32.250", "port": 4789}], "fa_url": "192.168.32.250:4567"},
                        {"tenant_id": "aa146d1022fe4dd1a29042c2f234d84b", "name": "site2", "site_proxy": [{"ip": "192.168.87.250", "port": 4789}], "fa_url": "192.168.87.250:4567"}
                    ]
                    */
                    org.json.JSONArray siteMap=new org.json.JSONArray();
                    for(String refSite : sites)
                        siteMap.put(new org.json.JSONObject(m.getSiteTables(federationUser,cloudID,bb_version,refSite)));
                    FA_client4Sites fas=new FA_client4Sites(endpoint,federationUser,user,pass);
                    org.json.JSONObject fa_url=new org.json.JSONObject(m.getFAInfo(federationUser, cloudID));
                    siteMap=(org.json.JSONArray)this.modify_siteNames(siteMap, 0, sites);
                    System.out.println("\n\nSITETABLE:\n"+siteMap.toString(0));
                    Response r=fas.createSiteTable(tenantEntry.getString("id"), fa_url.getString("Ip")+":"+fa_url.getString("Port"), siteMap.toString(0));//aggiungere check
                    if(onePresent){
                        FA_client4Sites fas0=new FA_client4Sites(endpointone,federationUser,userone,passone);
                        org.json.JSONObject fa0obj = new org.json.JSONObject(m.getFAInfo(federationUser, "ONE"));
                        String faurlone = fa0obj.getString("Ip") + ":" + fa0obj.getString("Port");
                        Response r0=fas0.createSiteTable(tenantEntry0.getString("id"), faurlone, siteMap.toString(0));
                    }
                    ////3 Inserire NetTable su BNA
                    FA_client4Network fan=new FA_client4Network(endpoint,federationUser,user,pass);
                    tab = (org.json.JSONObject)this.modify_siteNames(tab, 1, sites);
                    try{
                        tab=this.deleteClonedJARinTab(tab);
                    }
                    catch(org.json.JSONException je){
                            System.out.println("\ndeleteClonedJARinTab error:\n"+je.getMessage());
                            }
                    System.out.println("\n\nNETTABLE:\n"+tab.toString(0));
                    Response rn=fan.createNetTable(tenantEntry.getString("id"), fa_url.getString("Ip")+":"+fa_url.getString("Port"), tab.toString(0));//aggiungere check
                    if(onePresent){
                        FA_client4Network fan0=new FA_client4Network(endpointone,federationUser,userone,passone);
                        org.json.JSONObject fa0obj = new org.json.JSONObject(m.getFAInfo(federationUser, "ONE"));
                        String faurlone = fa0obj.getString("Ip") + ":" + fa0obj.getString("Port");
                        Response rn0=fan.createNetTable(tenantEntry0.getString("id"), faurlone, tab.toString(0));
                    }
                }
                catch (MDBIException ex) {
                    System.out.println(ex.getMessage());
                    errMSG="MDBIException occurred in inner Try-Catch of LinksResource WS! Exception Message: "+ex.getMessage();
                } 
                catch (org.json.JSONException ex) {
                    System.out.println(ex.getMessage());
                    errMSG="JSONException occurred in inner Try-Catch of LinksResource WS! Exception Message: "+ex.getMessage();
                } 
                catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    errMSG="Generic Exception occurred in inner Try-Catch of LinksResource WS! Exception Message: "+ex.getMessage();
                }
            }
//                FA_ScriptInvoke fi = null;
//                if (!s.equals("ONE")) {
//
//                    String cloudID = s;
//
//                    String tmp = m.getFederatedCredentialfromTok(federationUser, federationUser, lic.getToken(), cloudID);
//                    org.json.JSONObject jc = null;
//                    if (tmp == null) {
//                        onePresent = true;
//                        throw new Exception("Cannot Retrieve cloud Credentials! BNA table create aborted!");
//                    } else {
//                        jc = new org.json.JSONObject(tmp);
//                    }
//                    String endpoint = (String) (m.getDatacenterFromId(federationUser, cloudID)).get("idmEndpoint");
//                    //FA_client4Network fan1 = new FA_client4Network(endpoint, federationUser, jc.getString("federatedUser"), jc.getString("federatedPassword"));
//                    String faurl = "";
//                    try {
//                        org.json.JSONObject faobj = new org.json.JSONObject(m.getFAInfo(federationUser, cloudID));
//                        faurl = faobj.getString("Ip") + ":" + faobj.getString("Port");
//                    } catch (org.json.JSONException je) {
//                        throw new org.json.JSONException("Cannot retrieve BNAInfo needed to interact with BNA of the site: " + cloudID);
//                    }
//                    //KeystoneTest key = new KeystoneTest(federationUser, jc.getString("federatedUser"), jc.getString("federatedPassword"), endpoint);
//                    String tmptmp = m.getTenantTables(federationUser, cloudID);
//                    org.json.JSONObject tenantEntry = new org.json.JSONObject(tmptmp);
//                    org.json.JSONObject obj=(org.json.JSONObject)tenantEntry.get("entryTenantTab");
//                    String tid=obj.getString("tenant_id");
//                    fi = new FA_ScriptInvoke(endpoint, tid, "admin", "0penstack");
//                    try {
//                        fi.FAScript(faurl.split(":")[0] + ":5051");
//
//                    } catch (WSException wse) {
//                        //something here
//
//                    }
//                }

            }
        } catch (Exception eg) {
            reply.put("returncode", 1);
            reply.put("errormesg", "Generic Exception: OPERATION ABORTED");
            return reply.toJSONString();
        }
        reply.put("returncode", 0);
        reply.put("errormesg", "None");
        return reply.toJSONString();
    }
    
    private org.json.JSONObject deleteClonedJARinTab(org.json.JSONObject tab )throws JSONException{
        org.json.JSONArray container=tab.getJSONArray("table");
        if(container.length()>1)
        {
            for (int j = 0; j < container.length(); j++) {
                org.json.JSONArray ja1 = container.getJSONArray(j);
                for (int i = j + 1; i < container.length(); i++) {
                    org.json.JSONArray janext=container.getJSONArray(i);
                    org.json.JSONObject ent1_i=janext.getJSONObject(0);
                    org.json.JSONObject ent2_i=janext.getJSONObject(1);
                    org.json.JSONObject ent1_j=ja1.getJSONObject(0);
                    org.json.JSONObject ent2_j=ja1.getJSONObject(1);
                    boolean firstentry= (ent1_i.getString("tenant_id").equals(ent1_j.getString("tenant_id")))&&(ent1_i.getString("site_name").equals(ent1_j.getString("site_name")))&&(ent1_i.getString("vnid").equals(ent1_j.getString("vnid")))&&(ent1_i.getString("name").equals(ent1_j.getString("name")));
                    boolean secondentry= (ent2_i.getString("tenant_id").equals(ent2_j.getString("tenant_id")))&&(ent2_i.getString("site_name").equals(ent2_j.getString("site_name")))&&(ent2_i.getString("vnid").equals(ent2_j.getString("vnid")))&&(ent2_i.getString("name").equals(ent2_j.getString("name")));    
                    if(firstentry&&secondentry)
                        container.remove(i);
                }
            }
        }
        tab.remove("table");
        tab.put("table", container);
        return tab;
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
        //String configFile="/home/beacon/beaconConf/configuration_bigDataPlugin.xml";
        m.init("/home/beacon/beaconConf/configuration_bigDataPlugin.xml");
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
        m.init("/home/beacon/beaconConf/configuration_bigDataPlugin.xmll");
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
        m.init("/home/beacon/beaconConf/configuration_bigDataPlugin.xml");
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
        m.init("/home/beacon/beaconConf/configuration_bigDataPlugin.xml");
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
        m.init("/home/beacon/beaconConf/configuration_bigDataPlugin.xml");
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
        m.init("/home/beacon/beaconConf/configuration_bigDataPlugin.xml");
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

        //db.init("/home/beacon/beaconConf/configuration_bigDataPlugin.xml");
        db.init(configFile);
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
        
        //db.init("/home/beacon/beaconConf/configuration_bigDataPlugin.xml");
        db.init(configFile);
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
    
    private HashMap retrieveFednetsInvolved(String federationUser,org.json.JSONArray networks_table,DBMongo m)throws Exception{
        HashMap hm=new HashMap();
       for(int i =0; i<networks_table.length();i++){
            org.json.JSONObject ob=(org.json.JSONObject)networks_table.getJSONObject(i);
            String s=(String) ob.get("site");
            int fednetId;
            org.json.JSONObject fednetobj=new org.json.JSONObject();
            try{
                fednetId=m.getfedsdnFednetIDFromBNMParams(federationUser, (String)ob.get("name"), s);
                org.json.JSONObject obtmp=new org.json.JSONObject(m.getfedsdnFednet(fednetId,federationUser));
                fednetobj.put("fedname", obtmp.getString("name"));
                fednetobj.put("fednetId",fednetId);
            }
            catch(Exception e){
                throw new Exception("Exception occurred in retrieving information needed for BNA NetTable construction! "+e.getMessage());
            }
            hm.put(s, fednetobj);
        }
        return hm;
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
        db.init(configFile);
        //db.init("/home/beacon/beaconConf/configuration_bigDataPlugin.xml");
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
    
    /**
     * review
     *
     * @param table
     * @param refSite
     * @param tenreview
     * @param m
     * @author apanarello
     * @return
     */
    private org.json.JSONObject storeIncomingBNANetTables(org.json.JSONObject table, String refSite, String ten, DBMongo m) {

        UUID uuid = null;
        Integer version = null;
        org.json.JSONObject objectJson = new org.json.JSONObject();
        org.json.JSONObject bnaSegTab = new org.json.JSONObject();
        org.json.JSONObject jsonObInner = new org.json.JSONObject();
        String tenant = ten;
        try {
            /*<<<<<<<<<<<<<<<<<<estrazione versione della tabella ricevuta>>>>>>>>>>>>>>>>*/
            version = table.getInt("version");
            /*<<<<<<<<<<<<<<<<<<   estrazione JSONObject innestato nella tabella   >>>>>>>>>>>>>>>>*/
            org.json.JSONObject innerJson = table.getJSONObject("table");
            /*<<<<<<<<<<<<<<<<<<   Ottengo set di chiava JSONObject innestato   >>>>>>>>>>>>>>>>*/
            Iterator tableKeys = innerJson.keys();

            /*<<<<<<<<<<<<<<<<<<  per ogni chiave estraggo il JSONArray associato  >>>>>>>>>>>>>>>>*/
            while (tableKeys.hasNext()) {

                org.json.JSONArray Arraynext = innerJson.getJSONArray(tableKeys.next().toString());
                Integer dim = Arraynext.length();
                /*<<<<<<<<<<<<<<<<<<  Creo chiave FK (Foreing Key    >>>>>>>>>>>>>>>>*/
                uuid = UUID.randomUUID();
                /*<<<<<<<<<<<<<<<<<<   estraggo elementi array (JSON OBJECT)   >>>>>>>>>>>>>>>>*/
                for (int i = 0; i < dim; i++) {
                    /*<<<<<<<<<<<<<<<<<<   Aggancio la chiave al JSON    >>>>>>>>>>>>>>>>*/
                    jsonObInner = Arraynext.getJSONObject(i);
                    bnaSegTab.put("FK", uuid.toString());
                    bnaSegTab.put("netEntry", jsonObInner); //da inserirer in BNANetSeg
                    try {
                        /*<<<<<<<<<<<<<<<<<<Inserimento BNANetSeg>>>>>>>>>>>>>>>>*/
                        m.insertNetTables(tenant, bnaSegTab.toString(0));
                    } catch (MDBIException ex) {
                        System.out.println("errore in MONGO:   " + ex.getMessage());
                        LOGGER.error("errore in MONGO:   " + ex.getMessage());
                    }

                }
                try {
                    /*<<<<<<<<<<<<<<<<<<      >>>>>>>>>>>>>>>>*/
                    /*<<<<<<<<<<Inserimento BNATableData>>>>>>>>>>>>>>*/
                    m.insertTablesData(uuid.toString(), tenant, version, refSite, jsonObInner.get("name").toString()); //ATTENZIONARE VEDI COMMENTO ***
                } catch (MDBIException ex) {
                    System.out.println("error in Mongo:   " + ex.getMessage());
                    LOGGER.error("error in Mongo:   " + ex.getMessage());
                }

            }

        } catch (JSONException ex) {
            System.out.println("errore parse:   " + ex.getMessage());
            LOGGER.error("errore parse:   " + ex.getMessage());
        }

        return objectJson;
    }
    
    /**
     * 
     * @param networks
     * @param version
     * @return
     * @throws JSONException 
     */
    public org.json.JSONObject constructNetworkTableJSON(ArrayList<String> networks, int version, Set sites,DBMongo m,String tenant) throws JSONException {
        /*
        
        [{ "tenant_id" : "aa477ca20d2f41a18f8c380db65990d5" , "site_name" : "UME" , "vnid" : "dd5ecc37-d27c-452e-9bcd-eeb6e9c55b79" , "name" : "reviewPrivate"},{ "tenant_id" : "d044e4b3bc384a5daa3678b87f97e3c2" , "site_name" : "CETIC" , "vnid" : "a779dd43-52bc-4172-9f8d-9a38374547aa" , "name" : "reviewPrivate"}]
         */
        org.json.JSONArray array_ext = new org.json.JSONArray();
        org.json.JSONArray bja = new org.json.JSONArray();
        org.json.JSONObject jo = null;
        Iterator it = networks.iterator();
        while (it.hasNext()) {

            if (!sites.contains("ONE")) {
                String tab_entry = (String) it.next();
                jo = new org.json.JSONObject(tab_entry);
                array_ext.put(jo);

            } else {

                String tab_entry = (String) it.next();
                jo = new org.json.JSONObject(tab_entry);
                if (sites.contains(jo.getString("site_name"))) {
                    array_ext.put(jo);
                } else {
                    org.json.JSONObject ttt=new org.json.JSONObject(m.getONEnetEntry(tenant));
                    array_ext.put(ttt.getJSONObject("entryNetTab"));
                }
            }
            bja.put(array_ext);
        }
        /*   
        
       org.json.JSONObject json1 = new org.json.JSONObject();
       org.json.JSONObject json2 = new org.json.JSONObject();

       json1.put("tenant_id", "aa477ca20d2f41a18f8c380db65990d5");
       json1.put("site_name", "site1");
       json1.put("name", "testnetint");
       json1.put("vnid", "994522a6-22da-4106-bde7-6e5b74394ea9");
       
       json2.put("tenant_id", "d044e4b3bc384a5daa3678b87f97e3c2");
       json2.put("site_name", "site2");
       json2.put("name", "testnetint");
       json2.put("vnid", "473ff6df-53fc-4f09-a281-6f27be7ca7ac");
     
       
       JSONArray array_int = new JSONArray();
       
       array_int.put(json1);
       array_int.put(json2);
       array_ext.put(array_int);
      */   
       org.json.JSONObject global = new org.json.JSONObject();
       global.put("version", version);
       global.put("table", bja);
      return global;
    }
    
    /**
     * This function prepare the object for FA Create Network Table function.
     * @param sites
     * @return 
     * @author gtricomi
     */
    public org.json.JSONObject append_ConstructNetworkTableJSON(org.json.JSONObject toUpdate,ArrayList<String> networks,int version) throws JSONException{
       org.json.JSONArray array_ext = new org.json.JSONArray();
       org.json.JSONObject tmpjofednet =(org.json.JSONObject)toUpdate.get("table");
       Iterator<String> fednets=tmpjofednet.keys();
       while(fednets.hasNext()){
          String tmp=fednets.next();
          org.json.JSONArray jatmp=tmpjofednet.getJSONArray(tmp);
          array_ext.put(jatmp);
       }
       org.json.JSONArray ja=null;
       Iterator it=networks.iterator();
       while(it.hasNext()){
           String tab_entry=(String)it.next();
           ja=new org.json.JSONArray(tab_entry); 
           array_ext.put(ja);
       }  
       org.json.JSONObject global = new org.json.JSONObject();
       global.put("version", version);
       global.put("table", array_ext);
       return global;
    }
    /**
     * @author apanarello
     * @param fedUser
     * @param refSite
     * @param version
     * @param newVersion
     * @param m 
     */
    
     public void updateVersionInTables(String fedUser, String refSite, Integer version, Integer newVersion, DBMongo m,ArrayList<String> whatTable){
          for (int i = 0; i < whatTable.size(); i++) {
                m.updateTableVersion(fedUser, refSite, version, newVersion, whatTable.get(i));
          }
         
     }
     
    private Object modify_siteNames(Object tab, int site, Set<String> sites) {
        switch (site) {
            case 0: //this is site table
            {
                org.json.JSONArray ja = (org.json.JSONArray) tab;

                try {
                    for (int i = 0; i < ja.length(); i++) {
                        org.json.JSONObject jo = ja.getJSONObject(i);
                        if (((sites.contains("UME"))||(sites.contains("UME_NFV")) )&& ((sites.contains("CETIC"))|| (sites.contains("CETIC_NFV")))&& (!sites.contains("ONE"))) {
                            if ((jo.getString("name").equals("UME"))||(jo.getString("name").equals("UME_NFV"))) {
                                jo.remove("name");
                                jo.put("name", "site1");
                            } else if ((jo.getString("name").equals("CETIC"))||(jo.getString("name").equals("CETIC_NFV"))) {
                                jo.remove("name");
                                jo.put("name", "site2");
                            }
                        } else if ((sites.contains("ONE")) && ((sites.contains("UME"))||(sites.contains("UME_NFV")) )) {
                            if ((jo.getString("name").equals("UME"))||(jo.getString("name").equals("UME_NFV"))) {
                                jo.remove("name");
                                jo.put("name", "site1");
                            } else if (jo.getString("name").equals("ONE")) {
                                jo.remove("name");
                                jo.put("name", "ons");//"site2"
                            }
                        } else if ((sites.contains("ONE")) && ((sites.contains("CETIC"))||(sites.contains("CETIC_NFV"))) ){
                            if (jo.getString("name").equals("ONE")) {
                                jo.remove("name");
                                jo.put("name","ons" );//"site1"
                            } else if ((jo.getString("name").equals("CETIC"))||(jo.getString("name").equals("CETIC_NFV"))) {
                                jo.remove("name");
                                jo.put("name", "site2");
                            }
                        } else {
                            System.out.println("SOMETHINGS HAS GOING WRONG IN TABLES MANAGEMENT!");
                        }
                    }
                } catch (org.json.JSONException je) {
                    System.out.println("SOMETHINGS HAS GOING WRONG IN TABLES MANAGEMENT!");
                }
                break;
            }
            case 1: {
                try {
                    org.json.JSONArray ja_ext = ((org.json.JSONObject) tab).getJSONArray("table");
                    for (int j = 0; j < ja_ext.length(); j++) {
                        org.json.JSONArray ja = ja_ext.getJSONArray(j);

                        for (int i = 0; i < ja.length(); i++) {
                            org.json.JSONObject jo = ja.getJSONObject(i);
                            if (((sites.contains("UME"))||(sites.contains("UME_NFV")) )&& ((sites.contains("CETIC"))|| (sites.contains("CETIC_NFV")))&& (!sites.contains("ONE"))){
                                if ((jo.getString("site_name").equals("UME"))||(jo.getString("site_name").equals("UME_NFV"))) {
                                    jo.remove("site_name");
                                    jo.put("site_name", "site1");
                                } else if ((jo.getString("site_name").equals("CETIC"))||(jo.getString("site_name").equals("CETIC_NFV"))) {
                                    jo.remove("site_name");
                                    jo.put("site_name", "site2");
                                }
                            } else if ((sites.contains("ONE")) && ((sites.contains("UME"))||(sites.contains("UME_NFV")) )) {
                                if ((jo.getString("site_name").equals("UME"))||(jo.getString("site_name").equals("UME_NFV"))) {
                                    jo.remove("site_name");
                                    jo.put("site_name", "site1");
                                } else if (jo.getString("site_name").equals("ONE")) {
                                    jo.remove("site_name");
                                    jo.put("site_name","ons" );//"site2"
                                }
                            } else if ((sites.contains("ONE")) && ((sites.contains("CETIC"))||(sites.contains("CETIC_NFV"))) ){
                                if (jo.getString("site_name").equals("ONE")) {
                                    jo.remove("site_name");
                                    jo.put("site_name","ons" );//"site1"  jo.put("site_name", "site1");//"ons"
                                } else if ((jo.getString("site_name").equals("CETIC"))||(jo.getString("site_name").equals("CETIC_NFV"))) {
                                    jo.remove("site_name");
                                    jo.put("site_name", "site2");
                                }
                            } else {
                                System.out.println("SOMETHINGS HAS GOING WRONG IN TABLES MANAGEMENT!");
                            }
                        }
                    }
                } catch (org.json.JSONException je) {
                    System.out.println("SOMETHINGS HAS GOING WRONG IN TABLES MANAGEMENT!"+je.getMessage());
                }
                break;
            }
        }
        
        return tab;

    }
}
