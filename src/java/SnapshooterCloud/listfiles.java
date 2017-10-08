package SnapshooterCloud;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Khalid
 */

import java.io.IOException;  

  
import javax.servlet.ServletException;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  


import com.datastax.driver.core.*;
import javax.naming.AuthenticationException;


public class listfiles extends HttpServlet {  
    
    //Global variables

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)  
                    throws ServletException, IOException {  
        
        common = new Common(request, response);

        try {
            
            common.proceedIfAuthenticated();

            String userId = common.getCurrentSession().getAttribute("USERID").toString();
            
            String dateFrom = request.getParameter("datefrom");
            String dateTo = request.getParameter("dateto");
            String screenName = request.getParameter("screenname");            
            String deviceId = request.getParameter("deviceid");
            String fromId =  common.nvl(request.getParameter("fromid"), "0");
            String limit = common.nvl(request.getParameter("limit"), "20");
            
            System.out.println("fromId="+fromId);
            System.out.println("limit="+limit); 
            
            if ((dateFrom==null) || (dateTo==null) || (screenName==null) || (userId==null) || (deviceId==null) ) {
                
                System.out.println("One of the values is null");
                System.out.println("dateFrom="+dateFrom);
                System.out.println("dateTo="+dateTo);
                System.out.println("screenName="+screenName);
                System.out.println("userId="+userId);
                System.out.println("deviceId="+deviceId);
                System.out.println("fromId="+fromId); 
                System.out.println("limit="+limit);                 

                return;
            }
           
            ResultSet results = dbapi.getFileList(Long.valueOf(dateFrom), 
                                                  Long.valueOf(dateTo), 
                                                  screenName, 
                                                  userId, 
                                                  deviceId,
                                                  fromId,
                                                  limit);
            
            //System.out.println("test3");            
            common.printMessage( common.wrapArrayToJSON("Records", results) );
            
            //System.out.println("test2");
            /*if (fileName == null ) {
                
                common.respondMessageErr("FILE_NAME_INCORRECT");
                
            }*/
          
        }
        /*catch (FileNotFoundException fne) {
            
            common.respondMessageErr("FILE_NOT_FOUND");
                      
        } */
        catch ( AuthenticationException e ){
            
            System.err.println("Exception in getting file list: " + e.getMessage()); 
            
        } 
             
    }  
}  

