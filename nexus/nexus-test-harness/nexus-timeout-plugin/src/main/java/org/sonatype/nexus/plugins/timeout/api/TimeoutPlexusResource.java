package org.sonatype.nexus.plugins.timeout.api;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "TimeoutPlexusResource" )
public class TimeoutPlexusResource
    extends AbstractPlexusResource
{        
    @Override
    public Object getPayloadInstance()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:status]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/timeout";
    }
    
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();
        
        int requestedTimeout = Integer.parseInt( form.getFirstValue( "timeout" ) );
		
		try
		{
		    Thread.sleep( 1000 * requestedTimeout );
		}
		catch ( Exception e )
		{
		}
		
		return null;
    }
}
