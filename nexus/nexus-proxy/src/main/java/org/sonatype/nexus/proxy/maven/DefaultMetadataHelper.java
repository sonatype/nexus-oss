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

package org.sonatype.nexus.proxy.maven;

import java.io.InputStream;

import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Default MetadataHelper in Nexus, works based on a Repository.
 * 
 * @author Juven Xu
 */
public class DefaultMetadataHelper
    extends AbstractMetadataHelper
{
    private Repository repository;

    public DefaultMetadataHelper( Repository repository )
    {
        this.repository = repository;
    }

    @Override
    public void store( String metadata, String path )
        throws Exception
    {
        // UIDs are like URIs! The separator is _always_ "/"!!!
        RepositoryItemUid mdUid = repository.createUid( path + "/maven-metadata.xml" );

        ContentLocator contentLocator = new StringContentLocator( metadata );

        DefaultStorageFileItem mdFile = new DefaultStorageFileItem(
            repository,
            mdUid.getPath(),
            true,
            true,
            contentLocator );

        repository.storeItem( mdFile );

        repository.removeFromNotFoundCache( mdUid.getPath() );
    }

    @Override
    public InputStream retrieveContent( String path )
        throws Exception
    {
        RepositoryItemUid uid = repository.createUid( path );

        return repository.retrieveItemContent( uid );
    }

}
