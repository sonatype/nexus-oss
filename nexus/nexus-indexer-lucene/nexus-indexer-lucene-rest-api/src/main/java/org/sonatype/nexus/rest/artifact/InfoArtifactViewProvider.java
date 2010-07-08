package org.sonatype.nexus.rest.artifact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.IteratorSearchResponse;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.rest.ArtifactViewProvider;
import org.sonatype.nexus.rest.model.ArtifactInfoResource;
import org.sonatype.nexus.rest.model.ArtifactInfoResourceResponse;

@Component( role = ArtifactViewProvider.class, hint = "info" )
public class InfoArtifactViewProvider
    extends AbstractLogEnabled
    implements ArtifactViewProvider
{

    @Requirement
    private IndexerManager indexerManager;

    public Object retrieveView( StorageItem item )
        throws IOException
    {
        if ( !( item instanceof StorageFileItem ) )
        {
            return null;
        }

        StorageFileItem fileItem = (StorageFileItem) item;

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

        ArtifactInfoResourceResponse result = new ArtifactInfoResourceResponse();

        ArtifactInfoResource resource = new ArtifactInfoResource();
        resource.setRepositoryId( item.getRepositoryItemUid().getRepository().getId() );
        resource.setRepositoryName( item.getRepositoryItemUid().getRepository().getName() );
        resource.setRepositoryPath( item.getRepositoryItemUid().getPath() );
        resource.setMd5Hash( fileItem.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY ) );
        resource.setSha1Hash( fileItem.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ) );
        resource.setLastChanged( fileItem.getModified() );
        resource.setRepositories( new ArrayList<String>( repositories ) );
        resource.setSize( fileItem.getLength() );
        resource.setUploaded( fileItem.getCreated() );
        resource.setUploader( fileItem.getAttributes().get( AccessManager.REQUEST_USER ) );
        resource.setMimeType( fileItem.getMimeType() );

        result.setData( resource );

        return result;
    }
}
