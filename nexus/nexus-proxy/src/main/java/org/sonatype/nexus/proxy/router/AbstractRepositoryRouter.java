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
package org.sonatype.nexus.proxy.router;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.EventMulticasterComponent;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * <p>
 * An abstract base implementation of repository Router. All current router implementations are derived from this class.
 * <p>
 * This abstract class handles the following functionalities:
 * <ul>
 * <li>Manages (dereferences) links if needed (defaults to true)</li>
 * </ul>
 * <p>
 * The subclasses only needs to implement the abstract method focusing on item retrieaval and other "basic" functions.
 * <p>
 * This class DOES NOT IMPLY any kind of retrieval strategy. It should be implemented in some subclass.
 * 
 * @author cstamas
 */
public abstract class AbstractRepositoryRouter
    extends EventMulticasterComponent
    implements RepositoryRouter, Initializable
{

    /**
     * ApplicationConfiguration.
     * 
     * @plexus.requirement
     */
    private ApplicationConfiguration applicationConfiguration;

    /** Should links be resolved? */
    private boolean followLinks;

    /** The place to store spoofing router stuff */
    private File fileStore;

    public void initialize()
    {
        applicationConfiguration.addConfigurationChangeListener( this );
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        followLinks = applicationConfiguration.getConfiguration().getRouting().isFollowLinks();

        fileStore = applicationConfiguration.getWorkingDirectory( "router-" + getId() );
    }

    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    // =====================================================================
    // RepositoryRouter iface

    public boolean isFollowLinks()
    {
        return followLinks;
    }

    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "retrieveItem() " + request.getRequestPath() );
        }

        // try to give it from our own file store
        File fileItem = getFileStoreFile( request.getRequestPath() );

        if ( fileItem.exists() && fileItem.isFile() )
        {
            try
            {
                DefaultStorageFileItem result = new DefaultStorageFileItem( this, request.getRequestPath(), true, true, new FileInputStream(
                    fileItem ) );
                
                result.setCreated( fileItem.lastModified() );
                
                result.setModified( fileItem.lastModified() );
                
                result.setLength( fileItem.length() );
                
                return result;
            }
            catch ( FileNotFoundException e )
            {
                throw new StorageException( "Cannot find the file?", e );
            }
        }

        try
        {
            // do retrieve
            StorageItem result = doRetrieveItem( request );

            // we we dereference links and got a link
            if ( isFollowLinks() && StorageLinkItem.class.isAssignableFrom( result.getClass() ) )
            {
                return dereferenceLink( (StorageLinkItem) result );
            }
            else
            {
                return result;
            }
        }
        catch ( ItemNotFoundException ex )
        {
            throw ex;
        }
    }

    public Collection<StorageItem> list( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "list() " + request.getRequestPath() );
        }

        try
        {
            // get a list of items
            List<StorageItem> result = doListItems( request );

            // if following links, must resolve possible link items from result
            if ( isFollowLinks() )
            {
                List<StorageItem> dereferencedResult = new ArrayList<StorageItem>( result.size() );

                for ( StorageItem item : result )
                {
                    if ( StorageLinkItem.class.isAssignableFrom( item.getClass() ) )
                    {
                        dereferencedResult.add( dereferenceLink( (StorageLinkItem) item ) );
                    }
                    else
                    {
                        dereferencedResult.add( item );
                    }
                    result = dereferencedResult;
                }
            }
            return result;
        }
        catch ( ItemNotFoundException ex )
        {
            throw ex;
        }
    }

    public void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "copyItem() " + from.getRequestPath() + " -> " + to.getRequestPath() );
        }

        // do the stuff
        doCopyItem( from, to );
    }

    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "moveItem() " + from.getRequestPath() + " -> " + to.getRequestPath() );
        }

        // do the stuff
        doMoveItem( from, to );
    }

    public void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "storeItem() " + request.getRequestPath() );
        }

        // do it
        doStoreItem( request, is, userAttributes );
    }

    public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "createCollection() " + request.getRequestPath() );
        }

        // do it
        doCreateCollection( request, userAttributes );
    }

    public void deleteItem( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "deleteItem() " + request.getRequestPath() );
        }

        // do it
        doDeleteItem( request );
    }

    public void storeItem( String path, InputStream is )
        throws IOException
    {
        File dest = getFileStoreFile( path );

        FileOutputStream fos = null;

        try
        {
            dest.getParentFile().mkdirs();

            fos = new FileOutputStream( dest );

            IOUtil.copy( is, fos );
        }
        finally
        {
            if ( fos != null )
            {
                fos.flush();

                fos.close();
            }
            
            IOUtil.close( is );
        }
    }

    public void deleteItem( String path )
        throws IOException
    {
        File dest = getFileStoreFile( path );

        dest.delete();
    }

    // =====================================================================
    // Customization stuff No1

    protected File getFileStoreFile( String path )
    {
        while ( path.startsWith( "/" ) )
        {
            path = path.substring( 1 );
        }

        File dest = new File( fileStore, path );

        return dest;
    }

    /**
     * Dereference link. We are trying to dereference it the "simplest" way. The subclasses will probably reimplement
     * this method in better way. The way it is implemented here will not for for UIDs!
     * 
     * @param link the link
     * @return the storage item
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     * @throws AccessDeniedException the access denied exception
     * @throws ItemNotFoundException the item not found exception
     * @throws RepositoryNotAvailableException the repository not available exception
     */
    protected StorageItem dereferenceLink( StorageLinkItem link )
        throws NoSuchResourceStoreException,
            AccessDeniedException,
            ItemNotFoundException,
            RepositoryNotAvailableException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Dereferencing link " + link.getTarget() );
        }

        // create a request
        ResourceStoreRequest request = new ResourceStoreRequest( link.getTarget(), true );

        // pass it over the existing item context (ie. auth stuff, etc)
        request.getRequestContext().putAll( link.getItemContext() );

        // and retrieve it
        return retrieveItem( request );
    }

    // =====================================================================
    // Customization stuff No2

    /**
     * Retrieve item preprocessor.
     */
    protected StorageItem retrieveItemPreprocessor( ResourceStoreRequest request )
        throws StorageException
    {
        return null;
    }

    /**
     * Retrieve item postprocessor. Since all our methods are working with lists (or list of reposes which produces a
     * list of items), we have to detect what the proper result it. The method takes a list of items and makes one from
     * them.
     * 
     * @param request the request
     * @param listOfStorageItems the list of storage items
     * @return the storage item
     */
    protected StorageItem retrieveItemPostprocessor( ResourceStoreRequest request, List<StorageItem> listOfStorageItems )
        throws StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Postprocessing result set of " + listOfStorageItems.size() + " items" );
        }

        if ( listOfStorageItems.size() == 1 )
        {
            // no problem here, return the one got
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Result set is of size 1, returning the only result." );
            }

            return listOfStorageItems.get( 0 );
        }
        else
        {
            // does the results list have file or link?
            boolean haveFileOrLink = false;

            // does the results list have collection?
            boolean haveCollection = false;

            // the result
            StorageItem result = null;

            for ( StorageItem item : listOfStorageItems )
            {
                if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
                {
                    // files have precedence in merge, this does not affects fetch-by-full path requests
                    haveFileOrLink = true;

                    result = item;
                }
                if ( StorageLinkItem.class.isAssignableFrom( item.getClass() ) )
                {
                    haveFileOrLink = true;

                    if ( result == null )
                    {
                        result = item;
                    }
                }
                if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
                {
                    haveCollection = true;
                }
            }
            if ( haveCollection && haveFileOrLink )
            {
                // we have mixed results, file wins
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Result set have files and collections intermixed, file wins." );
                }

                return result;
            }
            else
            {
                if ( haveCollection )
                {
                    // only collections
                    // mangle the collections coming from multpiple reposes to loose their UID
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger()
                            .debug(
                                "Result set have collections coming from multiple reposes, merging them to loose their UID." );
                    }

                    return new DefaultStorageCollectionItem( this, listOfStorageItems.get( 0 ).getPath(), true, true );
                }
                else
                {
                    // only files or links, 1st wins.
                    // this is a place where subclasses will implement some extra stuff, like "metadata merge"!
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger()
                            .debug(
                                "Result set have files and links only, that are coming from multiple reposes, 1st file wins." );
                    }

                    return result;
                }
            }
        }
    }

    // =====================================================================
    // Customization stuff No3

    /**
     * Do retrieve item.
     * 
     * @param request the request
     * @return the storage item
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    protected abstract StorageItem doRetrieveItem( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Do list items.
     * 
     * @param request the request
     * @return the list< storage item>
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    protected abstract List<StorageItem> doListItems( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Do copy item.
     * 
     * @param from the from
     * @param to the to
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    protected abstract void doCopyItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Do move item.
     * 
     * @param from the from
     * @param to the to
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    protected abstract void doMoveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Do store item.
     * 
     * @param request the request
     * @param is the is
     * @param userAttributes the user attributes
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    protected abstract void doStoreItem( ResourceStoreRequest request, InputStream is,
        Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException;

    /**
     * Do create a collection.
     * 
     * @param request the request
     * @param userAttributes the user attributes
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    protected void doCreateCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        throw new StorageException( "This router does not support CreateCollection" );
    }

    /**
     * Do delete item.
     * 
     * @param request the request
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     * @throws AccessDeniedException the access denied exception
     */
    protected abstract void doDeleteItem( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

}
