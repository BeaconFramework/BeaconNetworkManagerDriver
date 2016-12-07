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

/**
 * REST Web Service
 *
 * @author gtricomi
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
/* //>>>BEACON: CREARE SERVIZIO SUL BEACON BROKER PER INVOCARE QUESTA FUNZIONALITÀ
            OrchestrationManager om = new OrchestrationManager();
            String result = om.makeLink(id.longValue(), federationUser, null, m);// null will be substituted with an ArrayList<JSONObject> netTables that correspond at lic.getNetwork_tables()
                
*/               
            
            if (!result.equals("ok")) {
                reply.put("returncode", 1);
                reply.put("errormesg", "Generic Exception: OPERATION ABORTED");
                return reply.toJSONString();
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
}
