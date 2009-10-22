package com.sonatype.nexus.unpack.it.nxcm1312;

import java.io.File;
import java.util.prefs.Preferences;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.test.utils.DeployUtils;

import com.sonatype.nexus.licensing.NexusLicenseBuilder;
import com.sonatype.nexus.unpack.it.AbstractUnpackIT;

public class NXCM1312UploadExpiredLicenseIT
    extends AbstractUnpackIT
{

    @Override
    protected void beforeStart()
        throws Exception
    {
        Preferences.userRoot().node( NexusLicenseBuilder.PACKAGE ).putBoolean( "trialEligible", false );
        Preferences.userRoot().node( NexusLicenseBuilder.PACKAGE ).remove( "license" );
        Preferences.userRoot().node( NexusLicenseBuilder.PACKAGE ).sync();
    }

    @Test
    public void upload()
        throws Exception
    {
        try
        {
            DeployUtils.deployWithWagon( container, "http", nexusBaseUrl + "service/local/repositories/"
                + REPO_TEST_HARNESS_REPO + "/content-compressed", getTestFile( "bundle.zip" ), "license" );
        }
        catch ( org.apache.maven.wagon.TransferFailedException e )
        {
            Assert.assertTrue( e.getMessage().contains( "402" ) );
        }

        File root = new File( nexusWorkDir, "storage/nexus-test-harness-repo/license" );
        Assert.assertFalse( root.exists() );
    }

}
