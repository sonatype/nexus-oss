/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;

public class ReindexTest
    extends AbstractMavenRepoContentTests
{
    private ServletServer servletServer;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        servletServer = (ServletServer) lookup( ServletServer.class );

        servletServer.start();
    }

    protected void tearDown()
        throws Exception
    {
        servletServer.stop();

        super.tearDown();
    }

    protected void makeCentralDownloadRemoteIndex()
        throws Exception
    {
        CRepository repoConfig = defaultNexus.readRepository( "central" );

        // redirect it to our "sppof" jetty (see ReindexTest.xml in src/test/resources....
        repoConfig.getRemoteStorage().setUrl( "http://localhost:12345/central/" );

        // make the central download the remote indexes is found
        repoConfig.setDownloadRemoteIndexes( true );

        // update repo --> this will spawn one task doing reindex on central
        defaultNexus.updateRepository( repoConfig );
    }

    public void testHostedRepositoryReindex()
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

    public void testProxyRepositoryReindex()
        throws Exception
    {
        fillInRepo();

        try
        {
            makeCentralDownloadRemoteIndex();

            defaultNexus.reindexRepository( null, "central" );

            // should download index
            // log4j-1.2.12.jar :: sha1 = 057b8740427ee6d7b0b60792751356cad17dc0d9
            ArtifactInfo ai = defaultNexus.identifyArtifact(
                ArtifactInfo.SHA1,
                "057b8740427ee6d7b0b60792751356cad17dc0d9" );

            assertNotNull( "Should find it!", ai );

            assertEquals( "log4j", ai.groupId );
            assertEquals( "log4j", ai.artifactId );
            assertEquals( "1.2.12", ai.version );
        }
        catch ( NoSuchRepositoryException e )
        {
            fail( "NoSuchRepositoryException reindexing repository" );
        }
    }

    public void testGroupReindex()
        throws Exception
    {
        fillInRepo();

        try
        {
            makeCentralDownloadRemoteIndex();

            defaultNexus.reindexRepositoryGroup( null, "public" );

            // should download index
            // log4j-1.2.12.jar :: sha1 = 057b8740427ee6d7b0b60792751356cad17dc0d9
            ArtifactInfo ai = defaultNexus.identifyArtifact(
                ArtifactInfo.SHA1,
                "057b8740427ee6d7b0b60792751356cad17dc0d9" );

            assertNotNull( "Should find it!", ai );

            assertEquals( "log4j", ai.groupId );
            assertEquals( "log4j", ai.artifactId );
            assertEquals( "1.2.12", ai.version );
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            fail( "NoSuchRepositoryException reindexing repository" );
        }
    }

}
