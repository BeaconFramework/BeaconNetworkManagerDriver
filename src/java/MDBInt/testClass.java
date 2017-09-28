/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MDBInt;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author Giuseppe Tricomi
 */
public class testClass {
    
    /*
     public static void main(String[] args) {
         DBMongo m=new DBMongo();
         m.connectLocale("10.9.240.1");
         //DB database = m.getDB(m.getIdentityDB());
       /*DBCollection collection = database.getCollection("Federation_Credential");
       BasicDBObject researchField = new BasicDBObject("federationTenant", "review");
       DBCursor risultato = collection.find( researchField,(BasicDBObject)new BasicDBObject().put("federationTenant", 1));
       //CenantName=(String)risultato.get("federationTenant");
        // System.out.println(m.getfedsdnTenantid("review", "prova1test"));
        // String tmp=m.getUser("beacon","credentials", "userFederation", "20306e7ca1d77c289011e7683797cb48");
         try{
           //  JSONObject l= new JSONObject(tmp);
         //    FederationUser fu=new FederationUser(l.toString());
            // m.insertUser("beacon", "credentials", fu.toString());
             
         }
         catch(Exception e){}
         
     }
    */
    public static void main(String[] args) {
        DBMongo m=new DBMongo();
        //m.init("/home/giuseppe/NetBeansProjects/BeaconNetworkManagerDriver/web/WEB-INF/configuration_bigDataPlugin.xml");
        m.connectLocale("10.9.240.1");
        try{
        m.updateTableData("review", "site2", 20, 100);
        //System.out.println(m.getVersionBNATables("review", 7, "CETIC"));
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        
        
        
        
        
        
        //TENANT TABLE
        /*
        String result = m.getTenantTablesFromFedTenant("review", "review", "UME", "entryTenantTab", null);
        System.out.println("RES: "+result);
        
        result = m.getTenantTablesFromFedTenant("review", "review", "UME", "entryTenantTab", 114);
        System.out.println("RES: "+result);
        */
        
        
        //SITE TABLE
        /*
        //m.insertSiteTables("review", "UME", "{ \"referenceSite\" : \"UME\", \"version\": 115, \"entrySiteTab\" : { \"tenant_id\" : \"3029a98f60c24ac1b4ef4636c4ee3006\", \"name\" : \"UME\", \"fa_url\" : \"10.9.1.159:4567\", \"site_proxy\" : [ { \"port\" : 4789, \"ip\" : \"192.168.87.250\" } ] }}");

        String result = m.getSiteTablesFromFedTenant("review", "UME", "entrySiteTab", null);
        System.out.println("RES: "+result);
        
        result = m.getSiteTablesFromFedTenant("review", "UME", "entrySiteTab", 115);
        System.out.println("RES: "+result);
        */
        
        
        //HELP TABLE
        /*
        String result = m.getFednetsInSiteTablesFromFedTenant("review", "CETIC", "fednets", null);
        System.out.println("RES: "+result);
        
        result = m.getFednetsInSiteTablesFromFedTenant("review", "CETIC", "fednets", 1);
        System.out.println("RES: "+result);
        */
    }
    
}
/*
{ "_id" : "userFederation", "federationUser" : "userFederation", "federationPassword" : "20306e7ca1d77c289011e7683797cb48", "crediantialList" : [ 	{ 	"federatedUser" : "admin", 	"federatedCloud" : "UME", 	"federatedPassword" : "password" }, 	{ 	"federatedUser" : "admin", 	"federatedCloud" : "CETIC", 	"federatedPassword" : "password" }, 	{ 	"federatedUser" : "admin", 	"federatedCloud" : "UCM", 	"federatedPassword" : "password" } ], "insertTimestamp" : NumberLong("1456747122723") }
*/
