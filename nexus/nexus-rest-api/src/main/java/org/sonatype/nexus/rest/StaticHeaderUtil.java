package org.sonatype.nexus.rest;

import org.restlet.data.Form;
import org.restlet.data.Response;

public class StaticHeaderUtil
{
    public static void addResponseHeaders( Response response )
    {
        Form responseHeaders = (Form) response.getAttributes().get("org.restlet.http.headers");   
        
        if (responseHeaders == null)   
        {   
            responseHeaders = new Form();   
            response.getAttributes().put("org.restlet.http.headers", responseHeaders);   
        }   
          
        // Default cache for 30 days
        responseHeaders.add("Cache-Control", "max-age=2592000");
    }
}
