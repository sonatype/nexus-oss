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

import java.util.Collection;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;

/**
 * @author Juven Xu
 */
public class RecreateMavenMetadataWalkerProcessor
    extends AbstractWalkerProcessor
{
    private boolean isHostedRepo;

    private MavenRepository repository;

    private AbstractMetadataHelper mdHelper;

    @Override
    public void beforeWalk( WalkerContext context )
        throws Exception
    {
        isHostedRepo = false;

        repository = context.getResourceStore() instanceof MavenRepository ? (MavenRepository) context
            .getResourceStore() : null;

        if ( repository != null )
        {
            mdHelper = new DefaultMetadataHelper( repository );

            isHostedRepo = RepositoryType.HOSTED.equals( repository.getRepositoryType() );
        }

        setActive( isHostedRepo );
    }

    @Override
    public void processItem( WalkerContext context, StorageItem item )
        throws Exception
    {
        // nothing
    }

    @Override
    public void onCollectionExit( WalkerContext context, StorageCollectionItem coll )
        throws Exception
    {
        mdHelper.onDirEnter( coll.getPath() );

        Collection<StorageItem> items = repository.list( coll );

        for ( StorageItem item : items )
        {
            if ( item instanceof StorageFileItem )
            {
                mdHelper.processFile( item.getPath() );
            }
        }

        mdHelper.onDirExit( coll.getPath() );
    }
}
