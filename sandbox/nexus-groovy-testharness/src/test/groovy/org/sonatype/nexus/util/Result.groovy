package org.sonatype.nexus.util



/**
 * @author velo
 */
public class Result {

	def code;

	def content;

	void setConnection(connection) {
		code = connection.responseCode;

		try {
    		content = new XmlSlurper().parseText( connection.content.text )
    	} catch (e) {
    		content = null;
    	}
	}

}