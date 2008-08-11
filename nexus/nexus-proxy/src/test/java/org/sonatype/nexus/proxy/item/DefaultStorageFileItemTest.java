/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.item;

import static org.easymock.EasyMock.*;
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
        expect( repository.createUidForPath( "/a.txt" ) ).andReturn(
            getRepositoryItemUidFactory().createUid( repository, "/a.txt" ) );
        expect( repository.retrieveItemContent( isA( RepositoryItemUid.class ) ) ).andReturn(
            new ByteArrayInputStream( "/a.txt".getBytes() ) );

        replay( repository );

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
        expect( repository.createUidForPath( "/a.txt" ) ).andReturn(
            getRepositoryItemUidFactory().createUid( repository, "/a.txt" ) );
        expect( repository.retrieveItemContent( isA( RepositoryItemUid.class ) ) ).andReturn(
            new ByteArrayInputStream( "/a.txt".getBytes() ) );

        replay( repository );
        
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
        expect( repository.createUidForPath( "/some/dir/hierarchy/a.txt" ) ).andReturn(
            getRepositoryItemUidFactory().createUid( repository, "/some/dir/hierarchy/a.txt" ) );
        expect( repository.retrieveItemContent( isA( RepositoryItemUid.class ) ) ).andReturn(
            new ByteArrayInputStream( "/some/dir/hierarchy/a.txt".getBytes() ) );

        replay( repository );
        
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
        expect( repository.createUidForPath( "/some/dir/hierarchy/a.txt" ) ).andReturn(
            getRepositoryItemUidFactory().createUid( repository, "/some/dir/hierarchy/a.txt" ) );
        expect( repository.retrieveItemContent( isA( RepositoryItemUid.class ) ) ).andReturn(
            new ByteArrayInputStream( "/some/dir/hierarchy/a.txt".getBytes() ) );

        replay( repository );
        
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
