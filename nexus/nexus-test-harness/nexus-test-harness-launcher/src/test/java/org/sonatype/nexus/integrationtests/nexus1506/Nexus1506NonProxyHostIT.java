package org.sonatype.nexus.integrationtests.nexus1506;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

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
        
        CRemoteHttpProxySettings proxySettings = NexusConfigUtil.getNexusConfig().getGlobalHttpProxySettings();
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
        
        CRemoteHttpProxySettings proxySettings = NexusConfigUtil.getNexusConfig().getGlobalHttpProxySettings();
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
        
        CRemoteHttpProxySettings proxySettings = NexusConfigUtil.getNexusConfig().getGlobalHttpProxySettings();
        Assert.assertEquals( 0, proxySettings.getNonProxyHosts().size() );
    }
}
