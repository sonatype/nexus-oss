package org.sonatype.nexus.rest.artifact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
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

    public Object retrieveView( StorageItem item, Request req )
        throws IOException
    {
        StorageFileItem fileItem;
        if ( item instanceof StorageFileItem )
        {
            fileItem = (StorageFileItem) item;
        }
        else if ( item instanceof StorageLinkItem )
        {
            RepositoryItemUid itemUid = ( (StorageLinkItem) item ).getTarget();
            try
            {
                final StorageItem retrieveItem =
                    itemUid.getRepository().retrieveItem( new ResourceStoreRequest( itemUid.getPath(), true, false ) );
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
                getLogger().error( "Failed to resolve the storagelink" + itemUid, e );
                return null;
            }
        }
        else
        {
            return null;
        }

        Set<String> repositories = new LinkedHashSet<String>();
        try
        {
            IteratorSearchResponse searchResponse =
                indexerManager.searchArtifactSha1ChecksumIterator(
                    fileItem.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ), null, null, null, null,
                    null );

            for ( Iterator<ArtifactInfo> iterator = searchResponse.iterator(); iterator.hasNext(); )
            {
                ArtifactInfo info = iterator.next();
                repositories.add( info.repository );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            // should never trigger this exception since I'm searching on all repositories
            getLogger().error( e.getMessage(), e );
        }

        RepositoryItemUid itemUid = item.getRepositoryItemUid();

        // hosted / cache check usefull if the index is out to date or disable
        for ( Repository repo : repositoryRegistry.getRepositories() )
        {
            if ( repo.getLocalStorage().containsItem( repo, new ResourceStoreRequest( itemUid.getPath(), true, false ) ) )
            {
                repositories.add( repo.getId() );
            }
        }

        ArtifactInfoResourceResponse result = new ArtifactInfoResourceResponse();

        ArtifactInfoResource resource = new ArtifactInfoResource();
        resource.setRepositoryId( itemUid.getRepository().getId() );
        resource.setRepositoryName( itemUid.getRepository().getName() );
        resource.setRepositoryPath( itemUid.getPath() );
        resource.setMd5Hash( fileItem.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY ) );
        resource.setSha1Hash( fileItem.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ) );
        resource.setLastChanged( fileItem.getModified() );
        resource.setRepositories( createRepositoriesUrl( repositories, req, itemUid.getPath() ) );
        resource.setSize( fileItem.getLength() );
        resource.setUploaded( fileItem.getCreated() );
        resource.setUploader( fileItem.getAttributes().get( AccessManager.REQUEST_USER ) );
        resource.setMimeType( fileItem.getMimeType() );

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
