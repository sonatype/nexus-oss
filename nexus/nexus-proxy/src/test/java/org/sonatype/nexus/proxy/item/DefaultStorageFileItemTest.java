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
package org.sonatype.nexus.proxy.item;

import static org.easymock.EasyMock.expect;
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
        expect( repository.createUid( "/a.txt" ) ).andReturn( new DefaultRepositoryItemUid( repository, "/a.txt" ) );

        replay( repository );

        getRepositoryItemUidFactory().createUid( repository, "/a.txt" );

        //

        DefaultStorageFileItem file = new DefaultStorageFileItem(
            repository,
            "/a.txt",
            true,
            true,
            new StringContentLocator( "/a.txt" ) );
        checkAbstractStorageItem( repository, file, false, "a.txt", "/a.txt", "/" );

        // content
        InputStream is = file.getInputStream();
        assertEquals( true, IOUtil.contentEquals( is, new ByteArrayInputStream( file
            .getRepositoryItemUid().getPath().getBytes() ) ) );
    }

    public void testNonVirtualFileWithContentSimple()
        throws Exception
    {
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();
        expect( repository.createUid( "/a.txt" ) ).andReturn( new DefaultRepositoryItemUid( repository, "/a.txt" ) );

        replay( repository );

        getRepositoryItemUidFactory().createUid( repository, "/a.txt" );

        //

        DefaultStorageFileItem file = new DefaultStorageFileItem(
            repository,
            "/a.txt",
            true,
            true,
            new StringContentLocator( "THIS IS CONTENT" ) );
        checkAbstractStorageItem( repository, file, false, "a.txt", "/a.txt", "/" );

        // content
        InputStream fis = file.getInputStream();
        assertEquals( true, IOUtil.contentEquals( fis, new ByteArrayInputStream( "THIS IS CONTENT".getBytes() ) ) );
    }

    public void testNonVirtualFileDeep()
        throws Exception
    {
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();
        expect( repository.createUid( "/some/dir/hierarchy/a.txt" ) ).andReturn(
            new DefaultRepositoryItemUid( repository, "/some/dir/hierarchy/a.txt" ) );

        replay( repository );

        getRepositoryItemUidFactory().createUid( repository, "/some/dir/hierarchy/a.txt" );

        //

        DefaultStorageFileItem file = new DefaultStorageFileItem(
            repository,
            "/some/dir/hierarchy/a.txt",
            true,
            true,
            new StringContentLocator( "/some/dir/hierarchy/a.txt" ) );
        checkAbstractStorageItem( repository, file, false, "a.txt", "/some/dir/hierarchy/a.txt", "/some/dir/hierarchy" );

        // content
        InputStream is = file.getInputStream();
        assertEquals( true, IOUtil.contentEquals( is, new ByteArrayInputStream( file
            .getRepositoryItemUid().getPath().getBytes() ) ) );
    }

    public void testNonVirtualFileWithContentDeep()
        throws Exception
    {
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();
        expect( repository.createUid( "/some/dir/hierarchy/a.txt" ) ).andReturn(
            new DefaultRepositoryItemUid( repository, "/some/dir/hierarchy/a.txt" ) );

        replay( repository );

        getRepositoryItemUidFactory().createUid( repository, "/some/dir/hierarchy/a.txt" );

        //

        DefaultStorageFileItem file = new DefaultStorageFileItem(
            repository,
            "/some/dir/hierarchy/a.txt",
            true,
            true,
            new StringContentLocator( "THIS IS CONTENT" ) );
        checkAbstractStorageItem( repository, file, false, "a.txt", "/some/dir/hierarchy/a.txt", "/some/dir/hierarchy" );

        // content
        InputStream fis = file.getInputStream();
        assertEquals( true, IOUtil.contentEquals( fis, new ByteArrayInputStream( "THIS IS CONTENT".getBytes() ) ) );
    }

}
