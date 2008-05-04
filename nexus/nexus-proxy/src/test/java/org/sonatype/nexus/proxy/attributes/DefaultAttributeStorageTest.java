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
package org.sonatype.nexus.proxy.attributes;

import java.io.ByteArrayInputStream;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.DummyRepository;

/**
 * AttributeStorage implementation driven by XStream.
 * 
 * @author cstamas
 */
public class DefaultAttributeStorageTest
    extends PlexusTestCase
{

    protected DefaultAttributeStorage attributeStorage;

    public void setUp()
        throws Exception
    {
        super.setUp();

        attributeStorage = (DefaultAttributeStorage) lookup( AttributeStorage.ROLE );

        FileUtils.deleteDirectory( attributeStorage.getWorkingDirectory() );
    }

    public void testSimplePutGet()
        throws Exception
    {
        DummyRepository dummy = new DummyRepository();
        dummy.setId( "dummy" );

        DefaultStorageFileItem file = new DefaultStorageFileItem(
            dummy,
            "/a.txt",
            true,
            true,
            new ByteArrayInputStream( "CONTENT".getBytes() ) );

        file.getAttributes().put( "kuku", "kuku" );

        attributeStorage.putAttribute( file );

        RepositoryItemUid uid = new RepositoryItemUid( dummy, "/a.txt" );
        DefaultStorageFileItem file1 = (DefaultStorageFileItem) attributeStorage.getAttributes( uid );

        assertTrue( file1.getAttributes().containsKey( "kuku" ) );
        assertTrue( "kuku".equals( file1.getAttributes().get( "kuku" ) ) );
    }

    public void testSimplePutDelete()
        throws Exception
    {
        DummyRepository dummy = new DummyRepository();
        dummy.setId( "dummy" );

        DefaultStorageFileItem file = new DefaultStorageFileItem(
            dummy,
            "/b.txt",
            true,
            true,
            new ByteArrayInputStream( "CONTENT".getBytes() ) );

        file.getAttributes().put( "kuku", "kuku" );

        attributeStorage.putAttribute( file );

        RepositoryItemUid uid = new RepositoryItemUid( dummy, "/b.txt" );

        assertTrue( attributeStorage.getFileFromBase( uid, false ).exists() );

        assertTrue( attributeStorage.deleteAttributes( uid ) );

        assertFalse( attributeStorage.getFileFromBase( uid, false ).exists() );
    }
}
