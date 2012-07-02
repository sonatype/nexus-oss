/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.unpack.it;

import java.io.IOException;
import java.util.prefs.Preferences;

import org.restlet.data.MediaType;
import org.sonatype.licensing.product.ProductLicenseManager;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.testng.annotations.BeforeMethod;

import com.sonatype.nexus.licensing.DefaultNexusLicenseBuilder;

public abstract class AbstractUnpackIT
    extends AbstractNexusIntegrationTest
{
    protected ProductLicenseManager licenseManager;

    @Override
    @BeforeMethod( alwaysRun = true )
    public void oncePerClassSetUp()
        throws Exception
    {
        beforeStart();
        super.oncePerClassSetUp();
        setIndexingEnabled( false );
    }

    protected void beforeStart()
        throws Exception
    {
        licenseManager = lookup( ProductLicenseManager.class );
        licenseManager.uninstallLicense();

        Preferences.userRoot().node( DefaultNexusLicenseBuilder.PACKAGE ).remove( "trialEligible" );
        Preferences.userRoot().node( DefaultNexusLicenseBuilder.PACKAGE ).sync();
    }

    protected void setIndexingEnabled( final boolean enabled )
        throws IOException, InterruptedException
    {
        // remember secure value
        final boolean prevSecureTest = TestContainer.getInstance().getTestContext().isSecureTest();
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        // disable indexing for repository we use for testing.
        // Indexer PROBABLY causes issues like https://issues.sonatype.org/browse/NXCM-3986
        final RepositoryMessageUtil rmu = new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
        // we know from test premises what we will get back (not a group, not a shadow)
        final RepositoryResource repoModel = (RepositoryResource) rmu.getRepository( REPO_TEST_HARNESS_REPO );
        repoModel.setIndexable( enabled );
        rmu.updateRepo( repoModel );
        // set back what it was
        TestContainer.getInstance().getTestContext().setSecureTest( prevSecureTest );
        getEventInspectorsUtil().waitForCalmPeriod();
    }
}
