package org.sonatype.nexus.rest;

import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Handler;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class NexusDirectory
    extends Directory
{
    public NexusDirectory( Context context, String rootUri )
    {
        super( context, rootUri );
    }
    
    @Override
    public Handler findTarget( Request request, Response response )
    {
        Form responseHeaders = (Form) response.getAttributes().get("org.restlet.http.headers");   
        
        if (responseHeaders == null)   
        {   
            responseHeaders = new Form();   
            response.getAttributes().put("org.restlet.http.headers", responseHeaders);   
        }   
          
        // Default cache for 1 week
        responseHeaders.add("Cache-Control", "max-age=604800");
        
        return super.findTarget( request, response );
    }
}
