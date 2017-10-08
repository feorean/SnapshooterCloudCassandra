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
import java.lang.reflect.InvocationTargetException;
  
import javax.servlet.ServletException;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  


import com.datastax.driver.core.*;
import java.util.ArrayList;
import java.util.List;
import javax.naming.AuthenticationException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.annotation.MultipartConfig;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;


import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

@MultipartConfig
public class validateandgethash extends HttpServlet {  
    
    //Global variables

    private Common common;
    private Dbapi dbapi;  
    private String drupalAddress;

    @Override
    public void init( ){
        // Get the file location where it would be stored.
  
        drupalAddress = 
               getServletContext().getInitParameter("drupalAddress");
        
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
            System.out.println(dbuser.getUsername());
            System.out.println(dbuser.getPassword());
            if (!dbapi.checkIfUserExists(dbuser.getUsername())) {
                
                throw new EntityNotFoundException();
            }   

            if (dbuser.getUsername() == null || dbuser.getPassword() == null ) {
                
                System.out.println("NOT_ALL_VALUES_SUPPLIED:");
                System.out.println("username:" + dbuser.getUsername());
                System.out.println("password:" + dbuser.getPassword());
                
                throw new NullPointerException();                
            }
            
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(drupalAddress + "/validateandgethash");  
            
            List <NameValuePair> nvps = new ArrayList <>();
            nvps.add(new BasicNameValuePair("username", dbuser.getUsername()));
            nvps.add(new BasicNameValuePair("password", dbuser.getPassword()));
            httppost.setEntity(new UrlEncodedFormEntity(nvps));
            
            System.out.println("Executing request: " + httppost.getRequestLine());
            CloseableHttpResponse httpResponse = httpclient.execute(httppost);
            
            String str = EntityUtils.toString(httpResponse.getEntity());
            common.printMessage(str);

        System.out.println(str);
            
          
        }

        catch ( EntityNotFoundException e ) {
            
            common.respondMessageErr("VALIDATION_FAILED");
            
        } 
        
        catch ( NullPointerException e ) {
            
            common.respondMessageErr("VALIDATION_FAILED");
        } 
        
        catch (Error e) {
            
            common.respondMessageErr("VALIDATION_FAILED");
            System.err.println(e.toString());
        }
             
    }  
}  

