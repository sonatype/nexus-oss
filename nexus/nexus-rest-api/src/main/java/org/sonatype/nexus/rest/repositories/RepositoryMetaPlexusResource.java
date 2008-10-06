package org.sonatype.nexus.rest.repositories;

import java.util.logging.Level;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.cache.CacheStatistics;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.RepositoryMetaResource;
import org.sonatype.nexus.rest.model.RepositoryMetaResourceResponse;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component(role=PlexusResource.class, hint="RepositoryMetaPlexusResource")
public class RepositoryMetaPlexusResource
    extends AbstractRepositoryPlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/repositories/{" + REPOSITORY_ID_KEY + "}/meta";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String repoId = this.getRepositoryId( request );
        try
        {
            RepositoryMetaResource resource = new RepositoryMetaResource();

            Repository repository = getNexusInstance( request ).getRepository( repoId );

            String localPath = repository.getLocalUrl().substring( repository.getLocalUrl().indexOf( "file:" ) + 1 );

            // TODO: clean this up, ot at least centralize somewhere!
            // a stupid trick here
            try
            {
                CRepository model = getNexusInstance( request ).readRepository( repoId );

                resource.setRepoType( getRestRepoType( model ) );
                
                resource.setFormat( model.getType() );
            }
            catch ( NoSuchRepositoryException e )
            {
                CRepositoryShadow model = getNexusInstance( request ).readRepositoryShadow( repoId );

                resource.setRepoType( getRestRepoType( model ) );

                resource.setFormat( model.getType() );
            }

            resource.setId( repoId );
            
            try
            {
                resource.setSizeOnDisk( FileUtils.sizeOfDirectory( localPath ) );

                resource.setFileCountInRepository( org.sonatype.nexus.util.FileUtils.filesInDirectory( localPath ) );
            }
            catch ( IllegalArgumentException e )
            {
                // the repo is maybe virgin, so the dir is not created until some request needs it
            }

            // mustang is able to get this with File.getUsableFreeSpace();
            resource.setFreeSpaceOnDisk( -1 );

            CacheStatistics stats = repository.getNotFoundCache().getStatistics();

            resource.setNotFoundCacheSize( stats.getSize() );

            resource.setNotFoundCacheHits( stats.getHits() );

            resource.setNotFoundCacheMisses( stats.getMisses() );

            resource.setLocalStorageErrorsCount( 0 );

            resource.setRemoteStorageErrorsCount( 0 );

            RepositoryMetaResourceResponse result = new RepositoryMetaResourceResponse();

            result.setData( resource );

            return result;
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Repository not found, id=" + repoId );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
        }
    }

}
