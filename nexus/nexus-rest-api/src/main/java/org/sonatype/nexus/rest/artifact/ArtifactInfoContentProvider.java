package org.sonatype.nexus.rest.artifact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.rest.ArtifactViewProvider;
import org.sonatype.nexus.rest.model.ArtifactInfoResource;
import org.sonatype.nexus.rest.model.ArtifactInfoResourceResponse;

@Component( role = ArtifactViewProvider.class, hint = "artifactInfo" )
public class ArtifactInfoContentProvider
    implements ArtifactViewProvider
{
    @Requirement( role = RepositoryRegistry.class, hint = "protected" )
    private RepositoryRegistry repositoryRegistry;

    public Object retrieveView( StorageItem item )
        throws IOException
    {
        if ( !( item instanceof StorageFileItem ) )
        {
            return null;
        }

        // for "repositories" (added below as that):
        // notQuiteTrue #1: an artifact may be _duplicated_ in some other repository, this is neglecting that fact
        // notQuiteTrue #2: think about repository Routes
        // notQuitetrue #3: artifact accessed over direct repository URL is not listed here, and this is wrong
        Set<String> groups = new LinkedHashSet<String>();
        groups.add( item.getRepositoryId() );
        groups.addAll( repositoryRegistry.getGroupsOfRepository( item.getRepositoryId() ) );

        StorageFileItem fileItem = (StorageFileItem) item;

        ArtifactInfoResourceResponse result = new ArtifactInfoResourceResponse();

        ArtifactInfoResource resource = new ArtifactInfoResource();
        resource.setRepositoryId( item.getRepositoryItemUid().getRepository().getId() );
        resource.setRepositoryName( item.getRepositoryItemUid().getRepository().getName() );
        resource.setRepositoryPath( item.getRepositoryItemUid().getPath() );
        resource.setSha1Hash( fileItem.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ) );
        resource.setLastChanged( fileItem.getModified() );
        resource.setRepositories( new ArrayList<String>( groups ) );
        resource.setSize( fileItem.getLength() );
        resource.setUploaded( fileItem.getCreated() );
        resource.setUploader( fileItem.getAttributes().get( AccessManager.REQUEST_USER ) );
        resource.setMimeType( fileItem.getMimeType() );

        result.setData( resource );

        return result;
    }
}
