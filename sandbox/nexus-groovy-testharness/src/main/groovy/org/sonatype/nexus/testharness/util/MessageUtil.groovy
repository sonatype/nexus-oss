/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
