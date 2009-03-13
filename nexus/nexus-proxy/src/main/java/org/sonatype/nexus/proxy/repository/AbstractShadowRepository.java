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
package org.sonatype.nexus.proxy.repository;

import java.util.Map;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
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
        }
        else
        {
            throw new IncompatibleMasterRepositoryException( this, masterRepository );
        }
    }

    /**
     * The shadow is delegating it's availability to it's master, but we can still shot down the shadow onlu.
     */
    @Override
    public LocalStatus getLocalStatus()
    {
        return super.getLocalStatus().shouldServiceRequest()
            && getMasterRepository().getLocalStatus().shouldServiceRequest()
            ? LocalStatus.IN_SERVICE
            : LocalStatus.OUT_OF_SERVICE;
    }

    @Override
    public void onProximityEvent( AbstractEvent evt )
    {
        super.onProximityEvent( evt );

        if ( evt instanceof RepositoryItemEvent )
        {
            RepositoryItemEvent ievt = (RepositoryItemEvent) evt;

            // is this event coming from our master?
            if ( getMasterRepository() == ievt.getRepository() )
            {
                try
                {
                    if ( ievt instanceof RepositoryItemEventStore || ievt instanceof RepositoryItemEventCache )
                    {
                        createLink( ievt.getItem(), ievt.getContext() );
                    }
                    else if ( ievt instanceof RepositoryItemEventDelete )
                    {
                        deleteLink( ievt.getItem(), ievt.getContext() );
                    }
                }
                catch ( Exception e )
                {
                    getLogger().warn( "Could not sync shadow repository because of exception", e );
                }
            }
        }
    }

    protected abstract void deleteLink( StorageItem item, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException;

    protected abstract void createLink( StorageItem item, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException;

    protected void synchronizeLink( StorageItem item, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException
    {
        createLink( item, context );
    }

    /**
     * Synchronize with master.
     */
    public void synchronizeWithMaster()
    {
        getLogger().info( "Syncing shadow " + getId() + " with master repository " + getMasterRepository().getId() );

        ResourceStoreRequest root = new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true );

        clearCaches( root );

        AbstractFileWalkerProcessor sw = new AbstractFileWalkerProcessor()
        {
            @Override
            protected void processFileItem( WalkerContext context, StorageFileItem item )
                throws Exception
            {
                synchronizeLink( item, context.getContext() );
            }
        };

        DefaultWalkerContext ctx = new DefaultWalkerContext( getMasterRepository(), root );

        ctx.getProcessors().add( sw );

        getWalker().walk( ctx );
    }

    protected StorageItem doRetrieveItemFromMaster( RepositoryRequest request )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        return ( (AbstractRepository) getMasterRepository() ).doRetrieveItem( request );
    }

}
