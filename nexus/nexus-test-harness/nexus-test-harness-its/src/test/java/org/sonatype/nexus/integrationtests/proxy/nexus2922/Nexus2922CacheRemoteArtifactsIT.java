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
package org.sonatype.nexus.integrationtests.proxy.nexus2922;

import static org.sonatype.nexus.integrationtests.ITGroups.PROXY;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.index.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus2922CacheRemoteArtifactsIT
    extends AbstractNexusProxyIntegrationTest
{
    private static Gav GAV1;

    private static Gav GAV2;

    @Override
    protected void runOnce()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        settings.setSecurityAnonymousAccessEnabled( false );
        SettingsMessageUtil.save( settings );
    }
    
    @BeforeClass(alwaysRun = true)
    public void enableSecurity(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    public Nexus2922CacheRemoteArtifactsIT()
    {
        super( "release-proxy-repo-1" );
        GAV1 = GavUtil.newGav( "nexus2922", "artifact", "1.0.0" );
        GAV2 = GavUtil.newGav( "nexus2922", "artifact", "2.0.0" );
    }
    
    protected void clearCredentials()
    {
        TestContainer.getInstance().getTestContext().setUsername("");
        TestContainer.getInstance().getTestContext().setPassword("");
    }

    @Test(groups = PROXY)
    public void downloadNoPriv()
        throws IOException
    {
        String msg = null;
        clearCredentials();

        try
        {
            this.downloadArtifactFromRepository( REPO_RELEASE_PROXY_REPO1, GAV1, "target/downloads" );
            Assert.fail( "Should fail to download artifact" );
        }
        catch ( FileNotFoundException e )
        {
            // ok!
            msg = e.getMessage();
        }

        File file = new File( nexusWorkDir, "storage/release-proxy-repo-1/nexus2922/artifact/1.0.0/artifact-1.0.0.jar" );
        Assert.assertFalse( file.exists(), file.toString() );

        Assert.assertTrue( msg.contains( "401" ), msg );
    }

    @Test(groups = PROXY)
    public void downloadNoPrivFromProxy()
        throws IOException
    {
        String msg = null;
        clearCredentials();

        try
        {
            this.downloadArtifactFromRepository( REPO_TEST_HARNESS_REPO, GAV1, "target/downloads" );
            Assert.fail( "Should fail to download artifact" );
        }
        catch ( FileNotFoundException e )
        {
            // ok!
            msg = e.getMessage();
        }

        File file =
            new File( nexusWorkDir, "storage/nexus-test-harness-repo/nexus2922/artifact/1.0.0/artifact-1.0.0.jar" );
        Assert.assertFalse( file.exists(), file.toString() );

        Assert.assertTrue( msg.contains( "401" ), msg );
    }

    @Test(groups = PROXY)
    public void downloadAdmin()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        this.downloadArtifactFromRepository( REPO_RELEASE_PROXY_REPO1, GAV2, "target/downloads" );

        File file = new File( nexusWorkDir, "storage/release-proxy-repo-1/nexus2922/artifact/2.0.0/artifact-2.0.0.jar" );
        Assert.assertTrue( file.exists(), file.toString() );
    }
}
