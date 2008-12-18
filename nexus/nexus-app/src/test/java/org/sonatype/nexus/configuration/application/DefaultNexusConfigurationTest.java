/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.io.InputStreamFacade;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class DefaultNexusConfigurationTest
    extends AbstractNexusTestCase
{

    protected DefaultNexusConfiguration nexusConfiguration;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusConfiguration = (DefaultNexusConfiguration) this.lookup( NexusConfiguration.class );
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

        assertEquals( true, config.getSecurity().isEnabled() );

        config.getSecurity().setEnabled( false );

        nexusConfiguration.saveConfiguration();

        nexusConfiguration.loadConfiguration();

        config = nexusConfiguration.getConfiguration();

        assertEquals( false, config.getSecurity().isEnabled() );
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

        String proxyHostName = ( (CRemoteHttpProxySettings) ( (DefaultNexusConfiguration) nexusConfiguration )
            .getRemoteStorageContext().getRemoteConnectionContextObject(
                RemoteStorageContext.REMOTE_HTTP_PROXY_SETTINGS ) ).getProxyHostname();

        int proxyPort = ( (CRemoteHttpProxySettings) ( (DefaultNexusConfiguration) nexusConfiguration )
            .getRemoteStorageContext().getRemoteConnectionContextObject(
                RemoteStorageContext.REMOTE_HTTP_PROXY_SETTINGS ) ).getProxyPort();

        assertEquals(
            nexusConfiguration.getConfiguration().getGlobalHttpProxySettings().getProxyHostname(),
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
        assertEquals( true, config.getSecurity().isEnabled() );

        // modify it
        config.getSecurity().setEnabled( false );

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
        assertEquals( true, config.getSecurity().isEnabled() );
    }

    public void testGetConfiguration()
        throws Exception
    {
        assertEquals( null, nexusConfiguration.getConfiguration() );

        nexusConfiguration.loadConfiguration();

        assertTrue( nexusConfiguration.getConfiguration() != null );
    }

    public void testGetConfigurationAsStream()
        throws Exception
    {
        nexusConfiguration.loadConfiguration();

        IOUtil.contentEquals( new FileInputStream( new File( getNexusConfiguration() ) ), nexusConfiguration
            .getConfigurationAsStream() );
    }

    public void testGetDefaultConfigurationAsStream()
        throws Exception
    {
        nexusConfiguration.loadConfiguration();

        IOUtil.contentEquals( getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" ), nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfigurationAsStream() );
    }

    public void testNX467()
        throws Exception
    {
        // load default config
        nexusConfiguration.loadConfiguration();

        M2GroupRepository groupRouter = (M2GroupRepository) lookup( GroupRepository.class, "maven2" );

        // runtime state should equal to config
        assertEquals( nexusConfiguration.getConfiguration().getRouting().getGroups().isMergeMetadata(), groupRouter
            .isMergeMetadata() );

        // invert runtime state
        groupRouter.setMergeMetadata( !groupRouter.isMergeMetadata() );

        // force reloading of config
        nexusConfiguration.loadConfiguration( true );

        // runtime state should equal to config again
        assertEquals( nexusConfiguration.getConfiguration().getRouting().getGroups().isMergeMetadata(), groupRouter
            .isMergeMetadata() );
    }
}
