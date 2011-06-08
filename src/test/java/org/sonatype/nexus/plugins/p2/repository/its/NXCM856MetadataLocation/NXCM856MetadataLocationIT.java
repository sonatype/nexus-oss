/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.NXCM856MetadataLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.maven.index.artifact.Gav;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IntegrationIT;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.tycho.p2.facade.internal.DefaultTychoRepositoryIndex;
import org.sonatype.tycho.p2.facade.internal.GAV;

public class NXCM856MetadataLocationIT
    extends AbstractNexusProxyP2IntegrationIT
{
    public NXCM856MetadataLocationIT()
    {
        super( "m2hosted" );
    }

    public static class TestTychoRepositoryIndex
        extends DefaultTychoRepositoryIndex
    {
        public TestTychoRepositoryIndex( final File indexFile )
            throws IOException
        {
            gavs = read( new FileInputStream( indexFile ) );
        }
    }

    @Test
    public void make()
        throws Exception
    {

        deploy( new File( getOverridableFile( "p2artifacts" ), "nexus-p2-its-bundle/pom.xml" ),
            "org.sonatype.nexus.plugins.it.p2.repository", // groupId
            "org.sonatype.nexus.plugins.p2.repository.its.bundle", // artifactId
            "1.0.0", // version
            "pom" // packaging
        );

        deploy( new File( getOverridableFile( "p2artifacts" ),
            "nexus-p2-its-bundle/org.sonatype.nexus.plugins.p2.repository.its.bundle_1.0.0.jar" ),
            "org.sonatype.nexus.plugins.it.p2.repository", // groupId
            "org.sonatype.nexus.plugins.p2.repository.its.bundle", // artifactId
            "1.0.0", // version
            "jar" // packaging
        );

        TaskScheduleUtil.waitForAllTasksToStop();

        final File tempFile = File.createTempFile( "p2-metadata", ".properties" );
        try
        {
            final URL url = new URL( getNexusTestRepoUrl() + DefaultTychoRepositoryIndex.INDEX_RELPATH );
            downloadFile( url, tempFile.getAbsolutePath() );

            final DefaultTychoRepositoryIndex index = new TestTychoRepositoryIndex( tempFile );

            final List<GAV> projectGAVs = index.getProjectGAVs();

            for ( final GAV gav : projectGAVs )
            {
                System.err.println( gav.toExternalForm() );
            }

            Assert.assertTrue( projectGAVs.contains( new GAV( "org.sonatype.nexus.plugins.it.p2.repository", // groupId
                "org.sonatype.nexus.plugins.p2.repository.its.bundle", // artifactId
                "1.0.0" // version
            ) ) );
        }
        finally
        {
            tempFile.delete();
        }
    }

    public void deploy( final File artifactFile, final String groupId, final String artifactId, final String version,
                        final String packaging )
        throws Exception
    {
        final Gav gav = GavUtil.newGav( groupId, artifactId, version, packaging );

        getDeployUtils().deployWithWagon( "http", getRepositoryUrl( "m2hosted" ), artifactFile,
            getRelitiveArtifactPath( gav ) );
    }

}
