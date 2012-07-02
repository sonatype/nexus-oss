/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.unpack.it.nxcm1312;

import static org.testng.Assert.fail;

import java.io.File;
import java.util.prefs.Preferences;

import org.sonatype.licensing.product.ProductLicenseManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sonatype.nexus.licensing.DefaultNexusLicenseBuilder;
import com.sonatype.nexus.unpack.it.AbstractUnpackIT;

public class NXCM1312UploadExpiredLicenseIT
    extends AbstractUnpackIT
{
    protected ProductLicenseManager licenseManager;

    @Override
    protected void beforeStart()
        throws Exception
    {
        licenseManager = lookup( ProductLicenseManager.class );
        licenseManager.uninstallLicense();

        Preferences.userRoot().node( DefaultNexusLicenseBuilder.PACKAGE ).putBoolean( "trialEligible", false );
        Preferences.userRoot().node( DefaultNexusLicenseBuilder.PACKAGE ).remove( "license" );
        Preferences.userRoot().node( DefaultNexusLicenseBuilder.PACKAGE ).sync();
    }

    @Test
    public void upload()
        throws Exception
    {
        try
        {
            getDeployUtils().deployWithWagon( "http",
                nexusBaseUrl + "service/local/repositories/" + REPO_TEST_HARNESS_REPO + "/content-compressed",
                getTestFile( "bundle.zip" ), "license" );
            fail( "License should be expired" );
        }
        catch ( org.apache.maven.wagon.TransferFailedException e )
        {
            Assert.assertTrue( e.getMessage().contains( "402" ) );
        }

        File root = new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_REPO + "/license" );
        Assert.assertFalse( root.exists() );
    }

}
