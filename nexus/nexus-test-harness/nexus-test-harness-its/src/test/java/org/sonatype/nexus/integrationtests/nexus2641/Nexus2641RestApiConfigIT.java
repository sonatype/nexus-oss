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
package org.sonatype.nexus.integrationtests.nexus2641;

import static org.testng.Assert.assertEquals;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.RestApiSettings;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2641RestApiConfigIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void checkRestApiConfig()
        throws Exception
    {
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();

        // *NEXUS-3840
        assertEquals( settings.getGlobalRestApiSettings().getUiTimeout(), 60 );
        // *

        Assert.assertNotNull( settings.getGlobalRestApiSettings() );

        // enable it, not that even the baseUrl is not set, it will be filled with a defaut one
        RestApiSettings restApiSettings = new RestApiSettings();
        settings.setGlobalRestApiSettings( restApiSettings );
        SettingsMessageUtil.save( settings );
        settings = SettingsMessageUtil.getCurrentSettings();

        Assert.assertNotNull( settings.getGlobalRestApiSettings() );
        Assert.assertTrue( StringUtils.isNotEmpty( settings.getGlobalRestApiSettings().getBaseUrl() ) );
        Assert.assertEquals( false, settings.getGlobalRestApiSettings().isForceBaseUrl() );

        // now edit it
        restApiSettings.setBaseUrl( "http://myhost/nexus" );
        restApiSettings.setForceBaseUrl( true );
        settings.setGlobalRestApiSettings( restApiSettings );
        SettingsMessageUtil.save( settings );
        settings = SettingsMessageUtil.getCurrentSettings();

        Assert.assertNotNull( settings.getGlobalRestApiSettings() );
        Assert.assertEquals( "http://myhost/nexus", settings.getGlobalRestApiSettings().getBaseUrl() );
        Assert.assertEquals( true, settings.getGlobalRestApiSettings().isForceBaseUrl() );

        //now unset it
        settings.setGlobalRestApiSettings( null );
        SettingsMessageUtil.save( settings );
        
        Assert.assertNull( settings.getGlobalRestApiSettings() );
    }
}
