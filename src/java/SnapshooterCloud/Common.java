package SnapshooterCloud;

import com.datastax.driver.core.*;
import java.io.PrintWriter;
import java.io.*;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.naming.AuthenticationException;

//import org.json.simple.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Khalid
 */

 
public class Common {
    
    //Declarations
    
    private HttpSession session;
    private HttpServletRequest  request;
    private HttpServletResponse response;
       
    public enum MessageType {        
        Information, Error        
    }
    
   
    public Common (HttpServletRequest inRequest, HttpServletResponse inResponse) {
        
        request = inRequest;
        response = inResponse;
        
        response.setContentType("text/html"); 
                
    }
    
    
    public void setContentType(String contentType) {
        
        response.setContentType(contentType);
    }
    
    
    public HttpSession getCurrentSession() {
        
        if ( session == null ) {
            
            session=request.getSession(false);
            
        }
        
        return session;
        
    }
    
    public HttpSession createNewSession() {
        
        session=request.getSession();  
        
        return session;
    }
    
    public static String nvl(String value, String alternateValue) {
    if (value == null)
        return alternateValue;

    return value;
    }
    
    private String wrapMessage(MessageType messageType, String message) {
        

        String tmp = null;
        String result = null;

        try {        
        //System.out.println("Message type:"+messageType+" "+message);
        switch (messageType) {
            case Information: 
                tmp = "INFO";
                break;
            case Error:
                tmp = "ERR";
                break;
            default: {
                System.out.println("Wrong message type:"+messageType+" "+message);
                break; 
            }
        }

        if (tmp != null) {
            
            result = "{\""+tmp+"\":\""+nvl(message, " ")+"\"}";
            
        } else {
                       
            System.out.println("result is null"); 
            
        }

       }
        
        catch (NullPointerException e) {
            System.out.println("Got some nulllllllllllllllllllllllll222222222");
        }        
        
        return result;
    }
    
    public void respondMessage(MessageType messageType, String message) {

        PrintWriter out = null;
        
        try {
            
                out = response.getWriter();            
        
        }
        catch (IOException e) {
                
            System.err.println("IO Exception: " + e.getMessage());    
        }        
        
        try {
            if ((messageType != null) && (message != null ) && (out != null)) {

                out.println(wrapMessage(messageType, message));

            }
        }
        catch (NullPointerException e) {
            System.out.println("Got some nulllllllllllllllllllllllll");
        }
    }

    public void respondMessageInfo(String message) {
        
        respondMessage(MessageType.Information, message);
        
    }
    
    public void respondMessageErr(String message) {
        
        respondMessage(MessageType.Error, message);
        
    }
    

    public void printMessage(String message) {

        PrintWriter out = null;
        
        try {
            
                out = response.getWriter();            
        
        }
        catch (IOException e) {
                
            System.err.println("IO Exception: " + e.getMessage());    
        }        
        
        try {
            if ((message != null ) && (out != null)) {

                out.println( message);

            }
        }
        catch (NullPointerException e) {
            System.out.println("Got some nulllllllllllllllllllllllll");
        }
    }    
    
    //Authentication
    
    public void proceedIfAuthenticated() 
            throws AuthenticationException {
            
        if ( getCurrentSession() == null ) { 

            respondMessageErr("NOT_AUTHENTICATED");

            throw new AuthenticationException("NOT_AUTHENTICATED");

        }   
    
    }

    
    private String wrapJSONField(String fieldName, String value) {

        if (fieldName == null ) {

            return "";

        }

        String result  = "{\""+fieldName+"\":\""+nvl(value, " ")+"\"}";


        return result;
        
    }
    
    
    public String wrapArrayToJSON (String arrayName, ResultSet records) {

        if (arrayName == null) {
            
            System.out.println("Array name is empty!");
            return "";
            
        }
        
        if (records == null || records.isExhausted()) {
            
            System.out.println("Array is either null or empty!");
            return "";
            
        }
        
        JSONArray jsonArray = new JSONArray();
        
        //jsonArray.addAll(records);
        
        //int i = 0;
        while (!records.isExhausted()) { 

            //i++;

            Row row = records.one();

            //System.out.println(row.getString("filename"));
            //common.respondMessageInfo(row.getString("filename"));

            JSONObject obj=new JSONObject();
            
            Iterator<ColumnDefinitions.Definition> itr = row.getColumnDefinitions().iterator();
            while(itr.hasNext()) {
                
                ColumnDefinitions.Definition column = itr.next();
                
                if ( column.getType().getName() == DataType.Name.VARINT ) {
                    
                    obj.put(column.getName(), row.getVarint(column.getName()));
                    
                } else {
                    
                    obj.put(column.getName(), row.getString(column.getName()));
                    
                }
                
                //obj.put("fileName", row.getString("filename"));
                //obj.put("id", row.getVarint("localid"));

            }

            jsonArray.put(obj);              

        //(new JSONObject().put("fileName", row.getString("filename")));

        }
        
        JSONObject res=new JSONObject();
            res.put(arrayName, jsonArray);
        
        //System.out.print(obj);
    
        return res.toString();
    }
    
    public JSONArray strToJSONArray(String arrayName, String jsonData) {
        
        JSONObject obj = new JSONObject(jsonData);
        
        JSONArray array = obj.getJSONArray(arrayName);
        
        return array;
    }
    
    
}
