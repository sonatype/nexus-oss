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
package org.sonatype.nexus.proxy.attributes;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultRepositoryItemUid;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * AttributeStorage implementation driven by XStream.
 * 
 * @author cstamas
 */
public class DefaultAttributeStorageTest
    extends AbstractNexusTestEnvironment
{

    protected DefaultAttributeStorage attributeStorage;

    protected Repository repository;

    public void setUp()
        throws Exception
    {
        super.setUp();

        attributeStorage = (DefaultAttributeStorage) lookup( AttributeStorage.class );

        FileUtils.deleteDirectory( attributeStorage.getWorkingDirectory() );

        repository = createMock( Repository.class );

        RepositoryItemUid uid = createMock( RepositoryItemUid.class );

        expect( uid.getRepository() ).andReturn( repository ).anyTimes();
        expect( uid.getPath() ).andReturn( "/a.txt" ).anyTimes();

        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();

        expect( repository.createUid( "/a.txt" ) ).andReturn( new DefaultRepositoryItemUid( repository, "/a.txt" ) );
        expect( repository.createUid( "/b.txt" ) ).andReturn( new DefaultRepositoryItemUid( repository, "/b.txt" ) );

        replay( repository );

        getRepositoryItemUidFactory().createUid( repository, "/a.txt" );
        getRepositoryItemUidFactory().createUid( repository, "/b.txt" );

        replay( uid );
    }

    public void testSimplePutGet()
        throws Exception
    {
        DefaultStorageFileItem file = new DefaultStorageFileItem(
            repository,
            new ResourceStoreRequest( "/a.txt" ),
            true,
            true,
            new StringContentLocator( "CONTENT" ) );

        file.getAttributes().put( "kuku", "kuku" );

        attributeStorage.putAttribute( file );

        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid( repository, "/a.txt" );
        DefaultStorageFileItem file1 = (DefaultStorageFileItem) attributeStorage.getAttributes( uid );

        assertTrue( file1.getAttributes().containsKey( "kuku" ) );
        assertTrue( "kuku".equals( file1.getAttributes().get( "kuku" ) ) );
    }

    public void testSimplePutDelete()
        throws Exception
    {
        DefaultStorageFileItem file = new DefaultStorageFileItem(
            repository,
            new ResourceStoreRequest( "/b.txt" ),
            true,
            true,
            new StringContentLocator( "CONTENT" ) );

        file.getAttributes().put( "kuku", "kuku" );

        attributeStorage.putAttribute( file );

        RepositoryItemUid uid = getRepositoryItemUidFactory().createUid( repository, "/b.txt" );

        assertTrue( attributeStorage.getFileFromBase( uid ).exists() );

        assertTrue( attributeStorage.deleteAttributes( uid ) );

        assertFalse( attributeStorage.getFileFromBase( uid ).exists() );
    }
}
