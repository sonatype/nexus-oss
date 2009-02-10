package org.sonatype.nexus.rest.mirrors;

import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.metadata.NexusRepositoryMetadataHandler;
import org.sonatype.nexus.repository.metadata.MetadataHandlerException;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMirrorMetadata;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryPredefinedMirrorListPlexusResource" )
public class RepositoryPredefinedMirrorListPlexusResource
    extends AbstractRepositoryMirrorPlexusResource
{
    @Requirement
    NexusRepositoryMetadataHandler repoMetadata;
    
    public RepositoryPredefinedMirrorListPlexusResource()
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
        return new PathProtectionDescriptor( "/repository_predefined_mirrors/*", "authcBasic,perms[nexus:repositorypredefinedmirrors]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/repository_predefined_mirrors/{" + REPOSITORY_ID_KEY + "}";
    }
    
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        MirrorResourceListResponse dto = new MirrorResourceListResponse();
    
        try
        {
            RepositoryMetadata metadata = repoMetadata.readRepositoryMetadata( getRepositoryId( request ) );
            
            for ( RepositoryMirrorMetadata mirror : ( List<RepositoryMirrorMetadata> ) metadata.getMirrors() )
            {
                MirrorResource resource = new MirrorResource();
                resource.setId( mirror.getId() );
                resource.setUrl( mirror.getUrl() );
                dto.addData( resource );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().error( "Unable to retrieve metadata", e );
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid repository ID", e );
        }
        catch ( MetadataHandlerException e )
        {
            getLogger().error( "Unable to retrieve metadata, returning no items", e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to retrieve metadata", e );
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Metadata handling error", e );
        }
        
        return dto;
    }

}
