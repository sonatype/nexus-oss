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
package org.sonatype.nexus.proxy.attributes;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.Repository;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

/**
 * AttributeStorage implementation driven by XStream.
 * 
 * @author cstamas
 */
@BenchmarkHistoryChart( )
@BenchmarkMethodChart( )
@AxisRange( min = 0 )
public class DefaultAttributeStorageTest
    extends AbstractNexusTestEnvironment
{

    protected AttributeStorage attributeStorage;

    protected RepositoryItemUidFactory repositoryItemUidFactory;

    protected Repository repository;

    protected File localStorageDirectory;

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        attributeStorage = lookup( AttributeStorage.class, "fs" );

        repositoryItemUidFactory = lookup( RepositoryItemUidFactory.class );

        repository = lookup( Repository.class, "maven2" );

        CRepository repoConf = new DefaultCRepository();

        repoConf.setProviderRole( Repository.class.getName() );
        repoConf.setProviderHint( "maven2" );
        repoConf.setId( "dummy" );

        repoConf.setLocalStorage( new CLocalStorage() );
        repoConf.getLocalStorage().setProvider( "file" );
        localStorageDirectory = new File( getBasedir(), "target/test-reposes/repo1" );
        repoConf.getLocalStorage().setUrl( localStorageDirectory.toURI().toURL().toString() );

        Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
        repoConf.setExternalConfiguration( exRepo );
        M2RepositoryConfiguration exRepoConf = new M2RepositoryConfiguration( exRepo );
        exRepoConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        exRepoConf.setChecksumPolicy( ChecksumPolicy.STRICT_IF_EXISTS );

        if ( attributeStorage instanceof DefaultFSAttributeStorage )
        {
            FileUtils.deleteDirectory( ( (DefaultFSAttributeStorage) attributeStorage ).getWorkingDirectory() );
        }
        else if ( attributeStorage instanceof DefaultLSAttributeStorage )
        {
            FileUtils.deleteDirectory( new File( localStorageDirectory, ".nexus/attributes" ) );
        }

        repository.configure( repoConf );
    }

    @Test
    public void testSimplePutGet()
        throws Exception
    {
        DefaultStorageFileItem file =
            new DefaultStorageFileItem( repository, new ResourceStoreRequest( "/a.txt" ), true, true,
                new StringContentLocator( "CONTENT" ) );

        file.getAttributes().put( "kuku", "kuku" );

        attributeStorage.putAttributes( file.getRepositoryItemUid(), file.getRepositoryItemAttributes() );

        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid( repository, "/a.txt" );
        Attributes file1 = attributeStorage.getAttributes( uid );

        assertTrue( file1.containsKey( "kuku" ) );
        assertTrue( "kuku".equals( file1.get( "kuku" ) ) );
    }

    @Test
    public void testSimplePutGetNEXUS3911()
        throws Exception
    {
        DefaultStorageFileItem file =
            new DefaultStorageFileItem( repository, new ResourceStoreRequest( "/a.txt" ), true, true,
                new StringContentLocator( "CONTENT" ) );

        file.getAttributes().put( "kuku", "kuku" );

        attributeStorage.putAttributes( file.getRepositoryItemUid(), file.getRepositoryItemAttributes() );

        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid( repository, "/a.txt" );
        Attributes file1 = attributeStorage.getAttributes( uid );

        assertTrue( file1.containsKey( "kuku" ) );
        assertTrue( "kuku".equals( file1.get( "kuku" ) ) );

        // this above is same as in testSimplePutGet(), but now we will replace the attribute file

        // reverted back to "old" attributes
        File attributeFile =
            new File( ( (DefaultFSAttributeStorage) attributeStorage ).getWorkingDirectory(), repository.getId()
                + "/a.txt" );
        // File attributeFile = new File( localStorageDirectory, ".nexus/attributes/a.txt" );

        FileUtils.fileWrite( attributeFile.getAbsolutePath(), "<file" );

        // try to read it, we should not get NPE
        try
        {
            file1 = attributeStorage.getAttributes( uid );
        }
        catch ( NullPointerException e )
        {
            fail( "We should not get NPE!" );
        }

        assertNull( "file1 is corrupt, hence it should be null!", file1 );
    }

    @Test
    public void testSimplePutDelete()
        throws Exception
    {
        DefaultStorageFileItem file =
            new DefaultStorageFileItem( repository, new ResourceStoreRequest( "/b.txt" ), true, true,
                new StringContentLocator( "CONTENT" ) );

        file.getAttributes().put( "kuku", "kuku" );

        attributeStorage.putAttributes( file.getRepositoryItemUid(), file.getRepositoryItemAttributes() );

        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid( repository, "/b.txt" );

        assertNotNull( attributeStorage.getAttributes( uid ) );

        assertTrue( attributeStorage.deleteAttributes( uid ) );

        assertNull( attributeStorage.getAttributes( uid ) );
    }
}
