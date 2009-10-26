package org.sonatype.nexus.integrationtests.proxy.nexus2922;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

public class Nexus2922CacheRemoteArtifactsTest
    extends AbstractNexusProxyIntegrationTest
{

    static
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Before
    public void enableSecurity()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    private static Gav GAV1;

    private static Gav GAV2;

    @BeforeClass
    public static void init()
        throws IllegalArtifactCoordinateException
    {
        GAV1 = GavUtil.newGav( "nexus2922", "artifact", "1.0.0" );
        GAV2 = GavUtil.newGav( "nexus2922", "artifact", "2.0.0" );
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        settings.setSecurityAnonymousAccessEnabled( false );
        SettingsMessageUtil.save( settings );
    }

    public Nexus2922CacheRemoteArtifactsTest()
    {
        super( "release-proxy-repo-1" );
    }

    @Test
    public void downloadNoPriv()
        throws IOException
    {
        TestContainer.getInstance().getTestContext().setSecureTest( false );

        String msg = null;

        try
        {
            this.downloadArtifactFromRepository( REPO_RELEASE_PROXY_REPO1, GAV1, "target/downloads" );
            Assert.fail( "Should fail to downlo artifact" );
        }
        catch ( FileNotFoundException e )
        {
            // ok!
            msg = e.getMessage();
        }

        File file = new File( nexusWorkDir, "storage/release-proxy-repo-1/nexus2922/artifact/1.0.0/artifact-1.0.0.jar" );
        Assert.assertFalse( file.toString(), file.exists() );

        Assert.assertTrue( msg, msg.contains( "401" ) );
    }

    @Test
    public void downloadNoPrivFromProxy()
        throws IOException
    {
        TestContainer.getInstance().getTestContext().setSecureTest( false );

        String msg = null;

        try
        {
            this.downloadArtifactFromRepository( REPO_TEST_HARNESS_REPO, GAV1, "target/downloads" );
            Assert.fail( "Should fail to downlo artifact" );
        }
        catch ( FileNotFoundException e )
        {
            // ok!
            msg = e.getMessage();
        }

        File file =
            new File( nexusWorkDir, "storage/nexus-test-harness-repo/nexus2922/artifact/1.0.0/artifact-1.0.0.jar" );
        Assert.assertFalse( file.toString(), file.exists() );

        Assert.assertTrue( msg, msg.contains( "401" ) );
    }

    @Test
    public void downloadAdmin()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        this.downloadArtifactFromRepository( REPO_RELEASE_PROXY_REPO1, GAV2, "target/downloads" );

        File file = new File( nexusWorkDir, "storage/release-proxy-repo-1/nexus2922/artifact/2.0.0/artifact-2.0.0.jar" );
        Assert.assertTrue( file.toString(), file.exists() );
    }
}
