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
package org.sonatype.nexus.yum.internal.support;

import static com.google.code.tempusfugit.temporal.Duration.millis;
import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.freecompany.redline.header.Architecture.NOARCH;
import static org.freecompany.redline.header.Os.LINUX;
import static org.freecompany.redline.header.RpmType.BINARY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.component.annotations.Requirement;
import org.custommonkey.xmlunit.Diff;
import org.freecompany.redline.Builder;
import org.junit.Before;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.test.NexusTestSupport;
import org.sonatype.sisu.litmus.testsupport.TestTracer;
import org.sonatype.sisu.litmus.testsupport.TestUtil;
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers;
import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.ThreadSleep;
import com.google.code.tempusfugit.temporal.Timeout;

public class YumNexusTestSupport
    extends NexusTestSupport
{

    private static final Logger LOG = LoggerFactory.getLogger( YumNexusTestSupport.class );

    public static final TestUtil UTIL = new TestUtil( new Marker() );

    public static final File BASE_TMP_FILE = UTIL.resolveFile( "target/test-tmp" );

    public static final File BASE_FILE = UTIL.resolveFile( "target/test-classes" );

    public static final File RPM_BASE_FILE = new File( BASE_FILE, "repo" );

    public static final File TARGET_DIR = new File( BASE_TMP_FILE, "generated-repos" );

    public static final File BASE_CACHE_DIR = new File( BASE_TMP_FILE, ".cache" );

    public static final File PACKAGE_CACHE_DIR = new File( BASE_CACHE_DIR, ".packageFiles" );

    public static final File REPODATA_DIR = new File( TARGET_DIR, "repodata" );

    public static final File TEMPLATE_DIR = new File( BASE_FILE, "templates" );

    public static final String PRIMARY_XML = "primary.xml";

    public static final String REPOMD_XML = "repomd.xml";

    public static final String PRIMARY_XML_GZ = PRIMARY_XML + ".gz";

    private static final String REPO = "foo";

    public static final String TMP_DIR_KEY = "java.io.tmpdir";

    private String oldTmpDir;

    @Rule
    public final TestTracer tracer = new TestTracer( this );

    @Inject
    private GlobalRestApiSettings globalRestApiSettings;

    @Before
    public void setBaseUrl()
        throws ConfigurationException
    {
        globalRestApiSettings.setBaseUrl( "http://localhost:8080/nexus" );
        globalRestApiSettings.commitChanges();
    }

    protected void waitFor( Condition condition )
        throws TimeoutException, InterruptedException
    {
        waitOrTimeout( condition, Timeout.timeout( seconds( 60 ) ), new ThreadSleep( millis( 30 ) ) );
    }

    @Override
    protected void customizeContainerConfiguration( final ContainerConfiguration configuration )
    {
        super.customizeContainerConfiguration( configuration );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_ON );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        initConfigurations();
        super.setUp();
        initRestApiSettings();
        injectFields();
    }

    private void injectFields()
        throws Exception, IllegalAccessException
    {
        for ( Field field : getAllFields() )
        {
            if ( field.getAnnotation( Inject.class ) != null )
            {
                lookupField( field, "" );
                continue;
            }

            Requirement requirement = field.getAnnotation( Requirement.class );
            if ( requirement != null )
            {
                lookupField( field, requirement.hint() );
            }
        }
    }

    private void lookupField( Field field, String hint )
        throws Exception, IllegalAccessException
    {
        Object value = lookup( field.getType(), hint );
        if ( !field.isAccessible() )
        {
            field.setAccessible( true );
            field.set( this, value );
            field.setAccessible( false );
        }
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        System.setProperty( TMP_DIR_KEY, oldTmpDir );
        super.tearDown();
    }

    private void initConfigurations()
    {
        oldTmpDir = System.getProperty( TMP_DIR_KEY );
        System.setProperty( TMP_DIR_KEY, BASE_CACHE_DIR.getAbsolutePath() );
    }

    private void initRestApiSettings()
        throws Exception
    {
        NexusConfiguration config = lookup( NexusConfiguration.class );
        config.loadConfiguration( true );
    }

    private List<Field> getAllFields()
    {
        List<Field> fields = new ArrayList<Field>();
        Class<?> clazz = getClass();
        do
        {
            List<? extends Field> classFields = getFields( clazz );
            fields.addAll( classFields );
            clazz = clazz.getSuperclass();
        }
        while ( !Object.class.equals( clazz ) );
        return fields;
    }

    private List<? extends Field> getFields( Class<?> clazz )
    {
        return asList( clazz.getDeclaredFields() );
    }

    private static class Marker
    {

    }

    public static void assertRepository( File repodataDir, String templateName )
        throws Exception
    {
        LOG.debug( "Testing Repo {} ...", repodataDir );
        assertThat( repodataDir, FileMatchers.exists() );
        assertRepomdXml( repodataDir, templateName );
        assertPrimaryXml( repodataDir, templateName );
    }

    private static void assertPrimaryXml( File repodataDir, String templateName )
        throws Exception
    {
        File primaryXmlFile = new File( repodataDir, PRIMARY_XML_GZ );
        LOG.debug( "Testing file {} ...", primaryXmlFile );

        GZIPInputStream gzipInputStream = new GZIPInputStream( new FileInputStream( primaryXmlFile ) );
        Diff xmlDiff =
            new Diff( createTemplateFileReader( templateName, PRIMARY_XML ), new InputStreamReader( gzipInputStream ) );
        xmlDiff.overrideDifferenceListener( new TimeStampIgnoringDifferenceListener() );
        try
        {
            assertThat( xmlDiff.toString(), xmlDiff.similar(), is( true ) );
        }
        catch ( AssertionError e )
        {
            LOG.error( "Primary.xml failed test for template {} with following content : {}", templateName,
                       IOUtils.toString( new GZIPInputStream( new FileInputStream( primaryXmlFile ) ) ) );
            throw e;
        }
    }

    private static void assertRepomdXml( File repodataDir, String templateName )
        throws Exception
    {
        Diff xmlDiff =
            new Diff( createTemplateFileReader( templateName, REPOMD_XML ), new FileReader( new File( repodataDir,
                                                                                                      REPOMD_XML ) ) );

        xmlDiff.overrideDifferenceListener( new TimeStampIgnoringDifferenceListener() );
        assertThat( xmlDiff.toString(), xmlDiff.similar(), is( true ) );
    }

    public static FileReader createTemplateFileReader( String templateName, String fileName )
        throws FileNotFoundException
    {
        return new FileReader( new File( TEMPLATE_DIR, templateName + File.separator + fileName ) );
    }

    public static File createDummyRpm( String name, String version, File outputDirectory )
        throws NoSuchAlgorithmException, IOException
    {
        Builder rpmBuilder = new Builder();
        rpmBuilder.setVendor( "IS24" );
        rpmBuilder.setGroup( "is24" );
        rpmBuilder.setPackager( "maven - " + System.getProperty( "user.name" ) );
        try
        {
            rpmBuilder.setBuildHost( InetAddress.getLocalHost().getHostName() );
        }
        catch ( UnknownHostException e )
        {
            throw new RuntimeException( "Could not determine hostname.", e );
        }
        rpmBuilder.setPackage( name, version, "1" );
        rpmBuilder.setPlatform( NOARCH, LINUX );
        rpmBuilder.setType( BINARY );
        rpmBuilder.setSourceRpm( "dummy-source-rpm-because-yum-needs-this" );

        outputDirectory.mkdirs();

        String filename = rpmBuilder.build( outputDirectory );
        return new File( outputDirectory, filename );
    }

    public static File copyToTempDir( File srcDir )
        throws IOException
    {
        final File destDir = new File( BASE_TMP_FILE, RandomStringUtils.randomAlphabetic( 20 ) );
        copyDirectory( srcDir, destDir );
        return destDir;
    }

    public static MavenRepository createRepository( final boolean isMavenHostedRepository )
    {
        return createRepository( isMavenHostedRepository, REPO );
    }

    public static MavenRepository createRepository( final boolean isMavenHostedRepository,
                                                    final String repoId )
    {
        final RepositoryKind kind = mock( RepositoryKind.class );
        when( kind.isFacetAvailable( MavenHostedRepository.class ) ).thenReturn( isMavenHostedRepository );

        final MavenHostedRepository repository = mock( MavenHostedRepository.class );
        when( repository.getRepositoryKind() ).thenReturn( kind );
        when( repository.getId() ).thenReturn( repoId );
        when( repository.getProviderRole() ).thenReturn( Repository.class.getName() );
        when( repository.getProviderHint() ).thenReturn( "maven2" );

        if ( isMavenHostedRepository )
        {
            when( repository.adaptToFacet( HostedRepository.class ) ).thenReturn( repository );
        }
        else
        {
            when( repository.adaptToFacet( HostedRepository.class ) ).thenThrow( new ClassCastException() );
        }

        final File repoDir = new File( BASE_TMP_FILE, "tmp-repos/" + repoId );
        repoDir.mkdirs();
        when( repository.getLocalUrl() ).thenReturn( repoDir.toURI().toString() );

        return repository;
    }

    public static StorageItem createItem( String version, String filename )
    {
        final StorageItem item = mock( StorageItem.class );

        when( item.getPath() ).thenReturn( "blalu/" + version + "/" + filename );
        when( item.getParentPath() ).thenReturn( "blalu/" + version );
        when( item.getItemContext() ).thenReturn( new RequestContext() );

        return item;
    }

}
