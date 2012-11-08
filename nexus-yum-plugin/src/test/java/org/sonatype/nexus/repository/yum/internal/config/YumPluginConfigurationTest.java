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
import static java.lang.Thread.sleep;
import static org.apache.commons.io.IOUtils.write;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.repository.yum.internal.utils.AbstractYumNexusTestCase;
import org.sonatype.nexus.repository.yum.internal.rest.AliasNotFoundException;
import org.xml.sax.SAXException;

/**
 * Created by IntelliJ IDEA. User: MKrautz Date: 7/8/11 Time: 3:02 PM To change this template use File | Settings | File
 * Templates.
 */
public class YumPluginConfigurationTest
    extends AbstractYumNexusTestCase
{

    private static final String YUM_XML = "yum.xml";

    private static final String PRODUCTION_VERSION = "5.1.15-1";

    private static final String NEW_PRODUCTION_VERSION = "5.5.5";

    private static final String PRODUCTION = "production";

    private static final String NEW_ALIAS_VERSION = "aliasVersion";

    private static final String NEW_ALIAS = "writeAlias";

    private static final String NEW_REPO_NAME = "writeRepo";

    private static final String TRUNK_VERSION = "5.1.15-2";

    private static final String TRUNK = "trunk";

    private static final String MYREPO_ID = "releases";

    @Inject
    private YumPluginConfiguration yumConfiguration;

    @Inject
    private NexusConfiguration nexusConfiguration;

    @Before
    public void loadYumConfig()
    {
        yumConfiguration.load();
    }

    @Test
    public void loadConfigFile()
        throws Exception
    {
        final YumConfiguration expectedXmlConf = createXmlyumConfig();

        final YumConfiguration configuration = yumConfiguration.getXmlYumConfiguration();

        Assert.assertEquals( expectedXmlConf, configuration );
    }

    @Test
    public void saveConfig()
        throws Exception
    {
        final String testConfFilename = "yumWriteTest.xml";
        yumConfiguration.setFilename( testConfFilename );

        final YumConfiguration confToWrite = createXmlyumConfig();
        confToWrite.setRepositoryCreationTimeout( 150 );
        confToWrite.getAliasMappings().add( new AliasMapping( NEW_REPO_NAME, NEW_ALIAS, NEW_ALIAS_VERSION ) );

        yumConfiguration.saveConfig( confToWrite );

        assertSame( confToWrite, yumConfiguration.getXmlYumConfiguration() );
        assertConfigSaved( testConfFilename );
    }

    @Test( expected = AliasNotFoundException.class )
    public void aliasMappingNotFound()
        throws Exception
    {
        yumConfiguration.getVersion( "not", "present" );
    }

    @Test
    public void loadedVersionFound()
        throws Exception
    {
        final String version = yumConfiguration.getVersion( MYREPO_ID, TRUNK );
        Assert.assertEquals( TRUNK_VERSION, version );
    }

    @Test
    public void overrideExisting()
        throws Exception
    {
        final String newVersion = "myNewVersion";
        yumConfiguration.setAlias( MYREPO_ID, TRUNK, newVersion );

        final String actual = yumConfiguration.getVersion( MYREPO_ID, TRUNK );
        Assert.assertEquals( newVersion, actual );
    }

    @Test
    public void newVersionSaved()
        throws Exception
    {
        final String testConfFilename = "yumWriteTest2.xml";
        yumConfiguration.setFilename( testConfFilename );
        yumConfiguration.setAlias( NEW_REPO_NAME, NEW_ALIAS, NEW_ALIAS_VERSION );
        assertConfigSaved( testConfFilename );
    }

    @Test
    public void newVersionFound()
        throws Exception
    {
        final String newRepo = "the new on";
        final String newAlias = "new alias";
        final String version = "the version";
        yumConfiguration.setAlias( newRepo, newAlias, version );

        final String actual = yumConfiguration.getVersion( newRepo, newAlias );
        Assert.assertEquals( version, actual );
    }

    @Test
    public void shouldUpdateConfigIfFileIsWritten()
        throws Exception
    {
        yumConfiguration.load();
        manipulateConfigFile();
        Assert.assertEquals( NEW_PRODUCTION_VERSION, yumConfiguration.getVersion( MYREPO_ID, PRODUCTION ) );
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
        Assert.assertNotNull( config.getXmlYumConfiguration().getAliasMappings() );
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

    private void manipulateConfigFile()
        throws FileNotFoundException, IOException, InterruptedException
    {
        // wait one second, because last modification date of files has granularity
        // 1 second.
        sleep( 1000 );

        String configContent;
        InputStream inputStream = new FileInputStream( yumConfiguration.getConfigFile() );
        try
        {
            configContent = IOUtils.toString( inputStream );
        }
        finally
        {
            inputStream.close();
        }

        OutputStream outputStream = new FileOutputStream( yumConfiguration.getConfigFile() );
        try
        {
            write( configContent.replace( PRODUCTION_VERSION, NEW_PRODUCTION_VERSION ), outputStream );
        }
        finally
        {
            outputStream.close();
        }
    }

    private YumConfiguration createXmlyumConfig()
    {
        final YumConfiguration expectedXmlConf = new YumConfiguration();
        expectedXmlConf.setRepositoryCreationTimeout( 150 );
        expectedXmlConf.setAliasMappings( new LinkedHashSet<AliasMapping>() );
        expectedXmlConf.getAliasMappings().add( new AliasMapping( MYREPO_ID, TRUNK, TRUNK_VERSION ) );
        expectedXmlConf.getAliasMappings().add( new AliasMapping( MYREPO_ID, PRODUCTION, PRODUCTION_VERSION ) );
        return expectedXmlConf;
    }

    private void assertConfigSaved( final String testConfFilename )
        throws FileNotFoundException, SAXException, IOException
    {
        final FileReader expectedFile =
            new FileReader( new File( nexusConfiguration.getConfigurationDirectory(), "expetedWrittenYum.xml" ) );
        final FileReader writtenFile =
            new FileReader( new File( nexusConfiguration.getConfigurationDirectory(), testConfFilename ) );
        assertXMLEqual( expectedFile, writtenFile );
    }

}
