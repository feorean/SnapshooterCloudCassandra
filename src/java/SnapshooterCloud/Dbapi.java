package SnapshooterCloud;

import com.datastax.driver.core.*;
import java.math.BigInteger;
import javax.ws.rs.NotFoundException;
import java.util.UUID;
import javax.persistence.*;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Khalid
 */
public class Dbapi {
    
    private Cluster cluster;
    private Session dbsession;
    
   
    //Automatically create a session 
    public Dbapi(String clusterAddress) {
    
         // Connect to the cluster and keyspace "dbo"
        cluster = Cluster.builder().addContactPoint(clusterAddress).build();
        dbsession = cluster.connect("dbo"); 
    
    }
   
    public boolean checkIfUserExists(String username) {
     
        ResultSet results = dbsession.execute( "SELECT username FROM users where username='"+username+"' ");
        
        return !(results.isExhausted());
        
    }  
    
    public boolean checkIfTableExists(String tablename) {
        
        KeyspaceMetadata ks = cluster.getMetadata().getKeyspace("snaps");
        TableMetadata table = ks.getTable(tablename);
        
        return !(table == null);
        
    }  
    
    public void addUserSnapTable(String userid) throws Exception {
     
        String tablename = "snapshots_"+userid;
        
        if (!checkIfTableExists(tablename))  {  
        
                dbsession.execute( "CREATE TABLE snaps."+tablename+" ("
                                    +" createday varint, "
                                    +" createdatetime varint, "
                                    +" filename text, "
                                    +" filesize varint, "
                                    +" localid varint, "
                                    +" screenname text, "
                                    +" userid text, "
                                    +" deviceid text, "
                                    +" PRIMARY KEY ((deviceid, createday), screenname, localid, filename) "
                                    +" ) WITH CLUSTERING ORDER BY (screenname ASC, localid ASC, filename ASC)");
        
        }

        if (!checkIfTableExists(tablename))  {
        
            throw new Exception();
            
        }
       
    }     
    
    public User getDBUser(String username) throws NotFoundException {
     
        ResultSet results = dbsession.execute( "SELECT * FROM dbo.users where username='"+username+"'; ");
        
        User dbuser  = new User();
        
        if  (!results.isExhausted()) {

            //Get first row
            Row row = results.one();
            
            dbuser.setUsername(row.getString("username"));
            dbuser.setPassword(row.getString("password"));
            dbuser.setUserid(row.getString("userid"));
            
        } else {

            throw new NotFoundException();
        }

        
        return dbuser;
        
    }    
         
    
    public boolean addSnapRecordToDB(Long createday,
                                  Long createdatetime,
                                  String  filename,
                                  Long filesize,
                                  String  screenname,
                                  String  userid,
                                  String  deviceid,
                                  Long localid) {

            try {
               
                
                PreparedStatement statement = dbsession.prepare(

                        "INSERT INTO snaps.snapshots_"+userid+" (createday, createdatetime, filename, filesize,screenname, deviceid, localid)"
                                        + "VALUES (?,?,?,?,?,?,?);");

                BoundStatement boundStatement = new BoundStatement(statement);

                ResultSet rs = dbsession.execute(boundStatement.bind(BigInteger.valueOf(createday.longValue()),
                                                      BigInteger.valueOf(createdatetime.longValue()),
                                                      filename,
                                                      BigInteger.valueOf(filesize.longValue()),
                                                      screenname,
                                                      deviceid,
                                                      BigInteger.valueOf(localid.longValue()) ));             
                //return rs.one().getBool("[applied]");
                return true;
            }
            catch (Exception e){

                 System.err.println("Exception in insert a snapshot: " + e.getMessage());

                 throw e;
            }
        }
    
     public String getDatesInRange(Long dateFrom, Long dateTo)  {
         
         String tmpStr = "";

         if (dateFrom > dateTo) {
             
             System.out.println("Date_to can not be smaller than date_from");
         }
         
         for(Long i=dateFrom; i<=dateTo; i++){

             tmpStr = tmpStr+"," + String.valueOf(i);
         }
         //System.out.println("Dates:"+tmpStr.substring(1));
         return tmpStr.substring(1);
     }
    
     public ResultSet getFileList(Long      dateFrom,
                                  Long      dateTo,
                                  String    screenname,
                                  String    userid,
                                  String    deviceid,
                                  String    fromId,
                                  String    limit) {
     
        String dates =  getDatesInRange(dateFrom, dateTo);
        String idRange = "";
        
        //Check if number suuplied
        if ( fromId.matches("^[0-9]+$") && limit.matches("^[0-9]+$") ) {
        
            idRange = " and localid >"+fromId + " LIMIT "+limit; //+" and localid <= "+toId.toString();
                                      
        }
        
        String tmpSql = "select localid, filename, createdatetime, filesize from snaps.snapshots_"+userid+" where deviceid = '"+deviceid+"' " +
                                               " and screenname = '"+screenname+"' and createday IN ("+dates+")" + idRange;
        
        //System.out.println(tmpSql);
        
        ResultSet results = dbsession.execute(tmpSql);
        
        return results;
        
    }  
    
     
    public boolean checkIfDeviceScreenExists(String userid, String deviceid, String screen)  throws NotFoundException {
     
        if (userid == null || deviceid == null || screen == null) {
            
            return false;
        }
        
        ResultSet results = dbsession.execute( "select * from dbo.devices"
                                               + " where userid ='"+userid+"'"
                                               + " and deviceid ='"+deviceid+"'"
                                               + " and   screen ='"+screen+"' ; ");
        
        return !results.isExhausted();
    }
            
    public ResultSet getListOfDevices(String userid)  throws NotFoundException {
     
        if (userid == null) {
            
            return null;
        }
        
        ResultSet results = dbsession.execute( "select deviceid, devicetype, screen from dbo.devices"
                                               + " where userid ='"+userid+"' ; ");
        
        return results;
    }    
    
    public boolean addNewDevice(String userid, String deviceid, String devicetype, String screen) {
        
        if (userid == null || deviceid == null || devicetype == null || screen == null) {
            
            return false;
        }        
        
        try {

                PreparedStatement statement = dbsession.prepare(

                        "INSERT INTO dbo.devices (userid, deviceid, devicetype, screen)"
                                        + "VALUES (?,?,?,?);");

                BoundStatement boundStatement = new BoundStatement(statement);

                ResultSet rs = dbsession.execute(boundStatement.bind(userid, deviceid, devicetype, screen));             

                return true;
                
            }
            catch (Exception e){

                 System.err.println("Exception in device insert: " + e.getMessage());

                 throw e;
            }        
        
    }
    
    public String addUserToDb (User dbuser, String updateStatus) throws EntityExistsException {
        
        String userid = dbuser.getUserid()==null?String.valueOf(UUID.randomUUID()):dbuser.getUserid();        
        
/*        System.out.println(userid);
        System.out.println(dbuser.getUsername());
        System.out.println(dbuser.getPassword());        
*/        
        if (!checkIfUserExists(dbuser.getUsername()) && updateStatus.equals("FALSE")) {
        
            PreparedStatement statement = dbsession.prepare(

                            "INSERT INTO dbo.users (userid, username, password) "
                                            + "VALUES (?,?,?);");

            BoundStatement boundStatement = new BoundStatement(statement);

            ResultSet rs = dbsession.execute(boundStatement.bind(userid, dbuser.getUsername(), dbuser.getPassword()));
                    
        } else if (checkIfUserExists(dbuser.getUsername()) && updateStatus.equals("TRUE") ) {
        
                    PreparedStatement statement = dbsession.prepare(

                                    "update dbo.users set password = ? "
                                                    + "where userid = ? and username = ?");

                    BoundStatement boundStatement = new BoundStatement(statement);

                    ResultSet rs = dbsession.execute(boundStatement.bind(dbuser.getPassword(), userid, dbuser.getUsername()));                    
                
                } else {

                    throw new EntityExistsException();
            }
         
        
        return userid;
    }

    
    public boolean deleteSnap(String  userid,
                                String  deviceid,
                                Long createday,
                                String  screenname,
                                Long localid) {

            try {
               
                
                PreparedStatement statement = dbsession.prepare(

                        "delete from snaps.snapshots_"+userid+" "
                        +"where deviceid = ? and createday = ? and screenname = ? and localid = ?;");

                BoundStatement boundStatement = new BoundStatement(statement);

                ResultSet rs = dbsession.execute(boundStatement.bind(
                                                    deviceid,
                                                    BigInteger.valueOf(createday.longValue()),
                                                    screenname,
                                                    BigInteger.valueOf(localid.longValue()) ));             
                return true;
            }
            catch (Exception e){

                 System.err.println("Exception in snap delete: " + e.getMessage());

                 throw e;
            }
        }    
    
    
}
