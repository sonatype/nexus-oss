/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
import org.junit.Test;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.email.NexusEmailer;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.security.SecuritySystem;

public class DefaultNexusConfigurationTest
    extends AbstractNexusTestCase
{
    protected DefaultNexusConfiguration nexusConfiguration;

    protected SecuritySystem securitySystem;

    protected NexusEmailer nexusEmailer;

    protected GlobalHttpProxySettings globalHttpProxySettings;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusConfiguration = (DefaultNexusConfiguration) this.lookup( NexusConfiguration.class );

        nexusConfiguration.loadConfiguration();

        securitySystem = this.lookup( SecuritySystem.class );

        nexusEmailer = lookup( NexusEmailer.class );

        globalHttpProxySettings = lookup( GlobalHttpProxySettings.class );
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

    @Test
    public void testSaveConfiguration()
        throws Exception
    {
        Configuration config = nexusConfiguration.getConfigurationModel();

        assertEquals( true, this.securitySystem.isSecurityEnabled() );

        this.securitySystem.setSecurityEnabled( false );

        nexusConfiguration.saveConfiguration();

        nexusConfiguration.loadConfiguration();

        config = nexusConfiguration.getConfigurationModel();

        assertEquals( false, this.securitySystem.isSecurityEnabled() );
    }

    @Test
    public void testSaveGlobalProxyConfiguration()
        throws Exception
    {
        Configuration config = nexusConfiguration.getConfigurationModel();

        assertEquals( null, config.getGlobalHttpProxySettings() );
        assertTrue( !globalHttpProxySettings.isEnabled() );

        globalHttpProxySettings.setHostname( "testhost.proxy.com" );
        globalHttpProxySettings.setPort( 1234 );

        nexusConfiguration.saveConfiguration();

        // force reload
        nexusConfiguration.loadConfiguration( true );

        config = nexusConfiguration.getConfigurationModel();

        String proxyHostName =
            nexusConfiguration.getGlobalRemoteStorageContext().getRemoteProxySettings().getHostname();

        int proxyPort = nexusConfiguration.getGlobalRemoteStorageContext().getRemoteProxySettings().getPort();

        assertEquals( nexusConfiguration.getConfigurationModel().getGlobalHttpProxySettings().getProxyHostname(),
                      proxyHostName );

        assertEquals( nexusConfiguration.getConfigurationModel().getGlobalHttpProxySettings().getProxyPort(), proxyPort );

    }

    @Test
    public void testLoadConfiguration()
        throws Exception
    {
        // get it
        Configuration config = nexusConfiguration.getConfigurationModel();

        // check it for default value
        assertEquals( "smtp-host", config.getSmtpConfiguration().getHostname() );

        // modify it
        nexusEmailer.setSMTPHostname( "NEW-HOST" );

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
        config = nexusConfiguration.getConfigurationModel();

        // it again contains default value, coz we overwritten it before
        assertEquals( "smtp-host", config.getSmtpConfiguration().getHostname() );
        assertEquals( "smtp-host", nexusEmailer.getSMTPHostname() );
    }

    @Test
    public void testGetConfiguration()
        throws Exception
    {
        // well, this test has no meaning anymore, load happens in setUp()!
        nexusConfiguration.loadConfiguration( true );

        assertTrue( nexusConfiguration.getConfigurationModel() != null );
    }

    @Test
    public void testGetDefaultConfigurationAsStream()
        throws Exception
    {
        contentEquals( getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" ), nexusConfiguration
            .getConfigurationSource().getDefaultsSource().getConfigurationAsStream() );
    }

    @Test
    public void testGetAndReadConfigurationFiles()
        throws Exception
    {
        File testConfFile = new File( getConfHomeDir(), "test.xml" );

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

    @Test
    public void testNEXUS2212SaveInvalidConfig()
        throws Exception
    {
        Configuration nexusConfig = nexusConfiguration.getConfigurationModel();

        CRepository centralCRepo = null;

        for ( CRepository cRepo : nexusConfig.getRepositories() )
        {
            if ( cRepo.getId().equals( "central" ) )
            {
                centralCRepo = cRepo;
                break;
            }
        }

        assertNotNull( centralCRepo );

        centralCRepo.setLocalStatus( LocalStatus.OUT_OF_SERVICE.name() );
        nexusConfiguration.saveConfiguration();
    }

}
