/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.artifact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchResponse;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractArtifactViewProvider;
import org.sonatype.nexus.rest.ArtifactViewProvider;
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException;
import org.sonatype.nexus.rest.model.ArtifactInfoResource;
import org.sonatype.nexus.rest.model.ArtifactInfoResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryUrlResource;
import org.sonatype.plexus.rest.ReferenceFactory;

/**
 * Artifact info view provider.
 * 
 * @author Velo
 * @author cstamas
 */
@Component( role = ArtifactViewProvider.class, hint = "info" )
public class InfoArtifactViewProvider
    extends AbstractArtifactViewProvider
{
    @Requirement
    private IndexerManager indexerManager;

    @Requirement( hint = "protected" )
    private RepositoryRegistry protectedRepositoryRegistry;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private ReferenceFactory referenceFactory;

    @Requirement
    private AccessManager accessManager;

    @Override
    protected Object retrieveView( ResourceStoreRequest request, RepositoryItemUid itemUid, StorageItem item,
                                   Request req )
        throws IOException
    {
        StorageFileItem fileItem = (StorageFileItem) item;

        Set<String> repositories = new LinkedHashSet<String>();

        // the artifact does exists on the repository it was found =D
        repositories.add( itemUid.getRepository().getId() );

        final String checksum =
            fileItem == null ? null : fileItem.getRepositoryItemAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );
        if ( checksum != null )
        {
            IteratorSearchResponse searchResponse = null;
            
            try
            {
                searchResponse =
                    indexerManager.searchArtifactSha1ChecksumIterator( checksum, null, null, null, null, null );

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
            finally
            {
                if ( searchResponse != null )
                {
                    searchResponse.close();
                }
            }
        }

        // hosted / cache check useful if the index is out to date or disable
        for ( Repository repo : protectedRepositoryRegistry.getRepositories() )
        {
            // already found the artifact on this repo
            if ( repositories.contains( repo.getId() ) )
            {
                continue;
            }

            final ResourceStoreRequest repoRequest =
                new ResourceStoreRequest( itemUid.getPath(), request.isRequestLocalOnly(),
                    request.isRequestRemoteOnly() );
            if ( repo.getLocalStorage().containsItem( repo, repoRequest ) )
            {
                try
                {
                    StorageItem repoItem = repo.retrieveItem( repoRequest );
                    if ( checksum == null
                        || checksum.equals( repoItem.getRepositoryItemAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ) ) )
                    {
                        repositories.add( repo.getId() );
                    }
                }
                catch ( AccessDeniedException e )
                {
                    // that is fine, user doesn't have access
                    continue;
                }
                catch ( Exception e )
                {
                    getLogger().error( e.getMessage(), e );
                }
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
            resource.setMd5Hash( fileItem.getRepositoryItemAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY ) );
            resource.setSha1Hash( checksum );
            resource.setLastChanged( fileItem.getModified() );
            resource.setSize( fileItem.getLength() );
            resource.setUploaded( fileItem.getCreated() );
            resource.setUploader( fileItem.getRepositoryItemAttributes().get( AccessManager.REQUEST_USER ) );
            resource.setMimeType( fileItem.getMimeType() );

            try
            {
                accessManager.decide( itemUid.getRepository(), request, Action.delete );
                resource.setCanDelete( true );
            }
            catch ( AccessDeniedException e )
            {
                resource.setCanDelete( false );
            }
        }

        result.setData( resource );

        return result;
    }

    /**
     * Here, we do want _real_ data: hashes, size, dates of link targets too, if any.
     * 
     * @return
     */
    @Override
    protected boolean dereferenceLinks()
    {
        return true;
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

            try
            {
                protectedRepositoryRegistry.getRepository( repositoryId );
                repoUrl.setCanView( true );
            }
            catch ( NoSuchRepositoryAccessException e )
            {
                // don't have view access, so won't see it!
                repoUrl.setCanView( false );
            }
            catch ( NoSuchRepositoryException e )
            {
                // completely unexpect, probably another thread removed this repo
                getLogger().error( e.getMessage(), e );
                continue;
            }

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
            repoUrl.setPath( path );

            urls.add( repoUrl );
        }
        return urls;
    }
}
