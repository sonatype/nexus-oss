package org.sonatype.nexus.rest.mirrors;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CMirror;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.model.MirrorResourceRequest;
import org.sonatype.nexus.rest.model.MirrorResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryMirrorPlexusResource" )
public class RepositoryMirrorPlexusResource
    extends AbstractRepositoryMirrorPlexusResource
{
    public RepositoryMirrorPlexusResource()
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
        return new PathProtectionDescriptor( "/repository_mirrors/*/*", "authcBasic,perms[nexus:repositorymirrors]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/repository_mirrors/{" + REPOSITORY_ID_KEY + "}/{" + MIRROR_ID_KEY + "}";
    }

    @Override
    //TODO: return mirror in a rest object
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        MirrorResourceResponse dto = new MirrorResourceResponse();
        
        try
        {
            dto.setData( 
                nexusToRestModel( 
                    getNexus().readMirror( 
                        getRepositoryId( request ), 
                        getMirrorId( request ) ) ) );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid repository id " + getRepositoryId( request ), e);
        }
        
        return dto;
    }
    
    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        MirrorResourceResponse dto = new MirrorResourceResponse();
        
        try
        {
            CMirror mirror = restToNexusModel( ( ( MirrorResourceRequest ) payload ).getData(), getNexus().readMirror( getRepositoryId( request ), getMirrorId( request ) ) );
            
            getNexus().updateMirror( 
                getRepositoryId( request ), 
                mirror );
            
            dto.setData( nexusToRestModel( mirror ) );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid repository id " + getRepositoryId( request ), e);
        }
        catch ( ConfigurationException e )
        {
            handleConfigurationException( e );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to update mirror", e );
        }
        
        return dto;
    }
    
    @Override
    //TODO: remove mirror and update repository
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            getNexus().deleteMirror( getRepositoryId( request ), getMirrorId( request ) );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid repository id " + getRepositoryId( request ), e);
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to delete mirror", e );
        }
    }
}
