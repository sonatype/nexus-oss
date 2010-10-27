/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package com.sonatype.nexus.unpack.it.nxcm1312;

import java.io.File;
import java.util.prefs.Preferences;

import org.testng.Assert;
import org.testng.annotations.Test;

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
            getDeployUtils().deployWithWagon( "http", nexusBaseUrl + "service/local/repositories/"
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
