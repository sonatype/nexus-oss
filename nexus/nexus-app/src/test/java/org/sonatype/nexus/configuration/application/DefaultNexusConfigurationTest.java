/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.configuration.application;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.io.InputStreamFacade;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.Configuration;

import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.security.SecuritySystem;

public class DefaultNexusConfigurationTest
    extends AbstractNexusTestCase
{

    protected DefaultNexusConfiguration nexusConfiguration;
    
    protected SecuritySystem securitySystem;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusConfiguration = (DefaultNexusConfiguration) this.lookup( NexusConfiguration.class );
        
        securitySystem = this.lookup( SecuritySystem.class );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    public void testSaveConfiguration()
        throws Exception
    {
        nexusConfiguration.loadConfiguration();

        Configuration config = nexusConfiguration.getConfiguration();

        assertEquals( true, this.securitySystem.isSecurityEnabled() );

        this.securitySystem.setSecurityEnabled( false );

        nexusConfiguration.saveConfiguration();

        nexusConfiguration.loadConfiguration();

        config = nexusConfiguration.getConfiguration();

        assertEquals( false, this.securitySystem.isSecurityEnabled() );
    }

    public void testSaveGlobalProxyConfiguration()
        throws Exception
    {
        // default has no Global Proxy, we will set one
        nexusConfiguration.loadConfiguration();

        Configuration config = nexusConfiguration.getConfiguration();

        assertEquals( null, config.getGlobalHttpProxySettings() );

        CRemoteHttpProxySettings settings = new CRemoteHttpProxySettings();

        settings.setProxyHostname( "testhost.proxy.com" );

        settings.setProxyPort( 1234 );

        nexusConfiguration.updateGlobalRemoteHttpProxySettings( settings );

        nexusConfiguration.saveConfiguration();

        // force reload
        nexusConfiguration.loadConfiguration( true );

        config = nexusConfiguration.getConfiguration();

        String proxyHostName =
            nexusConfiguration.getGlobalRemoteStorageContext().getRemoteProxySettings().getHostname();

        int proxyPort = nexusConfiguration.getGlobalRemoteStorageContext().getRemoteProxySettings().getPort();

        assertEquals( nexusConfiguration.getConfiguration().getGlobalHttpProxySettings().getProxyHostname(),
                      proxyHostName );

        assertEquals( nexusConfiguration.getConfiguration().getGlobalHttpProxySettings().getProxyPort(), proxyPort );

    }

    public void testLoadConfiguration()
        throws Exception
    {
        // this will create default config
        nexusConfiguration.loadConfiguration();

        // get it
        Configuration config = nexusConfiguration.getConfiguration();

        // check it for default value
        assertEquals( "smtp-host", config.getSmtpConfiguration().getHostname() );

        // modify it
        config.getSmtpConfiguration().setHostname( "NEW-HOST" );

        // save it
        nexusConfiguration.saveConfiguration();

        // replace it again with default "from behind"
        InputStreamFacade isf = new InputStreamFacade()
        {

            public InputStream getInputStream()
                throws IOException
            {
                return getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" );
            }

        };
        FileUtils.copyStreamToFile( isf, new File( getNexusConfiguration() ) );

        // force reload
        nexusConfiguration.loadConfiguration( true );

        // get the config
        config = nexusConfiguration.getConfiguration();

        // it again contains default value, coz we overwritten it before
        assertEquals( "smtp-host", config.getSmtpConfiguration().getHostname() );
    }

    public void testGetConfiguration()
        throws Exception
    {
        assertEquals( null, nexusConfiguration.getConfiguration() );

        nexusConfiguration.loadConfiguration();

        assertTrue( nexusConfiguration.getConfiguration() != null );
    }

    public void testGetDefaultConfigurationAsStream()
        throws Exception
    {
        nexusConfiguration.loadConfiguration();

        contentEquals( getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" ), nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfigurationAsStream() );
    }

    // this test have no sense anymore, after config refactoring
    // the config and repo "live object" are from now one
    // public void testNX467()
    // throws Exception
    // {
    // // load default config
    // nexusConfiguration.loadConfiguration();
    //
    // M2GroupRepository groupRouter = (M2GroupRepository) lookup( GroupRepository.class, "maven2" );
    //
    // // runtime state should equal to config
    // assertEquals( nexusConfiguration.getConfiguration().getRouting().isMergeMetadata(),
    // groupRouter.isMergeMetadata() );
    //
    // // invert runtime state
    // groupRouter.setMergeMetadata( !groupRouter.isMergeMetadata() );
    //
    // // force reloading of config
    // nexusConfiguration.loadConfiguration( true );
    //
    // // runtime state should equal to config again
    // assertEquals( nexusConfiguration.getConfiguration().getRouting().getGroups().isMergeMetadata(),
    // groupRouter.isMergeMetadata() );
    // }

    public void testGetAndReadConfigurationFiles()
        throws Exception
    {
        nexusConfiguration.loadConfiguration();

        File testConfFile = new File( CONF_HOME, "test.xml" );

        FileUtils.fileWrite( testConfFile.getAbsolutePath(), "test" );

        Map<String, String> confFileNames = nexusConfiguration.getConfigurationFiles();

        assertTrue( confFileNames.size() > 1 );

        assertTrue( confFileNames.containsValue( "nexus.xml" ) );

        assertTrue( confFileNames.containsValue( "test.xml" ) );

        for ( Map.Entry<String, String> entry : confFileNames.entrySet() )
        {
            if ( entry.getValue().equals( "test.xml" ) )
            {
                contentEquals( new ByteArrayInputStream( "test".getBytes() ), nexusConfiguration
                    .getConfigurationAsStreamByKey( entry.getKey() ).getInputStream() );
            }
            else if ( entry.getValue().equals( "nexus.xml" ) )
            {
                contentEquals( new FileInputStream( new File( getNexusConfiguration() ) ), nexusConfiguration
                    .getConfigurationAsStreamByKey( entry.getKey() ).getInputStream() );
            }
        }
        FileUtils.forceDelete( testConfFile );

    }
}
