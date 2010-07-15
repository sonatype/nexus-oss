package org.sonatype.nexus.rest.artifact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.restlet.data.Request;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.IteratorSearchResponse;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.router.RequestRoute;
import org.sonatype.nexus.rest.ArtifactViewProvider;
import org.sonatype.nexus.rest.model.ArtifactInfoResource;
import org.sonatype.nexus.rest.model.ArtifactInfoResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryUrlResource;
import org.sonatype.plexus.rest.ReferenceFactory;

@Component( role = ArtifactViewProvider.class, hint = "info" )
public class InfoArtifactViewProvider
    extends AbstractLogEnabled
    implements ArtifactViewProvider
{
    @Requirement
    private IndexerManager indexerManager;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private ReferenceFactory referenceFactory;
    
    @Requirement
    private RepositoryRouter repositoryRouter;

    public Object retrieveView( ResourceStore store, ResourceStoreRequest request, StorageItem item, Request req )
        throws IOException
    {
        RepositoryItemUid itemUid = null;

        StorageFileItem fileItem = null;

        if ( item == null )
        {
            if ( store instanceof RepositoryRouter )
            {
                RepositoryRouter repositoryRouter = (RepositoryRouter) store;
                // item is either not present or is not here yet (remote index)
                // the we can "simulate" what route would be used to get it, and just get info from the route
                RequestRoute route;

                try
                {
                    route = repositoryRouter.getRequestRouteForRequest( request );
                }
                catch ( ItemNotFoundException e )
                {
                    // this is thrown while getting routes for any path "outside" of legal ones is given
                    // like /content/foo/bar, since 2nd pathelem may be "repositories", "groups", "shadows", etc
                    // (depends on
                    // type of registered reposes)
                    return null;
                }

                // request would be processed by targeted repository
                Repository itemRepository = route.getTargetedRepository();

                // create an UID against that repository
                itemUid = itemRepository.createUid( route.getRepositoryPath() );
            }
            else if ( store instanceof Repository )
            {
                itemUid = ( (Repository) store ).createUid( request.getRequestPath() );
            }
            else
            {
                return null;
            }
        }
        else
        {
            itemUid = item.getRepositoryItemUid();

            if ( item instanceof StorageFileItem )
            {
                fileItem = (StorageFileItem) item;
            }
            else if ( item instanceof StorageLinkItem )
            {
                // TODO: we may have "deeper" links too! Implement this properly!
                try
                {
                    StorageItem retrieveItem =
                        repositoryRouter.dereferenceLink( (StorageLinkItem) item, request.isRequestLocalOnly(),
                            request.isRequestRemoteOnly() );

                    if ( retrieveItem instanceof StorageFileItem )
                    {
                        fileItem = (StorageFileItem) retrieveItem;
                    }
                    else
                    {
                        return null;
                    }
                }
                catch ( Exception e )
                {
                    getLogger().warn( "Failed to resolve the storagelink " + item.getRepositoryItemUid(), e );

                    return null;
                }
            }
            else
            {
                return null;
            }
        }

        Set<String> repositories = new LinkedHashSet<String>();

        if ( fileItem != null )
        {
            try
            {
                IteratorSearchResponse searchResponse =
                    indexerManager.searchArtifactSha1ChecksumIterator(
                        fileItem.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ), null, null, null,
                        null, null );

                for ( ArtifactInfo info : searchResponse )
                {
                    repositories.add( info.repository );
                }
            }
            catch ( NoSuchRepositoryException e )
            {
                // should never trigger this exception since I'm searching on all repositories
                getLogger().error( e.getMessage(), e );
            }
        }

        // hosted / cache check usefull if the index is out to date or disable
        for ( Repository repo : repositoryRegistry.getRepositories() )
        {
            if ( repo.getLocalStorage().containsItem(
                repo,
                new ResourceStoreRequest( itemUid.getPath(), request.isRequestLocalOnly(), request.isRequestLocalOnly() ) ) )
            {
                repositories.add( repo.getId() );
            }
        }

        ArtifactInfoResourceResponse result = new ArtifactInfoResourceResponse();

        ArtifactInfoResource resource = new ArtifactInfoResource();
        resource.setRepositoryId( itemUid.getRepository().getId() );
        resource.setRepositoryName( itemUid.getRepository().getName() );
        resource.setRepositoryPath( itemUid.getPath() );
        resource.setRepositories( createRepositoriesUrl( repositories, req, itemUid.getPath() ) );
        resource.setPresentLocally( fileItem != null );

        if ( fileItem != null )
        {
            resource.setMd5Hash( fileItem.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY ) );
            resource.setSha1Hash( fileItem.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ) );
            resource.setLastChanged( fileItem.getModified() );
            resource.setSize( fileItem.getLength() );
            resource.setUploaded( fileItem.getCreated() );
            resource.setUploader( fileItem.getAttributes().get( AccessManager.REQUEST_USER ) );
            resource.setMimeType( fileItem.getMimeType() );
        }

        result.setData( resource );

        return result;
    }

    private List<RepositoryUrlResource> createRepositoriesUrl( Set<String> repositories, Request req, String path )
    {
        if ( !path.startsWith( "/" ) )
        {
            path = "/" + path;
        }

        List<RepositoryUrlResource> urls = new ArrayList<RepositoryUrlResource>();
        for ( String repositoryId : repositories )
        {
            RepositoryUrlResource repoUrl = new RepositoryUrlResource();
            repoUrl.setRepositoryId( repositoryId );
            try
            {
                repoUrl.setRepositoryName( repositoryRegistry.getRepository( repositoryId ).getName() );
            }
            catch ( NoSuchRepositoryException e )
            {
                // should never happen;
                getLogger().error( e.getMessage(), e );
            }
            repoUrl.setArtifactUrl( referenceFactory.createReference( req,
                "content/repositories/" + repositoryId + path ).toString() );
            urls.add( repoUrl );
        }
        return urls;
    }
}
