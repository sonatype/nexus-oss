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
package org.sonatype.nexus.repository.yum.internal.config;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.inject.Inject;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.repository.yum.internal.utils.AbstractYumNexusTestCase;
import org.xml.sax.SAXException;

public class YumPluginConfigurationTest
    extends AbstractYumNexusTestCase
{

    private static final String YUM_XML = "yum.xml";

    @Inject
    private YumPluginConfiguration yumConfiguration;

    @Inject
    private NexusConfiguration nexusConfiguration;

    @Before
    public void loadYumConfig()
    {
        XMLUnit.setIgnoreWhitespace( true );
        yumConfiguration.load();
    }

    @Test
    public void loadConfigFile()
        throws Exception
    {
        final YumConfiguration expectedXmlConfig = createXmlYumConfig();

        final YumConfiguration configuration = yumConfiguration.getXmlYumConfiguration();

        Assert.assertEquals( expectedXmlConfig, configuration );
    }

    @Test
    public void saveConfig()
        throws Exception
    {
        final String testConfFilename = "yumWriteTest.xml";
        yumConfiguration.setFilename( testConfFilename );

        final YumConfiguration confToWrite = createXmlYumConfig();
        confToWrite.setRepositoryCreationTimeout( 150 );

        yumConfiguration.saveConfig( confToWrite );

        assertSame( confToWrite, yumConfiguration.getXmlYumConfiguration() );
        assertConfigSaved( testConfFilename );
    }

    @Test
    public void shouldCreateConfigFileIfNotExists()
        throws Exception
    {
        File tmpDir = createTmpDir();
        assertThat( new File( tmpDir, YUM_XML ).exists(), is( FALSE ) );

        YumPluginConfigurationImpl yumConfigurationHandler = new YumPluginConfigurationImpl(
            createNexusConfig( tmpDir )
        );
        yumConfigurationHandler.load();
        assertThat( new File( tmpDir, YUM_XML ).exists(), is( TRUE ) );
    }

    @Test
    public void shouldLoadOldOrEmptyYumXmlAndSetDefaults()
        throws Exception
    {
        // given
        final NexusConfiguration nexusConfig = mock( NexusConfiguration.class );
        when( nexusConfig.getConfigurationDirectory() ).thenReturn(
            UTIL.resolveFile( "target/test-classes/config/empty" ) );
        final YumPluginConfigurationImpl config = new YumPluginConfigurationImpl( nexusConfig );

        // then
        assertThat( config.getXmlYumConfiguration().getRepositoryCreationTimeout(), is( 120 ) );
    }

    private NexusConfiguration createNexusConfig( File tmpDir )
    {
        final NexusConfiguration nexusConfig = mock( NexusConfiguration.class );
        when( nexusConfig.getConfigurationDirectory() ).thenReturn( tmpDir );
        return nexusConfig;
    }

    private File createTmpDir()
    {
        File tmpDir = new File( ".", "target/tmp/" + randomAlphabetic( 10 ) );
        tmpDir.mkdirs();
        return tmpDir;
    }

    private YumConfiguration createXmlYumConfig()
    {
        final YumConfiguration expectedXmlConf = new YumConfiguration();
        expectedXmlConf.setRepositoryCreationTimeout( 150 );
        return expectedXmlConf;
    }

    private void assertConfigSaved( final String testConfFilename )
        throws SAXException, IOException
    {
        final FileReader expectedFile =
            new FileReader( new File( nexusConfiguration.getConfigurationDirectory(), "expetedWrittenYum.xml" ) );
        final FileReader writtenFile =
            new FileReader( new File( nexusConfiguration.getConfigurationDirectory(), testConfFilename ) );
        assertXMLEqual( expectedFile, writtenFile );
    }

}
