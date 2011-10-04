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
