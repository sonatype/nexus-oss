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
package org.sonatype.nexus.proxy.item;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

/**
 * The Class AbstractStorageItem.
 * 
 * @author cstamas
 */
public abstract class AbstractStorageItem
    implements StorageItem
{
    /** The repository item uid. */
    private transient RepositoryItemUid repositoryItemUid;

    /** The store. */
    private transient ResourceStore store;

    /** The trasient context. */
    private transient Map<String, Object> context;

    /** Used for versioning of attribute */
    private int generation = 0;

    /** The path. */
    private String path;

    /** The readable. */
    private boolean readable;

    /** The writable. */
    private boolean writable;

    /** The repository id. */
    private String repositoryId;

    /** The created. */
    private long created;

    /** The modified. */
    private long modified;

    /** The stored locally. */
    private long storedLocally;

    /** The last remoteCheck timestamp. */
    // TODO: leave the field name as-is coz of persistence and old nexuses!
    private long lastTouched;

    /** The last requested timestamp. */
    private long lastRequested;

    /** Expired flag */
    private boolean expired;

    /** The remote url. */
    private String remoteUrl;

    /** The persisted attributes. */
    private Map<String, String> attributes;

    /**
     * Instantiates a new abstract storage item.
     * 
     * @param path the path
     * @param readable the readable
     * @param writable the writable
     */
    public AbstractStorageItem( String path, boolean readable, boolean writable )
    {
        super();
        setPath( path );
        this.readable = readable;
        this.writable = writable;
        this.expired = false;
        this.created = System.currentTimeMillis();
        this.modified = this.created;
    }

    /**
     * Instantiates a new abstract storage item.
     * 
     * @param repository the repository
     * @param path the path
     * @param readable the readable
     * @param writable the writable
     */
    public AbstractStorageItem( Repository repository, String path, boolean readable, boolean writable )
    {
        this( path, readable, writable );
        this.store = repository;
        this.repositoryItemUid = repository.createUid( path );
        this.repositoryId = repository.getId();
    }

    /**
     * Instantiates a new abstract storage item.
     * 
     * @param router the router
     * @param path the path
     * @param virtual the virtual
     * @param readable the readable
     * @param writable the writable
     */
    public AbstractStorageItem( RepositoryRouter router, String path, boolean readable, boolean writable )
    {
        this( path, readable, writable );
        this.store = router;
        this.repositoryItemUid = null;
        this.repositoryId = null;
    }

    /**
     * Gets the store.
     * 
     * @return the store
     */
    public ResourceStore getStore()
    {
        return this.store;
    }

    /**
     * Sets the store.
     * 
     * @param store
     */
    public void setStore( ResourceStore store )
    {
        // only allow this when we are virtual!
        if ( isVirtual() )
        {
            this.store = store;
        }
    }

    public RepositoryItemUid getRepositoryItemUid()
    {
        return repositoryItemUid;
    }

    /**
     * Sets the UID.
     * 
     * @param repositoryItemUid
     */
    public void setRepositoryItemUid( RepositoryItemUid repositoryItemUid )
    {
        this.repositoryItemUid = repositoryItemUid;

        this.store = repositoryItemUid.getRepository();

        this.repositoryId = repositoryItemUid.getRepository().getId();

        this.path = repositoryItemUid.getPath();
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    /**
     * Sets the repository id.
     * 
     * @param repositoryId the new repository id
     */
    public void setRepositoryId( String repositoryId )
    {
        this.repositoryId = repositoryId;
    }

    public long getCreated()
    {
        return created;
    }

    /**
     * Sets the created.
     * 
     * @param created the new created
     */
    public void setCreated( long created )
    {
        this.created = created;
    }

    public long getModified()
    {
        return modified;
    }

    /**
     * Sets the modified.
     * 
     * @param modified the new modified
     */
    public void setModified( long modified )
    {
        this.modified = modified;
    }

    public boolean isReadable()
    {
        return readable;
    }

    /**
     * Sets the readable.
     * 
     * @param readable the new readable
     */
    public void setReadable( boolean readable )
    {
        this.readable = readable;
    }

    public boolean isWritable()
    {
        return writable;
    }

    /**
     * Sets the writable.
     * 
     * @param writable the new writable
     */
    public void setWritable( boolean writable )
    {
        this.writable = writable;
    }

    public String getPath()
    {
        return path;
    }

    /**
     * Sets the path.
     * 
     * @param path the new path
     */
    public void setPath( String path )
    {
        if ( StringUtils.isEmpty( path ) )
        {
            path = RepositoryItemUid.PATH_ROOT;
        }

        if ( path.length() > 1 && path.endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            this.path = path.substring( 0, path.length() - 1 );
        }
        else
        {
            this.path = path;
        }
    }

    public boolean isExpired()
    {
        return expired;
    }

    /**
     * Sets the expired flag.
     * 
     * @param expired
     */
    public void setExpired( boolean expired )
    {
        this.expired = expired;
    }

    public String getName()
    {
        return new File( getPath() ).getName();
    }

    public String getParentPath()
    {
        String parent = getPath().substring( 0, getPath().lastIndexOf( "/" ) );

        if ( StringUtils.isEmpty( parent ) )
        {
            parent = RepositoryItemUid.PATH_ROOT;
        }
        return parent;
    }

    public Map<String, String> getAttributes()
    {
        if ( attributes == null )
        {
            attributes = new HashMap<String, String>();
        }

        return attributes;
    }

    public Map<String, Object> getItemContext()
    {
        if ( context == null )
        {
            context = new HashMap<String, Object>();
        }

        return context;
    }

    public boolean isVirtual()
    {
        return getRepositoryItemUid() == null;
    }

    public String getRemoteUrl()
    {
        return remoteUrl;
    }

    /**
     * Sets the remote url.
     * 
     * @param remoteUrl the new remote url
     */
    public void setRemoteUrl( String remoteUrl )
    {
        this.remoteUrl = remoteUrl;
    }

    public long getStoredLocally()
    {
        return storedLocally;
    }

    /**
     * Sets the stored locally.
     * 
     * @param storedLocally the new stored locally
     */
    public void setStoredLocally( long storedLocally )
    {
        this.storedLocally = storedLocally;
    }

    public long getRemoteChecked()
    {
        return lastTouched;
    }

    /**
     * Sets the remote checked.
     * 
     * @param remoteChecked the new remote checked
     */
    public void setRemoteChecked( long lastTouched )
    {
        this.lastTouched = lastTouched;
    }

    public long getLastRequested()
    {
        return lastRequested;
    }

    /**
     * Sets the last requested timestamp.
     * 
     * @param lastRequested
     */
    public void setLastRequested( long lastRequested )
    {
        this.lastRequested = lastRequested;
    }

    public int getGeneration()
    {
        return generation;
    }

    public void incrementGeneration()
    {
        this.generation++;
    }

    public void overlay( StorageItem item )
        throws IllegalArgumentException
    {
        if ( item != null && this.getClass().isAssignableFrom( item.getClass() ) )
        {
            // TODO: WHY?
            // these do not overlays:
            // path
            // readable
            // writable
            // repositoryItemUid
            // store

            // these do overlays:
            setRepositoryId( item.getRepositoryId() );
            setCreated( item.getCreated() );
            setModified( item.getModified() );
            setStoredLocally( item.getStoredLocally() );
            setRemoteChecked( item.getRemoteChecked() );
            setLastRequested( item.getLastRequested() );
            setExpired( item.isExpired() );
            setRemoteUrl( item.getRemoteUrl() );
            getAttributes().putAll( item.getAttributes() );
            getItemContext().putAll( item.getItemContext() );
        }
        else
        {
            if ( item == null )
            {
                throw new IllegalArgumentException( "Cannot overlay null item onto this item of class "
                    + this.getClass().getName() );
            }
            else
            {
                throw new IllegalArgumentException( "Cannot overlay storage item of class " + item.getClass().getName()
                    + " onto this item of class " + this.getClass().getName() );
            }
        }
    }

}
