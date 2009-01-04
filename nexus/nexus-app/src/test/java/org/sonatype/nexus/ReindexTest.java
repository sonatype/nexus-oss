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

    protected void makeCentralPointTo( String url )
        throws Exception
    {
        CRepository repoConfig = defaultNexus.readRepository( "central" );

        // redirect it to our "sppof" jetty (see ReindexTest.xml in src/test/resources....
        repoConfig.getRemoteStorage().setUrl( url );

        // make the central download the remote indexes is found
        repoConfig.setDownloadRemoteIndexes( true );

        // update repo --> this will _NOT_ spawn one task doing reindex on central (coz we are modifying the same model
        // got from readRepo())
        defaultNexus.updateRepository( repoConfig );
    }

    protected void validateIndexWithIdentify( boolean shouldBePresent, String sha1Hash, String gid, String aid,
        String version )
        throws Exception
    {
        ArtifactInfo ai = defaultNexus.identifyArtifact( ArtifactInfo.SHA1, sha1Hash );

        if ( shouldBePresent )
        {
            assertNotNull( "Should find it!", ai );

            assertEquals( gid, ai.groupId );
            assertEquals( aid, ai.artifactId );
            assertEquals( version, ai.version );
        }
        else
        {
            assertNull( "Should not find it!", ai );
        }
    }

    public void testHostedRepositoryReindex()
        throws Exception
    {
        fillInRepo();

        defaultNexus.reindexRepository( null, "releases" );

        validateIndexWithIdentify(
            true,
            "86e12071021fa0be4ec809d4d2e08f07b80d4877",
            "org.sonatype.nexus",
            "nexus-indexer",
            "1.0-beta-4" );
    }

    public void testProxyRepositoryReindex()
        throws Exception
    {
        fillInRepo();

        makeCentralPointTo( "http://localhost:12345/central/" );

        defaultNexus.reindexRepository( null, "central" );

        validateIndexWithIdentify( true, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
    }

    public void testGroupReindex()
        throws Exception
    {
        fillInRepo();

        makeCentralPointTo( "http://localhost:12345/central/" );

        // central is member of public group
        defaultNexus.reindexRepositoryGroup( null, "public" );

        validateIndexWithIdentify( true, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
    }

    // XXX
    // https://grid.sonatype.org/ci/view/Nexus/job/Nexus/515/
    // this test is broken by indexer changes
    // fix it and enable it
    public void SKIPtestIncrementalIndexes()
        throws Exception
    {
        // day 1
        makeCentralPointTo( "http://localhost:12345/central-inc1/" );

        defaultNexus.reindexRepository( null, "central" );

        // validation
        validateIndexWithIdentify( true, "cf4f67dae5df4f9932ae7810f4548ef3e14dd35e", "antlr", "antlr", "2.7.6" );
        validateIndexWithIdentify( false, "83cd2cd674a217ade95a4bb83a8a14f351f48bd0", "antlr", "antlr", "2.7.7" );

        validateIndexWithIdentify( true, "3640dd71069d7986c9a14d333519216f4ca5c094", "log4j", "log4j", "1.2.8" );
        validateIndexWithIdentify( false, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
        validateIndexWithIdentify( false, "f0a0d2e29ed910808c33135a3a5a51bba6358f7b", "log4j", "log4j", "1.2.15" );

        // day 2
        makeCentralPointTo( "http://localhost:12345/central-inc2/" );

        defaultNexus.reindexRepository( null, "central" );

        // validation
        validateIndexWithIdentify( true, "cf4f67dae5df4f9932ae7810f4548ef3e14dd35e", "antlr", "antlr", "2.7.6" );
        validateIndexWithIdentify( true, "83cd2cd674a217ade95a4bb83a8a14f351f48bd0", "antlr", "antlr", "2.7.7" );

        validateIndexWithIdentify( true, "3640dd71069d7986c9a14d333519216f4ca5c094", "log4j", "log4j", "1.2.8" );
        validateIndexWithIdentify( true, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
        validateIndexWithIdentify( false, "f0a0d2e29ed910808c33135a3a5a51bba6358f7b", "log4j", "log4j", "1.2.15" );

        // day 3
        makeCentralPointTo( "http://localhost:12345/central-inc3/" );

        defaultNexus.reindexRepository( null, "central" );

        // validation
        validateIndexWithIdentify( true, "cf4f67dae5df4f9932ae7810f4548ef3e14dd35e", "antlr", "antlr", "2.7.6" );
        validateIndexWithIdentify( true, "83cd2cd674a217ade95a4bb83a8a14f351f48bd0", "antlr", "antlr", "2.7.7" );

        validateIndexWithIdentify( true, "3640dd71069d7986c9a14d333519216f4ca5c094", "log4j", "log4j", "1.2.8" );
        validateIndexWithIdentify( true, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
        validateIndexWithIdentify( true, "f0a0d2e29ed910808c33135a3a5a51bba6358f7b", "log4j", "log4j", "1.2.15" );
    }

}
