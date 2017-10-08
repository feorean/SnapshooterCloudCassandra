package SnapshooterCloud;

import java.io.IOException;  
import java.io.PrintWriter;  
  
import javax.servlet.ServletException;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  
import javax.servlet.http.HttpSession;  
import com.datastax.driver.core.*;
import javax.servlet.annotation.MultipartConfig;
import javax.ws.rs.NotFoundException;
import org.json.JSONObject;

@MultipartConfig
public class authenticate extends HttpServlet {  
    
    //Global variables
    Dbapi  dbapi;
    Common common;
    User dbuser;

    public void init() throws ServletException {
        
        String clusterAddress = 
               getServletContext().getInitParameter("clusterAddress");


        dbapi = new Dbapi(clusterAddress);    
               
    }
     
    protected void doPost(HttpServletRequest request, HttpServletResponse response)  
                    throws ServletException, IOException {  

        
        common = new Common(request, response);
        
        if( common.getCurrentSession() != null ){  
 
            common.respondMessageInfo("AUTHENTICATED");  
            
            //common.getCurrentSession().invalidate();
            return;
        } 
        
        
        String username=request.getParameter("username");  
        String password=request.getParameter("password");  
        
        System.out.println(username);
        System.out.println(password);
        
        if (username == null || password == null) {
            
           //common.respondMessageErr("USERNAME_PASSWORD_INCORRECT"); 
           return;
            
        }
        
        
        try {
                    
            dbuser = dbapi.getDBUser(username);
            
        } catch (NotFoundException nfe) {
            
            common.respondMessageErr("AUTHENTICATION_FAILED"); 
            return;
            
        }
                
                  
        if(password.equals(dbuser.getPassword()) ) {  
                              
            HttpSession session = common.createNewSession();
            
            if (session != null) {

                session.setAttribute("USERID", dbuser.getUserid());
                //common.respondMessageInfo("AUTHENTICATED");
                JSONObject obj = new JSONObject().put("AUTHENTICATED", "YES");
                common.printMessage(obj.toString());
            }
                    
        }  
        else{  
            
            common.respondMessageErr("PASSWORD_INCORRECT");  
            
        }  
        
        
        //finalizeRequest();
        
    }  
}  
