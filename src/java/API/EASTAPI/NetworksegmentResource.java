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
import API.EASTAPI.Clients.NetworkSegment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.apache.log4j.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;


import org.jclouds.openstack.neutron.v2.domain.Network;
import JClouds_Adapter.NeutronTest;
import JClouds_Adapter.OpenstackInfoContainer;
import MDBInt.DBMongo;
import MDBInt.FederatedUser;
import MDBInt.FederationUser;
import OSFFMIDM.SimpleIDM;
import javax.ws.rs.core.Response;
import org.json.simple.parser.ParseException;
//import OSFFM_ORC.OrchestrationManager;


/**
 * REST Web Service
 *
 * @author gtricomi
 */
@Path("/fednet/eastBr/network")
public class NetworksegmentResource {
    
    @Context
    private String user,password;
    private UriInfo context;
    private SimpleIDM sidm;
    private DBMongo db;
    static final Logger LOGGER = Logger.getLogger(NetworksegmentResource.class);
    /**
     * Creates a new instance of NetworksegmentResource
     */
    public NetworksegmentResource() {
        sidm=new SimpleIDM();
        this.db=new DBMongo();
    }

    /**
     * Retrieves representation of an instance of EASTAPI.NetworksegmentResource
     * @return an instance of java.lang.String
     * @author gtricomi
     */
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public String add_netSegment(String content) throws JSONException{
        JSONObject reply = new JSONObject();
        JSONParser parser = new JSONParser();
        JSONObject input = null;
        String OSF_network_segment_id = null;
        String OSF_cmp_endpoint = null;
        String OSF_token = null;
        String OSF_tenant = null;
        String OSF_user = null;
        String OSF_region = null;
        String OSF_cloud = null;
        String response = "";
        JSONObject network_info = null;
        try {
            /*
             {
             "network_segment_id": "55b24c84-b96a-45ab-b007-9eee9c487c31",
             "cmp_endpoint": "http://172.17.1.217:35357/v2.0",
             "token": "86734b78980"
             }
             */
            //retrieve JSON value from REST request
            input = new JSONObject(content);
            OSF_token = (String) input.get("token");
            OSF_network_segment_id = (String) input.get("network_segment_id");
            OSF_cmp_endpoint = (String) input.get("cmp_endpoint");

            //verrà restituito l'OSFFM endpoint
            //ricavare dal simple IDM gli elementi che mi mancano ovvero:
            //String endpoint, String tenant, String user, String password, String region
            String tenantDB = this.db.getTenantDBName("token", OSF_token);
            JSONObject federationcredential = new JSONObject(this.db.getFederationCredential(tenantDB, OSF_token));
            JSONObject federatedcredential = new JSONObject(this.db.getFederatedCredential(tenantDB, OSF_token, OSF_cmp_endpoint));
            //funzione che verifica l'esistenza del netsegment
            NeutronTest neutron = new NeutronTest(OSF_cmp_endpoint, federationcredential.getString("federationUser"), federatedcredential.getString("federatedUser"), federatedcredential.getString("federatedPassword"), federatedcredential.getString("Region"));
            Network netSearched = neutron.getNetworkFromId(OSF_network_segment_id);
            if (netSearched == null) {
                reply.append("returncode", 1);
                reply.append("errormesg", "Netegment Not present on cloud pointed by cmp_endpoint");
                reply.append("network_info", "");
                return reply.toString();
            } else {
                //FOR FUTURE USAGE: these parameters will be provided from FEDSDN or DASHBOARD OS2OS?
                HashMap params = new HashMap();
                //////String dhcpEnable=(String)input.get("dhcpEnable");//FOR FUTURE USAGE
                String dhcpEnable = null;
                params.put("dhcpEnable", dhcpEnable);
                //////String shared=(String)input.get("shared");//FOR FUTURE USAGE
                String shared = null;
                params.put("shared", shared);
                //////String external=(String)input.get("external");//FOR FUTURE USAGE
                String external = null;
                params.put("external", external);
                //////String adminStateUp=(String)input.get("adminStateUp");//FOR FUTURE USAGE
                String adminStateUp = null;
                params.put("adminStateUp", adminStateUp);

                sidm = new SimpleIDM(); //>>>BEACON: VERIFY THIS POINT
                String dbName = sidm.retrieve_TenantDB("federationTenant", OSF_tenant);

                sidm.setDbName(dbName);  //>>>BEACON: FOR THE MOMENT OUR TESTING DB IS CALLED beacon
                //sidm.setDbName("beacon");

                //FederationUser fu = sidm.getFederationU(OSF_token, OSF_cmp_endpoint);//OSF_cmp_endpoint questo non è usato
/* //>>>BEACON: CREARE SERVIZIO SUL BEACON BROKER PER INVOCARE QUESTA FUNZIONALITÀ
                OrchestrationManager om = new OrchestrationManager();
                response = om.networkSegmentAdd(dbName, fu, OSF_network_segment_id, OSF_cloud, params).toString();
                
                
                
*/              
                JSONObject job = new JSONObject(); 
                JSONObject job_in = new JSONObject(); 
                JSONObject j_map =new JSONObject(params);
                job_in.put("token",OSF_token);
                job_in.put("endpoint", OSF_cmp_endpoint);
                job.put("fedUser", job_in);
                job.put("dbName",dbName);
                job.put("netSeg",OSF_network_segment_id);
                
                job.put("CloudName",OSF_cloud);
                job.put("hashMapParam",j_map);
                String baseBBURL=""; /////////* OTTENERE BASE URL BB*/////////////
                /* devo chiamare il put??*/
                
                NetworkSegment ns = new NetworkSegment(user, password);
                Response r;
                r = ns.addNetSegm(job, baseBBURL);
                
                org.json.simple.parser.JSONParser p = new org.json.simple.parser.JSONParser();
                Object obj = null;
                try {
                    obj = p.parse(r.readEntity(String.class));
                } catch (ParseException ex) {
                    LOGGER.error("Exception occurred in Parsing JSON returned from FEDSDN \n" + ex.getMessage());
                }
                
                JSONObject output = (JSONObject) obj;
                reply.append("returncode", output.get("returncode"));
                reply.append("errormesg", output.get("errormesg"));
                network_info.put("internalId", (String) ((org.json.JSONArray) output.get("ResponseArray")).getJSONObject(0).get("internalId"));
                network_info.put("FedSDN_netSegId", (String) ((org.json.JSONArray) output.get("ResponseArray")).getJSONObject(0).get("FedSDN_netSegId"));
                network_info.put("network_address", (String) ((org.json.JSONArray) output.get("ResponseArray")).getJSONObject(0).get("network_address"));
                network_info.put("network_mask", (String) ((org.json.JSONArray) output.get("ResponseArray")).getJSONObject(0).get("network_mask"));
                network_info.put("size", (String) ((org.json.JSONArray) output.get("ResponseArray")).getJSONObject(0).get("size"));
                reply.append("network_info", network_info);
                return reply.toString();
            }
        } catch (JSONException pe) {
            reply.append("returncode", 1);
            reply.append("errormesg", "JSON_INPUT_UNPARSABLE: OPERATION ABORTED! " + pe.getMessage());
            reply.append("network_info", "");
            LOGGER.error(pe.getMessage());
            return reply.toString();
        } catch (Exception e) {
            reply.append("returncode", 1);
            reply.append("errormesg", "Generic Exception occurred! Contact Administrator.");
            reply.append("network_info", "");
            LOGGER.error(e.getMessage());
            return reply.toString();
        }
        //Momentaneamente si lavora con una approccio end to end nei confronti del fedsdn quindi questa parte non và tenuta in considerazione e viene lasciata per futuri sviluppi 
        ////L'APPROCCIO (di gestione di tutte le cloud da parte del fedsdn con un solo comando potrebbe essere corretto). UPDATE: È CORRETTO MA SONO COSTRETTO A RIEMPIRE MANUALMENTE LE INFORMAZIONI SUL FEDSDN
        ////FACENDO SI CHE LE RICHIESTE SIANO DIROTTATE SUL OSSFM(probabilmente l'approccio corretto è quello di aggiungere 
        //////manualmente attraverso chiamate ai REST webservice del fedsdn le informazioni sui vari cloud passando però come cmp_endpoint 
        //////l'indirizzo del OSFFM
        ////////UPDATE:28/07 il cmp_endpoint sul fedsdn è quello delle cloud
       
        //return reply.toJSONString();
    }
            
            
            
            
            
            
    }

