/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.unpack.it;

import java.util.prefs.Preferences;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.testng.annotations.BeforeMethod;

import com.sonatype.nexus.licensing.NexusLicenseBuilder;
import com.sonatype.nexus.licensing.NexusLicensingManager;

public abstract class AbstractUnpackIT
    extends AbstractNexusIntegrationTest
{

    protected NexusLicensingManager licenseManager;

    @Override
    @BeforeMethod
    public void oncePerClassSetUp()
        throws Exception
    {
        beforeStart();

        super.oncePerClassSetUp();
    }

    protected void beforeStart()
        throws Exception
    {
        licenseManager = lookup( NexusLicensingManager.class );
        licenseManager.uninstallLicense();

        Preferences.userRoot().node( NexusLicenseBuilder.PACKAGE ).remove( "trialEligible" );
        Preferences.userRoot().node( NexusLicenseBuilder.PACKAGE ).sync();

    }
}
