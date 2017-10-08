package SnapshooterCloud;

import java.io.IOException;  
import java.io.PrintWriter; 
import java.io.*;
  
import javax.servlet.ServletException;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  
import javax.servlet.http.HttpSession;  

import java.util.Enumeration;

import com.datastax.driver.core.*;
import javax.naming.AuthenticationException;

public class downloadSnapshot extends HttpServlet {  
    
    //Global variables

    private String filePath;
    private File file ;
    private String fileName;
    private Common common;
    private Dbapi dbapi;    

    public void init( ){
        // Get the file location where it would be stored.
        filePath = 
             getServletContext().getInitParameter("store_location"); 
        
        String clusterAddress = 
               getServletContext().getInitParameter("clusterAddress");


        dbapi = new Dbapi(clusterAddress); 
      
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)  
                    throws ServletException, IOException {  
        
        common = new Common(request, response);

//        Enumeration<String> headerNames = request.getHeaderNames();
//
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            
//            //System.out.print("n");
//            
//            String headerValue = "";
//            
//            Enumeration<String> headers = request.getHeaders(headerName);
//            while (headers.hasMoreElements()) {
//                 headerValue = headerValue +" " + headers.nextElement();
//                //System.out.print( headerValue);
//                //System.out.print("n");
//            }
//            
//            System.out.print(headerName + ":" + headerValue);
//        }
                
        
        
        try {
            
            common.proceedIfAuthenticated();

            String userId = common.getCurrentSession().getAttribute("USERID").toString();

            fileName = request.getParameter("filename");

     
            if ( (fileName == "") || (fileName == null) ) {
                
                common.respondMessageErr("FILE_NAME_INCORRECT");
                return;
            }
          
            File imageFile = new File(filePath + File.separator
                + fileName);
            
            if ( !imageFile.exists() ) {
                
                throw new FileNotFoundException();
                
            }
           
            common.setContentType("image/png");
            
            OutputStream targetFileStream = response.getOutputStream();
           
            FileInputStream sourceFileStream = new FileInputStream(imageFile);
            
            byte[] buffer = new byte[4096];
            int length;
            while ((length = sourceFileStream.read(buffer)) > 0){
                targetFileStream.write(buffer, 0, length);
            }
            sourceFileStream.close();
            targetFileStream.flush();
            
          
        }
        catch (FileNotFoundException fne) {
            
            common.respondMessageErr("FILE_NOT_FOUND");
                      
        } 
        catch ( AuthenticationException | IOException e ){
            
            System.err.println("Exception in download snapshot: " + e.getMessage()); 
            
        }
             
    }  
}  
