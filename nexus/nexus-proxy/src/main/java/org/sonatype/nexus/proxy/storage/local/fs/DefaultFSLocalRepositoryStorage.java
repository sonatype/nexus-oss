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
package org.sonatype.nexus.proxy.storage.local.fs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.AbstractLocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

/**
 * The Class DefaultFSLocalRepositoryStorage.
 * 
 * @author cstamas
 * @plexus.component role-hint="file"
 */
public class DefaultFSLocalRepositoryStorage
    extends AbstractLocalRepositoryStorage
    implements LocalRepositoryStorage
{
    private static final String LINK_PREFIX = "LINK to ";

    /**
     * The UID factory.
     * 
     * @plexus.requirement
     */
    private RepositoryItemUidFactory repositoryItemUidFactory;

    /**
     * Instantiates a new default FS local repository storage.
     */
    public DefaultFSLocalRepositoryStorage()
    {
        super();
    }

    /**
     * Gets the base dir.
     * 
     * @return the base dir
     */
    public File getBaseDir( Repository repository )
        throws StorageException
    {
        URL url = getAbsoluteUrlFromBase( repository.createUidForPath( RepositoryItemUid.PATH_ROOT ) );
        File file;
        try
        {
            file = new File( url.toURI() );
        }
        catch ( URISyntaxException e )
        {
            file = new File( url.getPath() );
        }
        if ( file.exists() )
        {
            if ( file.isFile() )
            {
                throw new StorageException( "The baseDir property is not a directory: " + file.getAbsolutePath() );
            }
        }
        else
        {
            if ( !file.mkdirs() )
            {
                throw new StorageException( "Could not create the baseDir directory on path " + file.getAbsolutePath() );
            }
        }
        return file;
    }

    /**
     * Gets the file from base.
     * 
     * @param uid the uid
     * @return the file from base
     */
    public File getFileFromBase( RepositoryItemUid uid )
        throws StorageException
    {
        File repoBase = getBaseDir( uid.getRepository() );

        if ( !repoBase.exists() )
        {
            repoBase.mkdir();
        }
        File result = null;
        if ( uid.getPath() == null || RepositoryItemUid.PATH_ROOT.equals( uid.getPath() ) )
        {
            result = repoBase;
        }
        else if ( uid.getPath().startsWith( "/" ) )
        {
            result = new File( repoBase, uid.getPath().substring( 1 ) );
        }
        else
        {
            result = new File( repoBase, uid.getPath() );
        }
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( uid.toString() + " --> " + result.getAbsoluteFile() );
        }

        // to be foolproof, chrooting it
        if ( !result.getAbsolutePath().startsWith( getBaseDir( uid.getRepository() ).getAbsolutePath() ) )
        {
            throw new StorageException( "FileFromBase evaluated directory wrongly! baseDir="
                + getBaseDir( uid.getRepository() ).getAbsolutePath() + ", target=" + result.getAbsolutePath() );
        }
        else
        {
            return result;
        }
    }

    /**
     * Retrieve item from file.
     * 
     * @param uid the uid
     * @param target the target
     * @return the abstract storage item
     * @throws ItemNotFoundException the item not found exception
     * @throws StorageException the storage exception
     */
    protected AbstractStorageItem retrieveItemFromFile( RepositoryItemUid tuid, File target )
        throws ItemNotFoundException,
            StorageException
    {
        boolean mustBeACollection = tuid.getPath().endsWith( RepositoryItemUid.PATH_SEPARATOR );

        String path = tuid.getPath();

        if ( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }

        if ( StringUtils.isEmpty( path ) )
        {
            path = RepositoryItemUid.PATH_ROOT;
        }

        RepositoryItemUid uid = tuid.getRepository().createUidForPath( path );

        AbstractStorageItem result = null;
        if ( target.exists() && target.isDirectory() )
        {

            DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem(
                uid.getRepository(),
                uid.getPath(),
                target.canRead(),
                target.canWrite() );
            coll.setModified( target.lastModified() );
            coll.setCreated( target.lastModified() );
            getAttributesHandler().fetchAttributes( coll );
            result = coll;

        }
        else if ( target.exists() && target.isFile() && !mustBeACollection )
        {
            if ( checkBeginOfFile( LINK_PREFIX, target ) )
            {
                try
                {
                    DefaultStorageLinkItem link = new DefaultStorageLinkItem(
                        uid.getRepository(),
                        uid.getPath(),
                        target.canRead(),
                        target.canWrite(),
                        getLinkTarget( target ) );
                    getAttributesHandler().fetchAttributes( link );
                    link.setModified( target.lastModified() );
                    link.setCreated( target.lastModified() );
                    result = link;
                }
                catch ( NoSuchRepositoryException e )
                {
                    getLogger().warn( "Stale link object found on UID: " + uid.toString() + ", deleting it." );

                    target.delete();

                    throw new ItemNotFoundException( uid );
                }
            }
            else
            {
                DefaultStorageFileItem file = new DefaultStorageFileItem( uid.getRepository(), uid.getPath(), target
                    .canRead(), target.canWrite() );
                getAttributesHandler().fetchAttributes( file );
                file.setModified( target.lastModified() );
                file.setCreated( target.lastModified() );
                file.setLength( target.length() );
                result = file;
            }
        }
        else
        {
            throw new ItemNotFoundException( uid );
        }

        return result;
    }

    public boolean isReachable( RepositoryItemUid uid )
        throws StorageException
    {
        File target = getBaseDir( uid.getRepository() );

        return target.exists() && target.canWrite();
    }

    public boolean containsItem( RepositoryItemUid uid )
        throws StorageException
    {
        return getFileFromBase( uid ).exists();
    }

    public AbstractStorageItem retrieveItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {
        return retrieveItemFromFile( uid, getFileFromBase( uid ) );
    }

    public InputStream retrieveItemContent( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {
        try
        {
            return new FileInputStream( getFileFromBase( uid ) );
        }
        catch ( FileNotFoundException ex )
        {
            throw new ItemNotFoundException( uid );
        }
    }

    private synchronized void mkParentDirs( File target )
        throws StorageException
    {
        if ( !target.getParentFile().exists() && !target.getParentFile().mkdirs() )
        {
            throw new StorageException( "Could not create the directory hiearchy to write " + target.getAbsolutePath() );
        }
    }

    public void storeItem( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            StorageException
    {
        // set some sanity stuff
        item.setStoredLocally( System.currentTimeMillis() );
        item.setRemoteChecked( item.getStoredLocally() );
        item.setExpired( false );

        File target = null;
        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            target = getFileFromBase( item.getRepositoryItemUid() );
            try
            {
                mkParentDirs( target );

                InputStream is = null;
                FileOutputStream os = null;

                try
                {
                    is = ( (StorageFileItem) item ).getInputStream();

                    os = new FileOutputStream( target );

                    IOUtil.copy( is, os );

                    os.flush();
                }
                finally
                {
                    IOUtil.close( is );

                    IOUtil.close( os );
                }

                target.setLastModified( item.getModified() );

                ( (DefaultStorageFileItem) item ).setLength( target.length() );

                InputStream mdis = new FileInputStream( target );

                try
                {
                    getAttributesHandler().storeAttributes( item, mdis );
                }
                finally
                {
                    IOUtil.close( mdis );
                }
            }
            catch ( IOException e )
            {
                if ( target != null )
                {
                    target.delete();
                }

                throw new StorageException( "Got exception during storing on path "
                    + item.getRepositoryItemUid().toString(), e );
            }
        }
        else if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            target = getFileFromBase( item.getRepositoryItemUid() );

            mkParentDirs( target );

            target.mkdir();
            target.setLastModified( item.getModified() );
            getAttributesHandler().storeAttributes( item, null );
        }
        else if ( StorageLinkItem.class.isAssignableFrom( item.getClass() ) )
        {
            try
            {
                target = getFileFromBase( item.getRepositoryItemUid() );

                mkParentDirs( target );

                FileOutputStream os = new FileOutputStream( target );
                IOUtil.copy( new ByteArrayInputStream( ( LINK_PREFIX + ( (StorageLinkItem) item ).getTarget() )
                    .getBytes() ), os );
                os.flush();
                os.close();
                target.setLastModified( item.getModified() );
                getAttributesHandler().storeAttributes( item, null );
            }
            catch ( IOException ex )
            {
                if ( target != null )
                {
                    target.delete();
                }

                throw new StorageException( "Got exception during storing on path "
                    + item.getRepositoryItemUid().toString(), ex );
            }
        }
    }

    public void shredItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            StorageException
    {
        getAttributesHandler().deleteAttributes( uid );
        File target = getFileFromBase( uid );
        if ( target.exists() )
        {
            if ( target.isDirectory() )
            {
                try
                {
                    FileUtils.deleteDirectory( target );
                }
                catch ( IOException ex )
                {
                    throw new StorageException( "Could not delete directory from path " + target.getAbsolutePath(), ex );
                }
            }
            else if ( target.isFile() )
            {
                if ( !target.delete() )
                {
                    throw new StorageException( "Could not delete File from path " + target.getAbsolutePath() );
                }
            }
        }
        else
        {
            throw new ItemNotFoundException( uid.toString() );
        }
    }

    public Collection<StorageItem> listItems( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {
        File target = getFileFromBase( uid );
        List<StorageItem> result = new ArrayList<StorageItem>();
        if ( target.exists() )
        {
            if ( target.isDirectory() )
            {
                File[] files = target.listFiles();

                if ( files != null )
                {
                    for ( int i = 0; i < files.length; i++ )
                    {
                        if ( files[i].isFile() || files[i].isDirectory() )
                        {
                            RepositoryItemUid tuid;

                            if ( uid.getPath().endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
                            {
                                tuid = uid.getRepository().createUidForPath( uid.getPath() + files[i].getName() );
                            }
                            else
                            {
                                tuid = uid.getRepository().createUidForPath(
                                    uid.getPath() + RepositoryItemUid.PATH_SEPARATOR + files[i].getName() );
                            }

                            result.add( retrieveItemFromFile( tuid, files[i] ) );
                        }
                    }
                }
                else
                {
                    getLogger().warn( "Cannot list directory " + uid.toString() );
                }
            }
            else if ( target.isFile() )
            {
                result.add( retrieveItemFromFile( uid, target ) );
            }
            else
            {
                throw new ItemNotFoundException( uid );
            }
        }
        else
        {
            throw new ItemNotFoundException( uid.toString() );
        }
        return result;
    }

    protected boolean checkBeginOfFile( final String magic, File file )
    {
        if ( file != null && file.length() > magic.length() )
        {
            FileInputStream fis = null;

            try
            {
                byte[] buf = new byte[magic.length()];

                byte[] link = magic.getBytes();

                fis = new FileInputStream( file );

                boolean result = fis.read( buf ) == magic.length();

                if ( result )
                {
                    for ( int i = 0; i < magic.length() && result; i++ )
                    {
                        result = result && buf[i] == link[i];
                    }
                }

                return result;
            }
            catch ( IOException e )
            {
                return false;
            }
            finally
            {
                IOUtil.close( fis );
            }

        }
        else
        {
            return false;
        }
    }

    protected RepositoryItemUid getLinkTarget( File file )
        throws NoSuchRepositoryException
    {
        if ( file != null && file.length() > LINK_PREFIX.length() )
        {
            FileInputStream fis = null;

            try
            {
                fis = new FileInputStream( file );

                String link = IOUtil.toString( fis );

                String uidStr = link.substring( LINK_PREFIX.length(), link.length() );

                return repositoryItemUidFactory.createUid( uidStr );
            }
            catch ( IOException e )
            {
                return null;
            }
            finally
            {
                IOUtil.close( fis );
            }

        }
        else
        {
            return null;
        }
    }
}
