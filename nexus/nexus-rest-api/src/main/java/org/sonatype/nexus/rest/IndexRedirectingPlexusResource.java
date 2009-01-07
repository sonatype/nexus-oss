package org.sonatype.nexus.rest;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

/**
 * Resource to redirect to the absolute URI to the index.html.
 */
@Component(role=ManagedPlexusResource.class, hint="IndexRedirectingPlexusResource")
public class IndexRedirectingPlexusResource
    extends AbstractNexusPlexusResource implements ManagedPlexusResource
{

    @Requirement( hint = "indexTemplate" )
    private ManagedPlexusResource indexTemplateResource;
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {   
        response.redirectPermanent( createRootReference(request, indexTemplateResource.getResourceUri().replaceFirst( "/", "" ) ));
        
        return null;
    }

    
    
}
