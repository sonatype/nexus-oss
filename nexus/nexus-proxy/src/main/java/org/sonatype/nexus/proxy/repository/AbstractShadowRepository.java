/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.repository;

import java.util.Map;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.AbstractEvent;
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
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerContext;

/**
 * The Class ShadowRepository.
 * 
 * @author cstamas
 */
public abstract class AbstractShadowRepository
    extends AbstractRepository
    implements ShadowRepository
{
    /** The master repository. */
    private Repository masterRepository;

    private RepositoryKind repositoryKind = new DefaultRepositoryKind( ShadowRepository.class, null );

    /**
     * Gets the master repository.
     * 
     * @return the master repository
     */
    public Repository getMasterRepository()
    {
        return masterRepository;
    }

    public RepositoryKind getRepositoryKind()
    {
        return repositoryKind;
    }

    /**
     * Sets the master repository.
     * 
     * @param masterRepository the new master repository
     */
    public void setMasterRepository( Repository masterRepository )
        throws IncompatibleMasterRepositoryException
    {
        if ( getMasterRepositoryContentClass().getId().equals( masterRepository.getRepositoryContentClass().getId() ) )
        {
            this.masterRepository = masterRepository;

            getMasterRepository().addProximityEventListener( this );
        }
        else
        {
            throw new IncompatibleMasterRepositoryException( this, masterRepository );
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

            try
            {
                String shadowPath = transformMaster2Shadow( ievt.getItemUid().getPath() );

                if ( shadowPath != null )
                {
                    if ( ievt instanceof RepositoryItemEventStore || ievt instanceof RepositoryItemEventCache )
                    {
                        DefaultStorageLinkItem link = new DefaultStorageLinkItem( this, shadowPath, true, true, ievt
                            .getItemUid() );

                        if ( ievt.getContext() != null )
                        {
                            link.getItemContext().putAll( ievt.getContext() );
                        }

                        storeItem( link );
                    }
                    else if ( ievt instanceof RepositoryItemEventDelete )
                    {
                        deleteItem( createUid( shadowPath ), ievt.getContext() );
                    }
                }
            }
            catch ( Exception e )
            {
                getLogger().warn( "Could not sync shadow repository because of exception", e );
            }
        }
    }

    protected StorageItem doRetrieveItem( RepositoryItemUid uid, Map<String, Object> context )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        StorageItem result = null;

        try
        {
            result = super.doRetrieveItem( uid, context );

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
            RepositoryItemUid tuid = getMasterRepository().createUid( transformedPath );

            return ( (AbstractRepository) getMasterRepository() ).doRetrieveItem( tuid, context );
        }
    }

    public void storeItem( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
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

        SyncWalker sw = new SyncWalker( this );

        DefaultWalkerContext ctx = new DefaultWalkerContext( getMasterRepository() );

        ctx.getProcessors().add( sw );

        getWalker().walk( ctx );
    }

    /**
     * Gets the shadow path from master path. If path is not transformable, return null.
     * 
     * @param path the path
     * @return the shadow path
     */
    protected abstract String transformMaster2Shadow( String path );

    /**
     * Gets the master path from shadow path. If path is not transformable, return null.
     * 
     * @param path the path
     * @return the master path
     */
    protected abstract String transformShadow2Master( String path );

    protected class SyncWalker
        extends AbstractFileWalkerProcessor
    {
        private Repository repository;

        public SyncWalker( Repository repository )
        {
            this.repository = repository;
        }

        @Override
        protected void processFileItem( WalkerContext ctx, StorageFileItem item )
            throws Exception
        {
            String tuid = transformMaster2Shadow( item.getRepositoryItemUid().getPath() );

            if ( tuid != null )
            {
                DefaultStorageLinkItem link = new DefaultStorageLinkItem( repository, tuid, true, true, item
                    .getRepositoryItemUid() );

                storeItem( link );
            }
        }
    };

}
