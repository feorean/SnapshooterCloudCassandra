package SnapshooterCloud;

import java.io.IOException;  
import java.io.PrintWriter;  
  
import javax.servlet.ServletException;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  
import javax.servlet.http.HttpSession;  


public class disconnect extends HttpServlet {  
    
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)  
                    throws ServletException, IOException {  
  
        
        Common common = new Common(request, response);
        
        HttpSession session = common.getCurrentSession();

        if( session != null ){  
            
            session.invalidate();

            if(session!=null) {
                common.respondMessageInfo("DISCONNECTED");  
            }
                     
        } else {
            common.respondMessageErr("NOT_CONNECTED");
        }
        
        
        //out.close();
        
        
    }  
}  
