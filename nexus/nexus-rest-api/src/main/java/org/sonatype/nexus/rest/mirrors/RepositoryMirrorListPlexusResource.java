package org.sonatype.nexus.rest.mirrors;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.model.MirrorListResourceResponse;
import org.sonatype.nexus.rest.model.MirrorResourceRequest;
import org.sonatype.nexus.rest.model.MirrorResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryMirrorListPlexusResource" )
public class RepositoryMirrorListPlexusResource
    extends AbstractRepositoryMirrorPlexusResource
{
    public RepositoryMirrorListPlexusResource()
    {
        setModifiable( true );
    }
    
    @Override
    //TODO: define payload object
    public Object getPayloadInstance()
    {
        return new MirrorResourceRequest();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repository_mirrors/*", "authcBasic,perms[nexus:repositorymirrors]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/repository_mirrors/{" + REPOSITORY_ID_KEY + "}";
    }
    
    @Override
    //TODO: return mirrors in a rest object
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        MirrorListResourceResponse dto  = new MirrorListResourceResponse();
        
        try
        {
            CRepository repository = getNexus().readRepository( getRepositoryId( request ) );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid repository id " + getRepositoryId( request ), e);
        }
        
        return dto;
    }
    
    @Override
    //TODO: get url from rest object and update repository
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        MirrorResourceResponse dto = new MirrorResourceResponse();
        
        try
        {
            CRepository repository = getNexus().readRepository( getRepositoryId( request ) );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid repository id " + getRepositoryId( request ), e);
        }
        
        return dto;
    }
}
