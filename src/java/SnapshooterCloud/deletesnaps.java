package SnapshooterCloud;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.File;
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
public class deletesnaps extends HttpServlet {

    private String filePath;
    private Common common;
    private Dbapi dbapi;


    
    @Override
    public void init( ){
      // Get the file location where it would be stored.
      filePath = 
             getServletContext().getInitParameter("store_location"); 

        
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
        
        JSONArray arr = new JSONArray();
        
        try {
            
            //First check if authenticated  
            common.proceedIfAuthenticated();
            
            String jsonlist = request.getParameter("ids");
            String userid = common.getCurrentSession().getAttribute("USERID").toString();
            
            jsonlist = jsonlist.replace("\\", "");
            
            JSONObject deleteList = new JSONObject(jsonlist);
        
            String deviceId = deleteList.getString("deviceid");
            
            JSONArray files = deleteList.getJSONArray("deletelist");
            
            //JSONArray devices = common.strToJSONArray("ids", jsonlist);
            
            
            
            String str = "";
            final int n = files.length();
            for (int i = 0; i < n; ++i) {
              final JSONObject file = files.getJSONObject(i);
              
//                System.out.println("Device:"+deviceId+"\n\r"
//                                    +"Screen:"+file.getString("scr")+"\n\r"
//                                    +"Id:"+file.getLong("id")+"\n\r"
//                                    +"Dt:"+file.getString("dt"));

                dbapi.deleteSnap(userid, 
                                deviceId, 
                                Long.parseLong(file.getString("dt")), 
                                file.getString("scr"), 
                                file.getLong("id"));                
                System.out.println("Deleted Id:"+file.getLong("id"));

                File imageFile = new File(filePath + File.separator+file.getString("fn"));
                if (imageFile.exists()) {
                    System.out.println("Deleting:"+file.getString("fn"));
                    imageFile.delete();
                    
                }
                
                arr.put(file.getLong("id"));
                 
            }
            JSONObject obj = new JSONObject().put("DELETED", arr);
            common.printMessage(obj.toString());
            
            //common.respondMessageInfo("SYNC COMPLETE");

        }
        catch ( Exception e ){
            
            System.err.println("Exception in deleteds sync: " + e.getMessage() ); 
            common.respondMessageErr("ERROR_IN_SYNC_DELETEDS");
        }
   }


    
}
