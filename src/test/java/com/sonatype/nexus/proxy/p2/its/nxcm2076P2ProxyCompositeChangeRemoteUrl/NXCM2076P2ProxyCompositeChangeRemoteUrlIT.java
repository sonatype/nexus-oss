/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.proxy.p2.its.nxcm2076P2ProxyCompositeChangeRemoteUrl;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

import com.sonatype.nexus.proxy.p2.its.AbstractNexusProxyP2IntegrationIT;

public class NXCM2076P2ProxyCompositeChangeRemoteUrlIT
    extends AbstractNexusProxyP2IntegrationIT
{
    private static final String REPOSITORY_ID = "p2proxyComposite";

    private RepositoryMessageUtil repoUtil;

    public NXCM2076P2ProxyCompositeChangeRemoteUrlIT()
        throws Exception
    {
        repoUtil =
            new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void testChangeRemoteUrl()
        throws Exception
    {
        File artifactsXmlFile =
            downloadFile( new URL( getRepositoryUrl( REPOSITORY_ID ) + "artifacts.xml" ),
                          "target/downloads/nxcm2076/artifactsBeforeChange.xml" );
        String artifactsXml = FileUtils.fileRead( artifactsXmlFile );
        Assert.assertTrue( artifactsXml.contains( "id=\"com.sonatype.nexus.p2.its.bundle\"" ) );
        Assert.assertFalse( artifactsXml.contains( "id=\"com.sonatype.nexus.p2.its.bundle3\"" ) );

        try
        {
            downloadFile( new URL( getRepositoryUrl( REPOSITORY_ID )
                + "plugins/com.sonatype.nexus.p2.its.bundle3_1.0.0.jar" ),
                          "target/downloads/nxcm2076/com.sonatype.nexus.p2.its.bundle3_1.0.0.jar" );
            Assert.fail( "Expected FileNotFoundException for " + getRepositoryUrl( REPOSITORY_ID )
                + "plugins/com.sonatype.nexus.p2.its.bundle3_1.0.0.jar" );
        }
        catch ( FileNotFoundException expected )
        {
        }

        // Change the remote url
        RepositoryProxyResource p2ProxyRepo = (RepositoryProxyResource) repoUtil.getRepository( REPOSITORY_ID );
        String remoteUrl = p2ProxyRepo.getRemoteStorage().getRemoteStorageUrl();
        System.out.println( remoteUrl );
        remoteUrl = remoteUrl.replace( "p2repoCompositeContentAndArtifacts", "nxcm2076" );
        p2ProxyRepo.getRemoteStorage().setRemoteStorageUrl( remoteUrl );
        repoUtil.updateRepo( p2ProxyRepo, false );

        TaskScheduleUtil.waitForAllTasksToStop();

        artifactsXmlFile =
            downloadFile( new URL( getRepositoryUrl( REPOSITORY_ID ) + "artifacts.xml" ),
                          "target/downloads/nxcm2076/artifactsAfterChange.xml" );
        artifactsXml = FileUtils.fileRead( artifactsXmlFile );
        Assert.assertFalse( artifactsXml.contains( "id=\"com.sonatype.nexus.p2.its.bundle\"" ) );
        Assert.assertTrue( artifactsXml.contains( "id=\"com.sonatype.nexus.p2.its.bundle3\"" ) );

        downloadFile( new URL( getRepositoryUrl( REPOSITORY_ID )
            + "plugins/com.sonatype.nexus.p2.its.bundle3_1.0.0.jar" ),
                      "target/downloads/nxcm2076/com.sonatype.nexus.p2.its.bundle3_1.0.0.jar" );
    }
}
