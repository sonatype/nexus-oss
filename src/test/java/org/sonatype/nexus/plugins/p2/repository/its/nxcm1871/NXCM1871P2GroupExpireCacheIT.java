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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1871;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.contains;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

import java.io.File;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ExpireCacheTaskDescriptor;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.annotations.Test;

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
        final File f1 = downloadFile(
            new URL( getGroupUrl( getTestRepositoryId() ) + "/content.xml" ),
            "target/downloads/nxcm1871/1/content.xml"
        );
        assertThat( f1, exists() );
        assertThat( f1, contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) );
        assertThat( f1, not( contains( "com.sonatype.nexus.p2.its.feature3.feature.jar" ) ) );

        final File repo_nxcm1871_2 = new File( localStorageDir, "nxcm1871-2" );

        final File newContentXml = new File( localStorageDir, "nxcm1871-3/content.xml" );
        assertThat( newContentXml, exists() );
        assertThat( newContentXml, not( contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) ) );
        assertThat( newContentXml, contains( "com.sonatype.nexus.p2.its.feature3.feature.jar" ) );

        FileUtils.copyFileToDirectory( newContentXml, repo_nxcm1871_2 );

        final File newArtifactsXml = new File( localStorageDir, "nxcm1871-3/artifacts.xml" );
        FileUtils.copyFileToDirectory( newArtifactsXml, repo_nxcm1871_2 );

        final ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
        prop.setValue( getTestRepositoryId() );

        TaskScheduleUtil.runTask( ExpireCacheTaskDescriptor.ID, prop );

        // make sure nexus has the right content after reindex
        final File f2 = downloadFile(
            new URL( getGroupUrl( getTestRepositoryId() ) + "/content.xml" ),
            "target/downloads/nxcm1871/2/content.xml"
        );
        assertThat( f2, exists() );
        assertThat( f2, not( contains( "com.sonatype.nexus.p2.its.feature2.feature.jar" ) ) );
        assertThat( f2, contains( "com.sonatype.nexus.p2.its.feature3.feature.jar" ) );

        assertThat( FileTestingUtils.compareFileSHA1s( f1, f2 ), is( false ) );
    }

}
