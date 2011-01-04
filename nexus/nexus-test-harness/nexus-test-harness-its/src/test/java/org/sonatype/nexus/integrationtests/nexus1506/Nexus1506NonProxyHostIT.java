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
package org.sonatype.nexus.integrationtests.nexus1506;

import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus1506NonProxyHostIT
extends AbstractNexusIntegrationTest
{
    @Test
    public void checkNonProxyHosts()
        throws Exception
    {
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();

        settings.setGlobalHttpProxySettings( new RemoteHttpProxySettings() );
        settings.getGlobalHttpProxySettings().setProxyHostname( "proxyHost" );
        settings.getGlobalHttpProxySettings().setProxyPort( 3211 );
        settings.getGlobalHttpProxySettings().addNonProxyHost( "foo" );
        settings.getGlobalHttpProxySettings().addNonProxyHost( "bar" );
        
        Assert.assertEquals( 204, SettingsMessageUtil.save( settings ).getCode() );
        
        settings = SettingsMessageUtil.getCurrentSettings();
        Assert.assertEquals( 2, settings.getGlobalHttpProxySettings().getNonProxyHosts().size() );
        
        CRemoteHttpProxySettings proxySettings = getNexusConfigUtil().getNexusConfig().getGlobalHttpProxySettings();
        Assert.assertEquals( "foo", proxySettings.getNonProxyHosts().get( 0 ) );
        Assert.assertEquals( "bar", proxySettings.getNonProxyHosts().get( 1 ) );
        Assert.assertEquals( 2, proxySettings.getNonProxyHosts().size() );
    }
    
    @Test
    public void checkNonProxyHostsEmptyAndNulls()
        throws Exception
    {
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();

        settings.setGlobalHttpProxySettings( new RemoteHttpProxySettings() );
        settings.getGlobalHttpProxySettings().setProxyHostname( "proxyHost" );
        settings.getGlobalHttpProxySettings().setProxyPort( 3211 );
        settings.getGlobalHttpProxySettings().addNonProxyHost( "" );
        settings.getGlobalHttpProxySettings().addNonProxyHost( "foo" );
        settings.getGlobalHttpProxySettings().addNonProxyHost( null );
        
        Assert.assertEquals( 204, SettingsMessageUtil.save( settings ).getCode() );
        
        settings = SettingsMessageUtil.getCurrentSettings();
        Assert.assertEquals( 1, settings.getGlobalHttpProxySettings().getNonProxyHosts().size() );
        
        CRemoteHttpProxySettings proxySettings = getNexusConfigUtil().getNexusConfig().getGlobalHttpProxySettings();
        Assert.assertEquals( "foo", proxySettings.getNonProxyHosts().get( 0 ) );
        Assert.assertEquals( 1, proxySettings.getNonProxyHosts().size() );
    }
    
    @Test
    public void checkNonProxyHostsEmpty()
        throws Exception
    {
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();

        settings.setGlobalHttpProxySettings( new RemoteHttpProxySettings() );
        settings.getGlobalHttpProxySettings().setProxyHostname( "proxyHost" );
        settings.getGlobalHttpProxySettings().setProxyPort( 3211 );
        settings.getGlobalHttpProxySettings().getNonProxyHosts().clear();
        
        Assert.assertEquals( 204, SettingsMessageUtil.save( settings ).getCode() );
        
        settings = SettingsMessageUtil.getCurrentSettings();
        Assert.assertEquals( 0, settings.getGlobalHttpProxySettings().getNonProxyHosts().size() );
        
        CRemoteHttpProxySettings proxySettings = getNexusConfigUtil().getNexusConfig().getGlobalHttpProxySettings();
        Assert.assertEquals( 0, proxySettings.getNonProxyHosts().size() );
    }
}
