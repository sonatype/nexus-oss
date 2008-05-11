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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

/**
 * AttributeStorage implementation driven by XStream.
 * 
 * @author cstamas
 */
public class DefaultAttributesHandlerTest
    extends PlexusTestCase
{

    protected DefaultAttributesHandler attributesHandler;

    protected Repository repository;

    public void setUp()
        throws Exception
    {
        super.setUp();

        attributesHandler = (DefaultAttributesHandler) lookup( AttributesHandler.ROLE );

        FileUtils.deleteDirectory( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getWorkingDirectory() );

        repository = (Repository) lookup( Repository.ROLE, "maven2" );

        repository.setId( "dummy" );

        repository.setLocalUrl( new File( getBasedir(), "src/test/resources/repo1" ).toURI().toURL().toString() );

        DefaultFSLocalRepositoryStorage ls = (DefaultFSLocalRepositoryStorage) lookup(
            LocalRepositoryStorage.ROLE,
            "file" );

        repository.setLocalStorage( ls );
    }

    public void testRecreateAttrs()
        throws Exception
    {
        RepositoryItemUid uid = new RepositoryItemUid( repository, "/activemq/activemq-core/1.2/activemq-core-1.2.jar" );

        assertFalse( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getFileFromBase( uid, false ).exists() );
        
        repository.recreateAttributes( null );

        assertTrue( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getFileFromBase( uid, false ).exists() );
    }

    public void testRecreateAttrsWithCustomAttrs()
        throws Exception
    {
        RepositoryItemUid uid = new RepositoryItemUid( repository, "/activemq/activemq-core/1.2/activemq-core-1.2.jar" );

        assertFalse( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getFileFromBase( uid, false ).exists() );

        Map<String, String> customAttrs = new HashMap<String, String>();
        customAttrs.put( "one", "1" );
        customAttrs.put( "two", "2" );

        repository.recreateAttributes( customAttrs );

        assertTrue( ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .getFileFromBase( uid, false ).exists() );

        AbstractStorageItem item = attributesHandler.getAttributeStorage().getAttributes( uid );

        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        assertEquals( "1", item.getAttributes().get( "one" ) );

        assertEquals( "2", item.getAttributes().get( "two" ) );
    }
}
