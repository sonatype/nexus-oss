package org.sonatype.nexus.rest.mirrors;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Mirror;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.MirrorStatusResource;
import org.sonatype.nexus.rest.model.MirrorStatusResourceListResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryMirrorStatusPlexusResource" )
public class RepositoryMirrorStatusPlexusResource
    extends AbstractRepositoryMirrorPlexusResource
{
    public RepositoryMirrorStatusPlexusResource()
    {
        setModifiable( false );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repository_mirrors_status/*", "authcBasic,perms[nexus:repositorymirrorsstatus]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/repository_mirrors_status/{" + REPOSITORY_ID_KEY + "}";
    }

    @Override
    //TODO: return status of the mirror
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        MirrorStatusResourceListResponse dto = new MirrorStatusResourceListResponse();
        
        try
        {
            Repository repository = getRepositoryRegistry().getRepository( getRepositoryId( request ) );
            
            if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
            {
                ProxyRepository px = repository.adaptToFacet( ProxyRepository.class );
                
                for ( Mirror mirror : px.getDownloadMirrors().getMirrors() )
                {
                    MirrorStatusResource resource = new MirrorStatusResource();
                    resource.setId( mirror.getId() );
                    resource.setUrl( mirror.getUrl() );
                    resource.setStatus( px.getDownloadMirrors().isBlacklisted( mirror ) ? "Blacklisted" : "Available" );
                    
                    dto.addData( resource );
                }
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Repository is invalid type" );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid repository id " + getRepositoryId( request ), e);
        }
        
        return dto;
    }
}
