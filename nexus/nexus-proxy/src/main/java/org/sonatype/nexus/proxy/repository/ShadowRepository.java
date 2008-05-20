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
package org.sonatype.nexus.proxy.repository;

import java.util.Map;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.StoreFileWalker;

/**
 * The Class ShadowRepository.
 * 
 * @author cstamas
 */
public abstract class ShadowRepository
    extends DefaultRepository
    implements EventListener
{

    /** The master repository. */
    private Repository masterRepository;

    /**
     * Gets the master repository.
     * 
     * @return the master repository
     */
    public Repository getMasterRepository()
    {
        return masterRepository;
    }

    public RepositoryType getRepositoryType()
    {
        return RepositoryType.SHADOW;
    }

    public abstract ContentClass getMasterRepositoryContentClass();

    /**
     * Sets the master repository.
     * 
     * @param masterRepository the new master repository
     */
    public void setMasterRepository( Repository masterRepository )
    {
        if ( getMasterRepositoryContentClass().getId().equals( masterRepository.getRepositoryContentClass().getId() ) )
        {
            this.masterRepository = masterRepository;

            getMasterRepository().addProximityEventListener( this );
        }
        else
        {
            throw new IllegalArgumentException( "This shadow repository needs master repository with content class "
                + getMasterRepositoryContentClass() + ", but the passed master repository is contentClass "
                + masterRepository.getRepositoryContentClass() );
        }
    }

    /**
     * The shadow is delegating it's availability to it's master, but we can still shot down the shadow onlu.
     */
    public LocalStatus getLocalStatus()
    {
        return super.getLocalStatus().shouldServiceRequest()
            && getMasterRepository().getLocalStatus().shouldServiceRequest()
            ? LocalStatus.IN_SERVICE
            : LocalStatus.OUT_OF_SERVICE;
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( evt instanceof RepositoryItemEvent )
        {
            RepositoryItemEvent ievt = (RepositoryItemEvent) evt;
            if ( ievt.getItemUid().getPath().endsWith( ".pom" ) || ievt.getItemUid().getPath().endsWith( ".jar" ) )
            {
                try
                {
                    String tuid = transformMaster2Shadow( ievt.getItemUid().getPath() );

                    if ( ievt instanceof RepositoryItemEventStore || ievt instanceof RepositoryItemEventCache )
                    {
                        DefaultStorageLinkItem link = new DefaultStorageLinkItem( this, tuid, true, true, ievt
                            .getItemUid().toString() );
                        storeItem( link );
                    }
                    else if ( ievt instanceof RepositoryItemEventDelete )
                    {
                        deleteItem( new RepositoryItemUid( this, tuid ) );
                    }
                }
                catch ( Exception e )
                {
                    getLogger().warn( "Could not sync shadow repository because of exception", e );
                }
            }
        }
    }

    protected StorageItem doRetrieveItem( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        StorageItem result = null;

        try
        {
            result = super.doRetrieveItem( localOnly, uid, context );

            return result;
        }
        catch ( ItemNotFoundException e )
        {
            // if it is thrown by super.doRetrieveItem()
            String transformedPath = transformShadow2Master( uid.getPath() );

            if ( transformedPath == null )
            {
                throw new ItemNotFoundException( uid.getPath() );
            }

            // delegate the call to the master
            RepositoryItemUid tuid = new RepositoryItemUid( getMasterRepository(), transformedPath );

            return ( (AbstractRepository) getMasterRepository() ).doRetrieveItem( localOnly, tuid, context );
        }
    }

    public void storeItem( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            StorageException
    {
        if ( StorageLinkItem.class.isAssignableFrom( item.getClass() ) )
        {
            super.storeItem( item );
        }
        else
        {
            throw new UnsupportedOperationException( "Shadow repository may contain only links!" );
        }
    }

    /**
     * Synchronize with master.
     */
    public void synchronizeWithMaster()
    {
        getLogger().info( "Syncing shadow " + getId() + " with master repository " + getMasterRepository().getId() );

        clearCaches( RepositoryItemUid.PATH_ROOT );

        SyncWalker sw = new SyncWalker( this, getMasterRepository(), getLogger() );

        sw.walk( true, false );

        // delete all links
        // walk master repo
        // recreate links
    }

    /**
     * Gets the shadow path from master path.
     * 
     * @param path the path
     * @return the shadow path
     */
    protected abstract String transformMaster2Shadow( String path )
        throws ItemNotFoundException;

    /**
     * Gets the master path from shadow path.
     * 
     * @param path the path
     * @return the master path
     */
    protected abstract String transformShadow2Master( String path )
        throws ItemNotFoundException;

    protected class SyncWalker
        extends StoreFileWalker
    {

        private Repository repository;

        public SyncWalker( Repository repository, ResourceStore master, Logger logger )
        {
            super( master, logger );

            this.repository = repository;
        }

        @Override
        protected void processFileItem( StorageFileItem item )
        {
            if ( item.getPath().endsWith( ".pom" ) || item.getPath().endsWith( ".jar" ) )
            {
                try
                {
                    String tuid = transformMaster2Shadow( item.getRepositoryItemUid().getPath() );

                    DefaultStorageLinkItem link = new DefaultStorageLinkItem( repository, tuid, true, true, item
                        .getRepositoryItemUid().toString() );

                    storeItem( link );
                }
                catch ( Exception e )
                {
                    getLogger().warn( "Could not sync shadow repository because of exception", e );
                }
            }
        }
    };

}
