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


public class listdevices extends HttpServlet {  
    
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

            System.out.println("userid=" + userId);
            ResultSet results = dbapi.getListOfDevices(userId);
            
                      
            common.printMessage( common.wrapArrayToJSON("Devices", results) );
            
          
        }

        catch ( AuthenticationException e ){
            
            System.err.println("Exception in getting device list: " + e.getMessage()); 
            
        } 
             
    }  
}  

