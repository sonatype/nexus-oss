package org.sonatype.nexus.plugins.events.api;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.events.EventInspectorHost;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "EventInspectorsPlexusResource" )
public class EventInspectorsPlexusResource
    extends AbstractPlexusResource
{
    private static final String RESOURCE_URI = "/eventInspectors/isCalmPeriod";

    @Requirement
    private EventInspectorHost eventInspectorHost;

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "anon" );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        if ( eventInspectorHost.isCalmPeriod() )
        {
            response.setStatus( Status.SUCCESS_OK );
            return "Ok";
        }
        else
        {
            response.setStatus( Status.SUCCESS_ACCEPTED );
            return "Still munching on them...";
        }
    }

}
