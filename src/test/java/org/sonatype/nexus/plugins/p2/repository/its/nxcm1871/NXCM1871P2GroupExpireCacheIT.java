/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1871;

import java.io.File;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ExpireCacheTaskDescriptor;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class NXCM1871P2GroupExpireCacheIT
    extends AbstractNexusProxyP2IT
{

    public NXCM1871P2GroupExpireCacheIT()
    {
        super( "nxcm1871" );
    }

    @Test
    public void test()
        throws Exception
    {
        // check original content
        final File f1 =
            downloadFile( new URL( getGroupUrl( getTestRepositoryId() ) + "/content.xml" ),
                "target/downloads/nxcm1871/1/content.xml" );
        Assert.assertTrue( f1.exists() );
        String c = FileUtils.fileRead( f1 );
        Assert.assertTrue( c.contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) );
        Assert.assertFalse( c.contains( "com.sonatype.nexus.p2.its.feature3.feature.jar" ) );

        final File repo_nxcm1871_2 = new File( localStorageDir, "nxcm1871-2" );

        final File newContentXml = new File( localStorageDir, "nxcm1871-3/content.xml" );
        Assert.assertTrue( newContentXml.exists() );
        c = FileUtils.fileRead( newContentXml );
        Assert.assertFalse( c.contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) );
        Assert.assertTrue( c.contains( "com.sonatype.nexus.p2.its.feature3.feature.jar" ) );

        FileUtils.copyFileToDirectory( newContentXml, repo_nxcm1871_2 );

        final File newArtifactsXml = new File( localStorageDir, "nxcm1871-3/artifacts.xml" );
        FileUtils.copyFileToDirectory( newArtifactsXml, repo_nxcm1871_2 );

        final ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
        prop.setValue( getTestRepositoryId() );

        TaskScheduleUtil.runTask( ExpireCacheTaskDescriptor.ID, prop );

        // make sure nexus has the right content after reindex
        final File f2 =
            downloadFile( new URL( getGroupUrl( getTestRepositoryId() ) + "/content.xml" ),
                "target/downloads/nxcm1871/2/content.xml" );
        Assert.assertTrue( f2.exists() );
        c = FileUtils.fileRead( f2 );
        Assert.assertFalse( c.contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) );
        Assert.assertTrue( c.contains( "com.sonatype.nexus.p2.its.feature3.feature.jar" ) );

        Assert.assertFalse( FileTestingUtils.compareFileSHA1s( f1, f2 ) );
    }

}
