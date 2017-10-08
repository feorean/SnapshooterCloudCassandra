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
import javax.persistence.EntityExistsException;
import javax.servlet.annotation.MultipartConfig;

@MultipartConfig
public class adddbuser extends HttpServlet {  
    
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)  
                    throws ServletException, IOException {  
        
        //System.out.println("Username:"+request.getParameter("username"));
        //System.out.println("Password:"+request.getParameter("password"));        
        
        common = new Common(request, response);

        
        try {
            
            User dbuser = new User();
            
            dbuser.setUsername(request.getParameter("username"));            
            dbuser.setPassword(request.getParameter("password"));            
            dbuser.setUserid(request.getParameter("userid"));//If "" then generate a new
            
            dbapi.addUserToDb(dbuser, request.getParameter("update"));
                      
            //common.printMessage( common.wrapArrayToJSON("Devices", results) );

        //System.out.println(userid);
            
          
        }

        catch ( EntityExistsException e ){
            
            common.respondMessageErr("USER_EXISTS");
            
        } 
             
    }  
}  

