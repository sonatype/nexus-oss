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
package org.sonatype.nexus.proxy.attributes;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
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

/**
 * AttributeStorage implementation driven by XStream.
 * 
 * @author cstamas
 */
public class DefaultAttributeStorageTest
    extends AbstractNexusTestEnvironment
{

    protected AttributeStorage attributeStorage;

    protected RepositoryItemUidFactory repositoryItemUidFactory;

    protected Repository repository;

    protected File localStorageDirectory;

    public void setUp()
        throws Exception
    {
        super.setUp();

        attributeStorage = lookup( AttributeStorage.class );

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
        else
        {
            FileUtils.deleteDirectory( new File( localStorageDirectory, ".nexus/attributes" ) );
        }

        repository.configure( repoConf );
    }

    public void testSimplePutGet()
        throws Exception
    {
        DefaultStorageFileItem file =
            new DefaultStorageFileItem( repository, new ResourceStoreRequest( "/a.txt" ), true, true,
                new StringContentLocator( "CONTENT" ) );

        file.getAttributes().put( "kuku", "kuku" );

        attributeStorage.putAttribute( file );

        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid( repository, "/a.txt" );
        DefaultStorageFileItem file1 = (DefaultStorageFileItem) attributeStorage.getAttributes( uid );

        assertTrue( file1.getAttributes().containsKey( "kuku" ) );
        assertTrue( "kuku".equals( file1.getAttributes().get( "kuku" ) ) );
    }

    public void testSimplePutGetNEXUS3911()
        throws Exception
    {
        DefaultStorageFileItem file =
            new DefaultStorageFileItem( repository, new ResourceStoreRequest( "/a.txt" ), true, true,
                new StringContentLocator( "CONTENT" ) );

        file.getAttributes().put( "kuku", "kuku" );

        attributeStorage.putAttribute( file );

        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid( repository, "/a.txt" );
        DefaultStorageFileItem file1 = (DefaultStorageFileItem) attributeStorage.getAttributes( uid );

        assertTrue( file1.getAttributes().containsKey( "kuku" ) );
        assertTrue( "kuku".equals( file1.getAttributes().get( "kuku" ) ) );

        // this above is same as in testSimplePutGet(), but now we will replace the attribute file

        // reverted back to "old" attributes
        File attributeFile = new File( ((DefaultFSAttributeStorage)attributeStorage).getWorkingDirectory(), repository.getId() + "/a.txt" );
        // File attributeFile = new File( localStorageDirectory, ".nexus/attributes/a.txt" );
        
        FileUtils.fileWrite( attributeFile.getAbsolutePath(), "<file" );

        // try to read it, we should not get NPE
        try
        {
            file1 = (DefaultStorageFileItem) attributeStorage.getAttributes( uid );
        }
        catch ( NullPointerException e )
        {
            fail( "We should not get NPE!" );
        }

        assertNull( "file1 is corrupt, hence it should be null!", file1 );
    }

    public void testSimplePutDelete()
        throws Exception
    {
        DefaultStorageFileItem file =
            new DefaultStorageFileItem( repository, new ResourceStoreRequest( "/b.txt" ), true, true,
                new StringContentLocator( "CONTENT" ) );

        file.getAttributes().put( "kuku", "kuku" );

        attributeStorage.putAttribute( file );

        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid( repository, "/b.txt" );

        assertNotNull( attributeStorage.getAttributes( uid ) );

        assertTrue( attributeStorage.deleteAttributes( uid ) );

        assertNull( attributeStorage.getAttributes( uid ) );
    }
}
