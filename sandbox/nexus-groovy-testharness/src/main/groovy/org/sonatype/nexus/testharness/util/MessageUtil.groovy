package org.sonatype.nexus.testharness.util


/**
 * @author velo
 *
 */
public class MessageUtil{

    static def baseUrl = "http://localhost:8081/nexus/service/local/"
    
    def messageUrl;
    
    def messageIdUrl;
    
    Result doGet() {
        def url = (baseUrl + messageUrl).toURL(); 

 		def connection = url.openConnection();
 		connection.setRequestMethod("GET" );
        connection.connect();
        
        return new Result(connection:connection);	
    }

    Result doGet(String id) {
        def url = (baseUrl + messageIdUrl.replace("\$id", id)).toURL(); 

 		def connection = url.openConnection();
 		connection.setRequestMethod("GET" );
        connection.connect();
        
        return new Result(connection:connection);	
    }

    Result doPut(id, xml) {
        def url = (baseUrl + messageIdUrl.replace("\$id", id)).toURL(); 
 		def connection = url.openConnection()
 		connection.setRequestProperty("Content-Type", "application/xml" )
 		connection.setRequestMethod("PUT" )
        connection.doOutput = true
        Writer writer = new OutputStreamWriter(connection.outputStream)
        writer.write(xml)
        writer.flush()
        writer.close()
        connection.connect()	
        
        return new Result(connection:connection);
    }

    Result doDelete(id) {
        def url = (baseUrl + messageIdUrl.replace("\$id", id)).toURL(); 
 		def connection = url.openConnection()
 		connection.setRequestProperty("Content-Type", "application/xml" )
 		connection.setRequestMethod("DELETE" )
        connection.connect()	
        
        return new Result(connection:connection);
    }

	Result doPost(xml) {
        def url = ( baseUrl + messageUrl ).toURL()
 		def connection = url.openConnection()
 		connection.setRequestMethod("POST" )
 		connection.setRequestProperty("Content-Type", "application/xml" )
        connection.doOutput = true
        Writer writer = new OutputStreamWriter(connection.outputStream)
        writer.write(xml)
        writer.flush()
        writer.close()
        connection.connect()	
        
        return new Result(connection:connection);
    }
	    
    
}
