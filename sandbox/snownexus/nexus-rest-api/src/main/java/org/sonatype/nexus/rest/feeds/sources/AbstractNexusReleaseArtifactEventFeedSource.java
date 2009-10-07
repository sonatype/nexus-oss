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
package org.sonatype.nexus.rest.feeds.sources;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.Repository;

public abstract class AbstractNexusReleaseArtifactEventFeedSource
    extends AbstractNexusItemEventFeedSource
{
    @Requirement( hint = "artifact" )
    private SyndEntryBuilder<NexusArtifactEvent> entryBuilder;

    @Override
    public SyndEntryBuilder<NexusArtifactEvent> getSyndEntryBuilder( NexusArtifactEvent event )
    {
        return entryBuilder;
    }

    protected Set<String> getRepoIdsFromParams( Map<String, String> params )
    {
        Set<String> result = new HashSet<String>();

        Collection<Repository> repos = getRepositoryRegistry().getRepositories();

        for ( Repository repo : repos )
        {
            // huh? release as policy exists for MavenRepository only?
            if ( repo.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
            {
                if ( RepositoryPolicy.RELEASE.equals( repo.adaptToFacet( MavenRepository.class ).getRepositoryPolicy() ) )
                {
                    result.add( repo.getId() );
                }
            }
        }

        return result;
    }
}
