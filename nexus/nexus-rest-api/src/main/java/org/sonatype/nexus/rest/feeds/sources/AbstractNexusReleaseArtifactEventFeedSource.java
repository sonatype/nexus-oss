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
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.feeds.NexusArtifactEvent;

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

        Collection<CRepository> repos = getNexus().listRepositories();

        for ( CRepository repo : repos )
        {
            if ( repo.getRepositoryPolicy().equals( "release" ) )
            {
                result.add( repo.getId() );
            }
        }

        return result;
    }
}
