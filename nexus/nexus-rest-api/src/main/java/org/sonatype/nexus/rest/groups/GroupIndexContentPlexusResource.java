/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.groups;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.ArtifactPackagingMapper;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.rest.AbstractIndexContentPlexusResource;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Group index content resource.
 *
 * @author dip
 */
@Component( role = PlexusResource.class, hint = "groupIndexResource" )
public class GroupIndexContentPlexusResource
    extends AbstractIndexContentPlexusResource
{
    public static final String GROUP_ID_KEY = "groupId";

    @Requirement
    private ArtifactPackagingMapper artifactPackagingMapper;

    @Override
    public String getResourceUri()
    {
        return "/repo_groups/{" + GROUP_ID_KEY + "}/index_content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repo_groups/*/index_content/**", "authcBasic,tgiperms" );
    }

    @Override
    protected IndexingContext getIndexingContext( Request request )
        throws ResourceException
    {
        try
        {
            String groupId = String.valueOf( request.getAttributes().get( GROUP_ID_KEY ) );

            // just to test availability, this will throw NoSuchRepository if there is none found
            getRepositoryRegistry().getRepositoryWithFacet( groupId, GroupRepository.class );

            return indexerManager.getRepositoryIndexContext( groupId );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
    }

    /**
     * Convert from ArtifactInfo to a NexusArtifact. Limited functionality, just enough to make index browsing work.
     */
    @Override
    protected NexusArtifact ai2Na( Request request, ArtifactInfo ai )
    {
        if ( ai == null )
        {
            return null;
        }

        NexusArtifact a = new NexusArtifact();

        try
        {
            String groupId = String.valueOf( request.getAttributes().get( GROUP_ID_KEY ) );

            // just to test availability, this will throw NoSuchRepository if there is none found
            getRepositoryRegistry().getRepositoryWithFacet( groupId, GroupRepository.class );

            IndexingContext indexingContext = indexerManager.getRepositoryIndexContext( groupId );

            Gav gav =
                new Gav( ai.groupId, ai.artifactId, ai.version, ai.classifier,
                         artifactPackagingMapper.getExtensionForPackaging( ai.packaging ), null, null, null,
                         VersionUtils.isSnapshot( ai.version ), false, null, false, null );

            Reference repoRoot =
                createRepositoryGroupReference( request, groupId, indexingContext.getGavCalculator().gavToPath( gav ) );

            a.setResourceURI( repoRoot.toString() );
        }
        catch ( NoSuchRepositoryException e )
        {
            return null;
        }
        catch ( IOException e )
        {
            return null;
        }

        a.setGroupId( ai.groupId );
        a.setArtifactId( ai.artifactId );
        a.setVersion( ai.version );
        a.setClassifier( ai.classifier );
        a.setPackaging( ai.packaging );
        a.setRepoId( ai.repository );
        a.setContextId( ai.context );

        return a;
    }

}
