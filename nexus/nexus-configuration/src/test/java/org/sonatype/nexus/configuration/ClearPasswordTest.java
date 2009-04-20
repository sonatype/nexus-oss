package org.sonatype.nexus.configuration;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.configuration.source.ApplicationConfigurationSource;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;

import com.thoughtworks.xstream.XStream;

public class ClearPasswordTest
    extends AbstractNexusTestCase
{
    private ApplicationConfigurationSource getConfigSource()
        throws Exception
    {
        // get the config
        return this.lookup( ApplicationConfigurationSource.class, "file" );
    }

    public void testDefaultConfig()
        throws Exception
    {
        // start with the default nexus config
        this.copyDefaultConfigToPlace();

        this.doTestLogic();
    }

    public void testUpgrade()
        throws Exception
    {
        // copy a conf file that needs to be upgraded to the config dir
        FileUtils.copyURLToFile( Thread.currentThread().getContextClassLoader().getResource(
            "org/sonatype/nexus/configuration/upgrade/nexus-001-1.xml" ), new File( this.getNexusConfiguration() ) );

        this.doTestLogic();
    }

    private void doTestLogic()
        throws Exception
    {
        ApplicationConfigurationSource source = this.getConfigSource();

        Configuration config = source.loadConfiguration();

        // make sure the smtp-password is what we expect
        Assert.assertEquals( "Incorrect SMTP password found in nexus.xml", "smtp-password", config
            .getSmtpConfiguration().getPassword() );

        // set the clear passwords
        String password = "clear-text";

        // smtp
        config.getSmtpConfiguration().setPassword( password );

        // global proxy
        config.setGlobalHttpProxySettings( new CRemoteHttpProxySettings() );
        config.getGlobalHttpProxySettings().setAuthentication( new CRemoteAuthentication() );
        config.getGlobalHttpProxySettings().getAuthentication().setPassword( password );
        config.getSecurity().setAnonymousPassword( password );

        // anon username
        config.getSecurity().setAnonymousPassword( password );

        // repo auth pass
        CRepository central = this.getCentralRepo( config );
        central.getRemoteStorage().setAuthentication( new CRemoteAuthentication() );
        central.getRemoteStorage().getAuthentication().setPassword( password );

        // repo proxy pass
        central.getRemoteStorage().setHttpProxySettings( new CRemoteHttpProxySettings() );
        central.getRemoteStorage().getHttpProxySettings().setAuthentication( new CRemoteAuthentication() );
        central.getRemoteStorage().getHttpProxySettings().getAuthentication().setPassword( password );

        // now we need to make the file valid....
        config.getGlobalHttpProxySettings().setProxyPort( 1234 );
        central.getRemoteStorage().getHttpProxySettings().setProxyPort( 1234 );

        // save it
        source.storeConfiguration();

        Assert.assertTrue( "Configuration is corroupt, passwords are encrypted (in memory). ", new XStream().toXML(
            config ).contains( password ) );

        // now get the file and look for the "clear-text"
        String configString = FileUtils.fileRead( this.getNexusConfiguration() );

        Assert.assertFalse( "Clear text password found in nexus.xml:\n" + configString, configString
            .contains( password ) );

        // make sure we do not have the default smtp password either
        Assert.assertFalse( "Old SMTP password found in nexus.xml", configString.contains( "smtp-password" ) );

        // now load it again and make sure the password is clear text
        Configuration newConfig = source.loadConfiguration();
        Assert.assertEquals( password, newConfig.getSmtpConfiguration().getPassword() );
        Assert.assertEquals( password, newConfig.getGlobalHttpProxySettings().getAuthentication().getPassword() );
        Assert.assertEquals( password, newConfig.getSecurity().getAnonymousPassword() );

        central = this.getCentralRepo( newConfig );
        Assert.assertEquals( password, central.getRemoteStorage().getAuthentication().getPassword() );
        Assert.assertEquals( password, central
            .getRemoteStorage().getHttpProxySettings().getAuthentication().getPassword() );

    }

    private CRepository getCentralRepo( Configuration config )
    {
        for ( CRepository repo : (List<CRepository>) config.getRepositories() )
        {
            if ( repo.getId().equals( "central" ) )
            {
                return repo;
            }
        }
        return null;
    }

}
