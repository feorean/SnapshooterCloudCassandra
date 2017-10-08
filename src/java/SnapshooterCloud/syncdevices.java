package SnapshooterCloud;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Khalid
 */
@MultipartConfig
public class syncdevices extends HttpServlet {

  
    private Common common;
    private Dbapi dbapi;


    
    @Override
    public void init( ){
      // Get the file location where it would be stored.

      String clusterAddress = 
             getServletContext().getInitParameter("clusterAddress");
        
      
      dbapi = new Dbapi(clusterAddress); 
        
    }
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Common common = new Common(request, response);
        
//        Enumeration<String> headerNames = request.getHeaderNames();
//
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            System.out.print(headerName);
//            //System.out.print("n");
//
//            Enumeration<String> headers = request.getHeaders(headerName);
//            while (headers.hasMoreElements()) {
//                String headerValue = headers.nextElement();
//                System.out.print( headerValue);
//                //System.out.print("n");
//            }
//        }
        
        try {
            
            //First check if authenticated  
            common.proceedIfAuthenticated();
            
            String jsonlist = request.getParameter("devicelist");
            String userid = common.getCurrentSession().getAttribute("USERID").toString();
            
            jsonlist = jsonlist.replace("\\", "");
            
            JSONArray devices = common.strToJSONArray("devices", jsonlist);
            
            //System.out.println(devices.toString());
            
            
            final int n = devices.length();
            for (int i = 0; i < n; ++i) {
              final JSONObject person = devices.getJSONObject(i);
              

              
              //Add only if all values are set
              if (person.has("id") && person.getString("id") != null 
                  && person.has("type") && person.getString("type") != null  
                  && person.has("screen") && person.getString("screen") != null) {
              
                                
                    //Add if not exists
                    if (!dbapi.checkIfDeviceScreenExists(userid, person.getString("id"), person.getString("screen"))) {
                        dbapi.addNewDevice(userid,
                                person.getString("id"),
                                person.getString("type"),
                                person.getString("screen"));
                    } 
              } else {
                  
                  common.respondMessageErr("One of device values is null or not set! ( deviceid, type or screen )!"); 
              }
              
            }
            
            
            /*String screenName= request.getParameter("screenname"); 
            String userId = common.getCurrentSession().getAttribute("USERID").toString();
            String createDate = request.getParameter("createdate"); 
            String localId = request.getParameter("localid");
         
            
            if (deviceid == null || screenName == null || createDate == null || localId == null) {

              common.respondMessageErr("deviceid, screenName or createDate incorrect!"); 

              return;

            }  */
            
            common.respondMessageInfo("SYNC COMPLETE");

        }
        catch ( Exception e ){
            
            System.err.println("Exception in device sync: " + e.getMessage() ); 
            common.respondMessageErr("ERROR_IN_SYNC_DEVICES");
        }
   }


    
}
