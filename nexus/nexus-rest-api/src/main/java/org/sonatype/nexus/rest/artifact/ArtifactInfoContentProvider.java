package org.sonatype.nexus.rest.artifact;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.codehaus.plexus.classworlds.UrlUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.ArtifactViewProvider;
import org.sonatype.nexus.rest.model.ArtifactInfoResource;
import org.sonatype.nexus.rest.model.ArtifactInfoResourceResponse;

@Component( role = ArtifactViewProvider.class, hint = "artifactInfo" )
public class ArtifactInfoContentProvider
    implements ArtifactViewProvider
{

    @Requirement( role = RepositoryRegistry.class, hint = "protected" )
    private RepositoryRegistry repositoryRegistry;

    public Object retrieveView( ResourceStoreRequest req, StorageItem item )
        throws IOException, AccessDeniedException, NoSuchResourceStoreException, IllegalOperationException,
        ItemNotFoundException, StorageException, ResourceException
    {
        if ( !( item instanceof StorageFileItem ) )
        {
            return null;
        }

        Repository repo;
        repo = repositoryRegistry.getRepository( item.getRepositoryId() );

        Set<String> groups = new LinkedHashSet<String>();
        groups.add( item.getRepositoryId() );
        groups.addAll( repositoryRegistry.getGroupsOfRepository( item.getRepositoryId() ) );

        StorageFileItem fileItem = (StorageFileItem) item;

        ArtifactInfoResourceResponse result = new ArtifactInfoResourceResponse();

        ArtifactInfoResource resource = new ArtifactInfoResource();
        resource.setDigesterMd5( fileItem.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY ) );
        resource.setDigesterSha1( fileItem.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ) );
        try
        {
            URI uri = repo.getLocalStorage().getAbsoluteUrlFromBase( repo, req ).toURI();
            resource.setDiskPath( UrlUtils.normalizeUrlPath( uri.toString() ) );
        }
        catch ( URISyntaxException e )
        {
            throw new ResourceException( e );
        }
        resource.setLastChanged( fileItem.getModified() );
        resource.setRepositories( new ArrayList<String>( groups ) );
        resource.setSize( fileItem.getLength() );
        resource.setUploaded( fileItem.getCreated() );
        resource.setUploader( fileItem.getMimeType() );

        result.setData( resource );

        return result;
    }
}
