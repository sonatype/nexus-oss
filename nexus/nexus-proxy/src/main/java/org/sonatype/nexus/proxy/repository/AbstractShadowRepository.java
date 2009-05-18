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

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.plexus.appevents.Event;

/**
 * The Class ShadowRepository.
 * 
 * @author cstamas
 */
public abstract class AbstractShadowRepository
    extends AbstractRepository
    implements ShadowRepository
{
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Override
    protected AbstractShadowRepositoryConfiguration getExternalConfiguration()
    {
        return (AbstractShadowRepositoryConfiguration) super.getExternalConfiguration();
    }

    public String getMasterRepositoryId()
    {
        return getExternalConfiguration().getMasterRepositoryId();
    }

    public void setMasterRepositoryId( String id )
        throws NoSuchRepositoryException,
            IncompatibleMasterRepositoryException
    {
        setMasterRepository( repositoryRegistry.getRepository( id ) );
    }

    public Repository getMasterRepository()
    {
        try
        {
            return repositoryRegistry.getRepository( getExternalConfiguration().getMasterRepositoryId() );
        }
        catch ( NoSuchRepositoryException e )
        {
            // erm?

            getLogger().warn(
                "ShadowRepository ID='" + getId() + "' cannot fetch it's master repository with ID='"
                    + getMasterRepositoryId() + "'!",
                e );

            return null;
        }
    }

    public boolean isSynchronizeAtStartup()
    {
        return getExternalConfiguration().isSynchronizeAtStartup();
    }

    public void setSynchronizeAtStartup( boolean val )
    {
        getExternalConfiguration().setSynchronizeAtStartup( val );
    }

    public void setMasterRepository( Repository masterRepository )
        throws IncompatibleMasterRepositoryException
    {
        if ( getMasterRepositoryContentClass().getId().equals( masterRepository.getRepositoryContentClass().getId() ) )
        {
            getExternalConfiguration().setMasterRepositoryId( masterRepository.getId() );
        }
        else
        {
            throw new IncompatibleMasterRepositoryException( this, masterRepository.getId() );
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
    public void onEvent( Event evt )
    {
        super.onEvent( evt );

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
                        createLink( ievt.getItem() );
                    }
                    else if ( ievt instanceof RepositoryItemEventDelete )
                    {
                        deleteLink( ievt.getItem() );
                    }
                }
                catch ( Exception e )
                {
                    getLogger().warn( "Could not sync shadow repository because of exception", e );
                }
            }
        }
    }

    protected abstract void deleteLink( StorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException;

    protected abstract void createLink( StorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException;

    protected void synchronizeLink( StorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException
    {
        createLink( item );
    }

    /**
     * Synchronize with master.
     */
    public void synchronizeWithMaster()
    {
        getLogger().info( "Syncing shadow " + getId() + " with master repository " + getMasterRepository().getId() );

        ResourceStoreRequest root = new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true );

        expireCaches( root );

        AbstractFileWalkerProcessor sw = new AbstractFileWalkerProcessor()
        {
            @Override
            protected void processFileItem( WalkerContext context, StorageFileItem item )
                throws Exception
            {
                synchronizeLink( item );
            }
        };

        DefaultWalkerContext ctx = new DefaultWalkerContext( getMasterRepository(), root );

        ctx.getProcessors().add( sw );

        try
        {
            getWalker().walk( ctx );
        }
        catch ( WalkerException e )
        {
            if ( !( e.getWalkerContext().getStopCause() instanceof ItemNotFoundException ) )
            {
                // everything that is not ItemNotFound should be reported,
                // otherwise just neglect it
                throw e;
            }
        }
    }

    protected StorageItem doRetrieveItemFromMaster( ResourceStoreRequest request )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        return ( (AbstractRepository) getMasterRepository() ).doRetrieveItem( request );
    }

}
