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
package org.sonatype.nexus.integrationtests.nexus2178;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.ErrorReportingSettings;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2178ErrorReportingConfigRestIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void validationConfiguration()
        throws Exception
    {
        // Default config
        GlobalConfigurationResource resource = SettingsMessageUtil.getCurrentSettings();

        Assert.assertFalse( resource.getErrorReportingSettings().isReportErrorsAutomatically(),
                            "Error reporting should be null by default" );

        // Set some values
        ErrorReportingSettings settings = resource.getErrorReportingSettings();
        settings.setJiraUsername( "someusername" );
        settings.setJiraPassword( "somepassword" );
        settings.setReportErrorsAutomatically( true );

        SettingsMessageUtil.save( resource );

        resource = SettingsMessageUtil.getCurrentSettings();

        Assert.assertNotNull( resource.getErrorReportingSettings(), "Error reporting should not be null" );
        Assert.assertEquals( "someusername", resource.getErrorReportingSettings().getJiraUsername() );
        Assert.assertEquals( AbstractNexusPlexusResource.PASSWORD_PLACE_HOLDER,
                             resource.getErrorReportingSettings().getJiraPassword() );

        // Clear them again
        resource.setErrorReportingSettings( null );

        Assert.assertTrue( SettingsMessageUtil.save( resource ).isSuccess() );

        resource = SettingsMessageUtil.getCurrentSettings();

        Assert.assertFalse( resource.getErrorReportingSettings().isReportErrorsAutomatically(),
                            "Error reporting should be null" );
    }
}
