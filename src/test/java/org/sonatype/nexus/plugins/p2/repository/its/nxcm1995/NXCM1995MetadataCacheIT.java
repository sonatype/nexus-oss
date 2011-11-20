/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1995;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.contains;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

import java.io.File;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.testng.annotations.Test;

public class NXCM1995MetadataCacheIT
    extends AbstractNexusProxyP2IT
{

    public NXCM1995MetadataCacheIT()
    {
        super( "nxcm1995" );
    }

    @Test( enabled = false )
    public void test()
        throws Exception
    {
        // check original content
        final File f1 = downloadFile(
            new URL( getNexusTestRepoUrl() + "/content.xml" ),
            "target/downloads/nxcm1995/1/content.xml"
        );
        assertThat( f1, exists() );
        assertThat( f1, contains( "com.adobe.flexbuilder.utils.osnative.win" ) );
        assertThat( f1, not( contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) ) );

        // check original artifact
        final File a1 = downloadFile(
            new URL( getNexusTestRepoUrl() + "/artifacts.xml" ),
            "target/downloads/nxcm1995/1/artifacts.xml"
        );
        assertThat( a1, exists() );
        assertThat( a1, contains( "com.adobe.flexbuilder.multisdk" ) );
        assertThat( a1, not( contains( "com.sonatype.nexus.p2.its.feature2" ) ) );

        final File reponxcm1995 = new File( localStorageDir, "nxcm1995" );

        // check new content
        final File newContentXml = new File( localStorageDir, "p2repo2/content.xml" );
        assertThat( newContentXml, exists() );
        assertThat( newContentXml, not( contains( "com.adobe.flexbuilder.utils.osnative.win" ) ) );
        assertThat( newContentXml, contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) );
        FileUtils.copyFileToDirectory( newContentXml, new File( reponxcm1995, "memberrepo1" ) );
        FileUtils.copyFileToDirectory( newContentXml, new File( reponxcm1995, "memberrepo2" ) );

        final File newArtifactsXml = new File( localStorageDir, "p2repo2/artifacts.xml" );
        assertThat( newArtifactsXml, exists() );
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
        final File f2 = downloadFile(
            new URL( getNexusTestRepoUrl() + "/content.xml" ),
            "target/downloads/nxcm1995/2/content.xml"
        );
        assertThat( f2, exists() );
        assertThat( f2, not( contains( "com.adobe.flexbuilder.utils.osnative.win" ) ) );
        assertThat( f2, contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) );

        assertThat( FileTestingUtils.compareFileSHA1s( f1, f2 ), is( false ) );

        // make sure nexus has the right content after metadata cache expires
        final File a2 = downloadFile(
            new URL( getNexusTestRepoUrl() + "/artifacts.xml" ),
            "target/downloads/nxcm1995/2/artifacts.xml"
        );
        assertThat( a2, exists() );
        assertThat( a2, not( contains( "com.adobe.flexbuilder.multisdk" ) ) );
        assertThat( a2, contains( "com.sonatype.nexus.p2.its.feature2" ) );

        assertThat( FileTestingUtils.compareFileSHA1s( a1, a2 ), is( false ) );
    }

}
