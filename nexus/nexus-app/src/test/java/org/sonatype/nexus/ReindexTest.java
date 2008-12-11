/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus;

import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

public class ReindexTest
    extends AbstractMavenRepoContentTests
{
    public void testRepositoryReindex()
        throws Exception
    {
        fillInRepo();

        try
        {
            defaultNexus.reindexRepository( null, "releases" );

            // nexus-indexer-1.0-beta-4.jar :: sha1 = 86e12071021fa0be4ec809d4d2e08f07b80d4877
            ArtifactInfo ai = defaultNexus.identifyArtifact(
                ArtifactInfo.SHA1,
                "86e12071021fa0be4ec809d4d2e08f07b80d4877" );

            assertNotNull( "Should find it!", ai );

            assertEquals( "org.sonatype.nexus", ai.groupId );
            assertEquals( "nexus-indexer", ai.artifactId );
            assertEquals( "1.0-beta-4", ai.version );
        }
        catch ( NoSuchRepositoryException e )
        {
            fail( "NoSuchRepositoryException reindexing repository" );
        }
    }
}
