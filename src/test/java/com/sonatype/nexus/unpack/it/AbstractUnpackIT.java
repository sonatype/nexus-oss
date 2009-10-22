package com.sonatype.nexus.unpack.it;

import java.util.prefs.Preferences;

import org.junit.Before;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

import com.sonatype.nexus.licensing.NexusLicenseBuilder;
import com.sonatype.nexus.licensing.NexusLicensingManager;

public abstract class AbstractUnpackIT
    extends AbstractNexusIntegrationTest
{

    protected NexusLicensingManager licenseManager;

    @Override
    @Before
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
