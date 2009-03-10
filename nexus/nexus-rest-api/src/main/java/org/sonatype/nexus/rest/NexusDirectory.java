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
        StaticHeaderUtil.addResponseHeaders( response );
        
        return super.findTarget( request, response );
    }
}
