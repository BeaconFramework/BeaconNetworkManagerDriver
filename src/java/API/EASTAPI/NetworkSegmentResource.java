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

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;

/**
 * REST Web Service
 * This class is generated for future use and it will be used to manage the information storing procedure if this is needed(or it will be removed).
 * @author gtricomi
 */
public class NetworkSegmentResource {

    /**
     * Creates a new instance of NetworkSegmentResource
     */
    private NetworkSegmentResource() {
        
    }

    /**
     * Get instance of the NetworkSegmentResource
     */
    public static NetworkSegmentResource getInstance() {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of NetworkSegmentResource class.
        return new NetworkSegmentResource();
    }

    /**
     * Retrieves representation of an instance of EASTAPI.NetworkSegmentResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/xml")
    public String getXml() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of NetworkSegmentResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/xml")
    public void putXml(String content) {
    }

    /**
     * DELETE method for resource NetworkSegmentResource
     */
    @DELETE
    public void delete() {
    }
}
