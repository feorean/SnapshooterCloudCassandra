package SnapshooterCloud;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import javax.imageio.*;
import java.awt.image.*;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.apache.commons.io.FilenameUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.annotation.*;
import java.util.Enumeration;
import org.json.JSONObject;



/**
 *
 * @author Khalid
 */
@MultipartConfig
public class uploadfile extends HttpServlet {


    private String filePath;    
    private Common common;
    private Dbapi dbapi;
    private Boolean _ifsnaptableexists;

    private Boolean ifSnapTableExists(String userid) {
    
        if (_ifsnaptableexists == null || !_ifsnaptableexists) {
            
            _ifsnaptableexists = Boolean.valueOf( dbapi.checkIfTableExists("snapshots_"+userid) );
            
        }

        return _ifsnaptableexists;
        
    }
    
    
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
        
        try {
            
            //First check if authenticated  
            common.proceedIfAuthenticated();
            
            String deviceid = request.getParameter("deviceid");
            String screenName= request.getParameter("screenname"); 
            String userId = common.getCurrentSession().getAttribute("USERID").toString();
            String createDate = request.getParameter("createdate"); 
            String localId = request.getParameter("localid");
         
            
            if (deviceid == null || screenName == null || createDate == null || localId == null) {

              common.respondMessageErr("deviceid, screenName or createDate incorrect!"); 

              return;

            }  
            
           
            //Check if first time uploading
            if (!ifSnapTableExists(userId).booleanValue()) {

                dbapi.addUserSnapTable(userId);
                
            }
               
          
            Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
            String fileNameFull = filePart.getSubmittedFileName();            
            String fileExtention = FilenameUtils.getExtension(fileNameFull);
            String fileName = FilenameUtils.getBaseName(fileNameFull);
            
            if (fileName == null) {
                
                System.out.println("FileName is null");
                
                return;
            }
            
            if ( filePart.getSize() <= 0 ) {
                
                common.respondMessageErr("EMPTY_FILE");
                
                return;
            }
                     
            
            InputStream fileContent;   
            OutputStream targetFileStream = null;
    
            try {
                
                fileContent = filePart.getInputStream();
                targetFileStream = new FileOutputStream(new File(filePath + File.separator
                + fileNameFull));
                
                int read = 0;
                final byte[] bytes = new byte[1024]; 

                while ((read = fileContent.read(bytes)) != -1) {
                    targetFileStream.write(bytes, 0, read);
                }                           
                
            } catch (FileNotFoundException fne) {
                      System.out.println("<br/> ERROR: " + fne.getMessage());
                      common.respondMessageErr("FILE_NOT_FOUND");
            } finally {
                    if (targetFileStream != null) {
                        targetFileStream.close();
                    }
            }
            
            /* //Resize the image
            BufferedImage originalImage = ImageIO.read(new File(filePath + File.separator + fileNameFull));
            int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
			
            BufferedImage resizedImage = resizeImage(originalImage, originalImage.getWidth()/4, 
                                                                         originalImage.getHeight()/4, type);
            
            ImageIO.write(resizedImage, "jpg", new File(filePath + File.separator + fileName+"_XS." + fileExtention)); 
            */    
            

            boolean insertResult = dbapi.addSnapRecordToDB(Long.parseLong(createDate.substring(0,8)),
                                    Long.parseLong(createDate),
                                    fileNameFull, 
                                    filePart.getSize(),
                                    screenName,
                                    userId,
                                    deviceid,
                                    Long.parseLong(localId)
                                   );
            
            
            if (insertResult) {                
                //System.err.println("OK2");
                //common.respondMessageInfo("SUCCESSFULLY_UPLOADED:"+fileNameFull);          
                JSONObject obj = new JSONObject().put("SUCCESSFULLY_UPLOADED", fileNameFull);
                common.printMessage(obj.toString());
                //System.err.println("OK3");  
            } else {
                
                System.out.println("Not inserted!");
            }
        }
        catch ( Exception e ){
            
            System.err.println("Exception in upload snapshot: " + e.getMessage() ); 
            common.respondMessageErr("ERROR_IN_UPLOAD");
        }
   }


    private static BufferedImage resizeImage(BufferedImage originalImage, int imgWidth, int imgHeight, int type){
        
	BufferedImage resizedImage = new BufferedImage(imgWidth, imgHeight, type);
	Graphics2D g = resizedImage.createGraphics();
	g.drawImage(originalImage, 0, 0, imgWidth, imgHeight, null);
	g.dispose();
		
	return resizedImage;
    }
    
    
}
