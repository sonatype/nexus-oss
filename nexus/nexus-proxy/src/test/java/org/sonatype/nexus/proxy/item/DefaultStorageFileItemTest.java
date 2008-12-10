/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.item;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;

public class DefaultStorageFileItemTest
    extends AbstractStorageItemTest
{
    public void testNonVirtualFileSimple()
        throws Exception
    {
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();
        expect( repository.createUid( "/a.txt" ) ).andReturn(
            new DefaultRepositoryItemUid( repository, "/a.txt" ) );
        expect( repository.retrieveItemContent( isA( RepositoryItemUid.class ) ) ).andReturn(
            new ByteArrayInputStream( "/a.txt".getBytes() ) );

        replay( repository );
        
        getRepositoryItemUidFactory().createUid( repository, "/a.txt" );
        
        //

        DefaultStorageFileItem file = new DefaultStorageFileItem( repository, "/a.txt", true, true );
        checkAbstractStorageItem( repository, file, false, "a.txt", "/a.txt", "/" );

        // content
        InputStream is = repository.retrieveItemContent( file.getRepositoryItemUid() );
        assertEquals( true, IOUtil.contentEquals( is, new ByteArrayInputStream( file
            .getRepositoryItemUid().getPath().getBytes() ) ) );
    }

    public void testNonVirtualFileWithContentSimple()
        throws Exception
    {
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();
        expect( repository.createUid( "/a.txt" ) ).andReturn(
            new DefaultRepositoryItemUid( repository, "/a.txt" ) );
        expect( repository.retrieveItemContent( isA( RepositoryItemUid.class ) ) ).andReturn(
            new ByteArrayInputStream( "/a.txt".getBytes() ) );

        replay( repository );

        getRepositoryItemUidFactory().createUid( repository, "/a.txt" );
        
        //
        
        DefaultStorageFileItem file = new DefaultStorageFileItem(
            repository,
            "/a.txt",
            true,
            true,
            new ByteArrayInputStream( "THIS IS CONTENT".getBytes() ) );
        checkAbstractStorageItem( repository, file, false, "a.txt", "/a.txt", "/" );

        // content
        InputStream ris = repository.retrieveItemContent( file.getRepositoryItemUid() );
        InputStream fis = file.getInputStream();
        assertEquals( true, IOUtil.contentEquals( ris, new ByteArrayInputStream( file
            .getRepositoryItemUid().getPath().getBytes() ) ) );
        assertEquals( true, IOUtil.contentEquals( fis, new ByteArrayInputStream( "THIS IS CONTENT".getBytes() ) ) );
    }

    public void testNonVirtualFileDeep()
        throws Exception
    {
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();
        expect( repository.createUid( "/some/dir/hierarchy/a.txt" ) ).andReturn(
            new DefaultRepositoryItemUid( repository, "/some/dir/hierarchy/a.txt" ) );
        expect( repository.retrieveItemContent( isA( RepositoryItemUid.class ) ) ).andReturn(
            new ByteArrayInputStream( "/some/dir/hierarchy/a.txt".getBytes() ) );

        replay( repository );
        
        getRepositoryItemUidFactory().createUid( repository, "/some/dir/hierarchy/a.txt"  );

        //
        
        DefaultStorageFileItem file = new DefaultStorageFileItem( repository, "/some/dir/hierarchy/a.txt", true, true );
        checkAbstractStorageItem( repository, file, false, "a.txt", "/some/dir/hierarchy/a.txt", "/some/dir/hierarchy" );

        // content
        InputStream is = repository.retrieveItemContent( file.getRepositoryItemUid() );
        assertEquals( true, IOUtil.contentEquals( is, new ByteArrayInputStream( file
            .getRepositoryItemUid().getPath().getBytes() ) ) );
    }

    public void testNonVirtualFileWithContentDeep()
        throws Exception
    {
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();
        expect( repository.createUid( "/some/dir/hierarchy/a.txt" ) ).andReturn(
            new DefaultRepositoryItemUid( repository, "/some/dir/hierarchy/a.txt" ) );
        expect( repository.retrieveItemContent( isA( RepositoryItemUid.class ) ) ).andReturn(
            new ByteArrayInputStream( "/some/dir/hierarchy/a.txt".getBytes() ) );

        replay( repository );
        
        getRepositoryItemUidFactory().createUid( repository, "/some/dir/hierarchy/a.txt"  );

        //
        
        DefaultStorageFileItem file = new DefaultStorageFileItem(
            repository,
            "/some/dir/hierarchy/a.txt",
            true,
            true,
            new ByteArrayInputStream( "THIS IS CONTENT".getBytes() ) );
        checkAbstractStorageItem( repository, file, false, "a.txt", "/some/dir/hierarchy/a.txt", "/some/dir/hierarchy" );

        // content
        InputStream ris = repository.retrieveItemContent( file.getRepositoryItemUid() );
        InputStream fis = file.getInputStream();
        assertEquals( true, IOUtil.contentEquals( ris, new ByteArrayInputStream( file
            .getRepositoryItemUid().getPath().getBytes() ) ) );
        assertEquals( true, IOUtil.contentEquals( fis, new ByteArrayInputStream( "THIS IS CONTENT".getBytes() ) ) );
    }

}
