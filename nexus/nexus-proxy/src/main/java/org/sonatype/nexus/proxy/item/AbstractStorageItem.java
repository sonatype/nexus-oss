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
 */
public abstract class AbstractStorageItem
    implements StorageItem
{
    
    public static final long EXPIRED_TS = 1;

    /** The repository item uid. */
    private transient RepositoryItemUid repositoryItemUid;

    /** The store. */
    private transient ResourceStore store;

    /** The context. */
    private transient Map<String, Object> context;

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

    /** The last touched. */
    private long lastTouched;

    /** The remote url. */
    private String remoteUrl;

    /** The attributes. */
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
        this.created = System.currentTimeMillis();
        this.modified = this.created;
        this.attributes = new HashMap<String, String>();
        this.context = new HashMap<String, Object>();
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
        this.repositoryItemUid = new RepositoryItemUid( repository, path );
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

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#getRepositoryItemUid()
     */
    public RepositoryItemUid getRepositoryItemUid()
    {
        return repositoryItemUid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#getRepositoryId()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#getCreated()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#getModified()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#isReadable()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#isWritable()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#getPath()
     */
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
        return getLastTouched() == EXPIRED_TS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#getName()
     */
    public String getName()
    {
        return new File( getPath() ).getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#getParentPath()
     */
    public String getParentPath()
    {
        String parent = getPath().substring( 0, getPath().lastIndexOf( "/" ) );

        if ( StringUtils.isEmpty( parent ) )
        {
            parent = RepositoryItemUid.PATH_ROOT;
        }
        return parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#getAttributes()
     */
    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#getItemContext()
     */
    public Map<String, Object> getItemContext()
    {
        return context;
    }

    /**
     * Sets the attributes.
     * 
     * @param attributes the attributes
     */
    public void setAttributes( Map<String, String> attributes )
    {
        this.attributes = attributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#isVirtual()
     */
    public boolean isVirtual()
    {
        return getRepositoryItemUid() == null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#getRemoteUrl()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageItem#getStoredLocally()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.proxy.item.StorageItem#getRemoteChecked()
     */
    public long getLastTouched()
    {
        return lastTouched;
    }

    /**
     * Sets the remote checked.
     * 
     * @param remoteChecked the new remote checked
     */
    public void setLastTouched( long lastTouched )
    {
        this.lastTouched = lastTouched;
    }

    public void overlay( StorageItem item )
        throws IllegalArgumentException
    {
        if ( item != null && this.getClass().isAssignableFrom( item.getClass() ) )
        {
            setRepositoryId( item.getRepositoryId() );
            setStoredLocally( item.getStoredLocally() );
            setLastTouched( item.getLastTouched() );
            setRemoteUrl( item.getRemoteUrl() );
            setCreated( item.getCreated() );
            setModified( item.getModified() );
            getAttributes().putAll( item.getAttributes() );
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
