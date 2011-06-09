/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1995;

import java.io.File;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IntegrationIT;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class NXCM1995MetadataCacheIT
    extends AbstractNexusProxyP2IntegrationIT
{

    public NXCM1995MetadataCacheIT()
    {
        super( "nxcm1995" );
    }

    @Ignore
    @Test
    public void test()
        throws Exception
    {
        // check original content
        final File f1 =
            downloadFile( new URL( getNexusTestRepoUrl() + "/content.xml" ), "target/downloads/nxcm1995/1/content.xml" );
        Assert.assertTrue( f1.exists() );
        String c = FileUtils.fileRead( f1 );
        Assert.assertTrue( c.contains( "com.adobe.flexbuilder.utils.osnative.win" ) );
        Assert.assertFalse( c.contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) );

        // check original artifact
        final File a1 =
            downloadFile( new URL( getNexusTestRepoUrl() + "/artifacts.xml" ),
                "target/downloads/nxcm1995/1/artifacts.xml" );
        Assert.assertTrue( a1.exists() );
        String a = FileUtils.fileRead( a1 );
        Assert.assertTrue( a.contains( "com.adobe.flexbuilder.multisdk" ) );
        Assert.assertFalse( a.contains( "com.sonatype.nexus.p2.its.feature2" ) );

        final File reponxcm1995 = new File( localStorageDir, "nxcm1995" );

        // check new content
        final File newContentXml = new File( localStorageDir, "p2repo2/content.xml" );
        Assert.assertTrue( newContentXml.exists() );
        c = FileUtils.fileRead( newContentXml );
        Assert.assertFalse( c.contains( "com.adobe.flexbuilder.utils.osnative.win" ) );
        Assert.assertTrue( c.contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) );
        FileUtils.copyFileToDirectory( newContentXml, new File( reponxcm1995, "memberrepo1" ) );
        FileUtils.copyFileToDirectory( newContentXml, new File( reponxcm1995, "memberrepo2" ) );

        final File newArtifactsXml = new File( localStorageDir, "p2repo2/artifacts.xml" );
        Assert.assertTrue( newArtifactsXml.exists() );
        FileUtils.copyFileToDirectory( newArtifactsXml, new File( reponxcm1995, "memberrepo1" ) );
        FileUtils.copyFileToDirectory( newArtifactsXml, new File( reponxcm1995, "memberrepo2" ) );

        // metadata cache expires in ONE minute, so let's give it some time to expire
        Thread.yield();
        Thread.sleep( 1 * 60 * 1000 );
        Thread.yield();
        Thread.sleep( 1 * 60 * 1000 );
        Thread.yield();

        // ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        // prop.setId( "repositoryId" );
        // prop.setValue( REPO );
        // TaskScheduleUtil.runTask( ExpireCacheTaskDescriptor.ID, prop );
        // TaskScheduleUtil.waitForAllTasksToStop();

        // make sure nexus has the right content after metadata cache expires
        final File f2 =
            downloadFile( new URL( getNexusTestRepoUrl() + "/content.xml" ), "target/downloads/nxcm1995/2/content.xml" );
        Assert.assertTrue( f2.exists() );
        c = FileUtils.fileRead( f2 );
        Assert.assertFalse( c.contains( "com.adobe.flexbuilder.utils.osnative.win" ) );
        Assert.assertTrue( c.contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) );

        Assert.assertFalse( FileTestingUtils.compareFileSHA1s( f1, f2 ) );

        // make sure nexus has the right content after metadata cache expires
        final File a2 =
            downloadFile( new URL( getNexusTestRepoUrl() + "/artifacts.xml" ),
                "target/downloads/nxcm1995/2/artifacts.xml" );
        Assert.assertTrue( a2.exists() );
        a = FileUtils.fileRead( a2 );
        Assert.assertFalse( a.contains( "com.adobe.flexbuilder.multisdk" ) );
        Assert.assertTrue( a.contains( "com.sonatype.nexus.p2.its.feature2" ) );

        Assert.assertFalse( FileTestingUtils.compareFileSHA1s( a1, a2 ) );
    }

}
