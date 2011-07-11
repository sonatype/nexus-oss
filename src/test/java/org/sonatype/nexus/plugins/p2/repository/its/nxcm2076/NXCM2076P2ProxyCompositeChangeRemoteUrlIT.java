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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm2076;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class NXCM2076P2ProxyCompositeChangeRemoteUrlIT
    extends AbstractNexusProxyP2IT
{
    private final RepositoryMessageUtil repoUtil;

    public NXCM2076P2ProxyCompositeChangeRemoteUrlIT()
        throws Exception
    {
        super( "nxcm2076" );
        repoUtil = new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void test()
        throws Exception
    {
        File artifactsXmlFile =
            downloadFile( new URL( getNexusTestRepoUrl() + "artifacts.xml" ),
                "target/downloads/nxcm2076/artifactsBeforeChange.xml" );
        String artifactsXml = FileUtils.fileRead( artifactsXmlFile );
        Assert.assertTrue( artifactsXml.contains( "id=\"com.sonatype.nexus.p2.its.bundle\"" ) );
        Assert.assertFalse( artifactsXml.contains( "id=\"com.sonatype.nexus.p2.its.bundle3\"" ) );

        try
        {
            downloadFile( new URL( getNexusTestRepoUrl() + "plugins/com.sonatype.nexus.p2.its.bundle3_1.0.0.jar" ),
                "target/downloads/nxcm2076/com.sonatype.nexus.p2.its.bundle3_1.0.0.jar" );
            Assert.fail( "Expected FileNotFoundException for " + getNexusTestRepoUrl()
                + "plugins/com.sonatype.nexus.p2.its.bundle3_1.0.0.jar" );
        }
        catch ( final FileNotFoundException expected )
        {
        }

        // Change the remote url
        final RepositoryProxyResource p2ProxyRepo =
            (RepositoryProxyResource) repoUtil.getRepository( getTestRepositoryId() );
        String remoteUrl = p2ProxyRepo.getRemoteStorage().getRemoteStorageUrl();
        System.out.println( remoteUrl );
        remoteUrl = remoteUrl.replace( "nxcm2076-1", "nxcm2076-2" );
        p2ProxyRepo.getRemoteStorage().setRemoteStorageUrl( remoteUrl );
        repoUtil.updateRepo( p2ProxyRepo, false );

        TaskScheduleUtil.waitForAllTasksToStop();

        artifactsXmlFile =
            downloadFile( new URL( getNexusTestRepoUrl() + "artifacts.xml" ),
                "target/downloads/nxcm2076/artifactsAfterChange.xml" );
        artifactsXml = FileUtils.fileRead( artifactsXmlFile );
        Assert.assertFalse( artifactsXml.contains( "id=\"com.sonatype.nexus.p2.its.bundle\"" ) );
        Assert.assertTrue( artifactsXml.contains( "id=\"com.sonatype.nexus.p2.its.bundle3\"" ) );

        downloadFile( new URL( getNexusTestRepoUrl() + "plugins/com.sonatype.nexus.p2.its.bundle3_1.0.0.jar" ),
            "target/downloads/nxcm2076/com.sonatype.nexus.p2.its.bundle3_1.0.0.jar" );
    }
}
