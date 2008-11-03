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

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.utils.StoreWalker;

/**
 * @author Juven Xu
 */
public class RecreateMavenMetadataWalker
    extends StoreWalker
{

    private Repository repository;

    private boolean isHostedRepo;

    private AbstractMetadataHelper mdHelper;

    public RecreateMavenMetadataWalker( Repository repository, Logger logger )
    {
        super( repository, logger );

        this.repository = repository;

        isHostedRepo = RepositoryType.HOSTED.equals( repository.getRepositoryType() );

        mdHelper = new DefaultMetadataHelper( repository, logger );

    }

    protected void beforeWalk()
    {
        if ( !isHostedRepo )
        {
            stop( new Exception( "Not allowed to create metadata files for non-hosted repositoty" ) );
        }

    }

    @Override
    protected void onCollectionEnter( StorageCollectionItem coll )
    {

        mdHelper.onDirEnter( coll.getPath() );

    }

    @Override
    protected void onCollectionExit( StorageCollectionItem coll )
    {
        try
        {
            mdHelper.onDirExit( coll.getPath() );
        }
        catch ( Exception e )
        {
            getLogger().info( "Can't create Metadata on exit: " + coll.getPath(), e );
        }
    }

    @Override
    protected void processItem( StorageItem item )
    {
        mdHelper.processFile( item.getPath() );
    }

    public Repository getRepository()
    {
        return repository;
    }

}
