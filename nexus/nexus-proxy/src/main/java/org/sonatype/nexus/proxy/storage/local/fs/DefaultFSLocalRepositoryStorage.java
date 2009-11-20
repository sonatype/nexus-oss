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
package org.sonatype.nexus.proxy.storage.local.fs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
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
import org.sonatype.nexus.util.ItemPathUtils;

/**
 * The Class DefaultFSLocalRepositoryStorage.
 * 
 * @author cstamas
 */
@Component( role = LocalRepositoryStorage.class, hint = DefaultFSLocalRepositoryStorage.PROVIDER_STRING )
public class DefaultFSLocalRepositoryStorage
    extends AbstractLocalRepositoryStorage
        implements Initializable
{
    public static final String PROVIDER_STRING = "file";

    private static final String LINK_PREFIX = "LINK to ";
    
    @Configuration( value = "${rename.retry.count}" )
    private String renameRetryCountString;
    
    @Configuration( value = "${rename.retry.delay}" )
    private String renameRetryDelayString;
    
    private int renameRetryCount = 0;
    private int renameRetryDelay = 0;

    /**
     * The UID factory.
     */
    @Requirement
    private RepositoryItemUidFactory repositoryItemUidFactory;
    
    public void initialize()
        throws InitializationException
    {
        try
        {
            renameRetryCount = Integer.parseInt( renameRetryCountString );
            renameRetryCount = Integer.parseInt( renameRetryDelayString );
        }
        catch ( NumberFormatException e )
        {
        }
        
        //initialize values, 0 isn't valid in either case
        if ( renameRetryCount == 0
            || renameRetryDelay == 0 )
        {
            renameRetryCount = 0;
            renameRetryDelay = 0;
        }
    }

    public String getProviderId()
    {
        return PROVIDER_STRING;
    }

    public void validateStorageUrl( String url )
        throws StorageException
    {
        boolean result = org.sonatype.nexus.util.FileUtils.validFileUrl( url );

        if ( !result )
        {
            throw new StorageException( "Invalid storage URL, not a file based one: " + url );
        }
    }

    /**
     * Gets the base dir.
     * 
     * @return the base dir
     */
    public File getBaseDir( Repository repository, ResourceStoreRequest request )
        throws StorageException
    {
        request.pushRequestPath( RepositoryItemUid.PATH_ROOT );

        URL url = getAbsoluteUrlFromBase( repository, request );

        request.popRequestPath();

        File file;

        try
        {
            file = new File( url.toURI() );
        }
        catch ( Throwable t )
        {
            file = new File( url.getPath() );
        }

        if ( file.exists() )
        {
            if ( file.isFile() )
            {
                throw new StorageException( "The \"" + repository.getName() + "\" (ID=\"" + repository.getId()
                    + "\") repository's baseDir is not a directory, path: " + file.getAbsolutePath() );
            }
        }
        else
        {
            if ( !file.mkdirs() )
            {
                throw new StorageException( "Could not create the baseDir directory for repository \""
                    + repository.getName() + "\" (ID=\"" + repository.getId() + "\") on path " + file.getAbsolutePath() );
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
    public File getFileFromBase( Repository repository, ResourceStoreRequest request )
        throws StorageException
    {
        File repoBase = getBaseDir( repository, request );

        if ( !repoBase.exists() )
        {
            repoBase.mkdir();
        }

        File result = null;

        if ( request.getRequestPath() == null || RepositoryItemUid.PATH_ROOT.equals( request.getRequestPath() ) )
        {
            result = repoBase;
        }
        else if ( request.getRequestPath().startsWith( "/" ) )
        {
            result = new File( repoBase, request.getRequestPath().substring( 1 ) );
        }
        else
        {
            result = new File( repoBase, request.getRequestPath() );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( request.getRequestPath() + " --> " + result.getAbsoluteFile() );
        }

        // to be foolproof, chrooting it
        if ( !result.getAbsolutePath().startsWith( getBaseDir( repository, request ).getAbsolutePath() ) )
        {
            throw new StorageException( "getFileFromBase() method evaluated directory wrongly in repository \""
                + repository.getName() + "\" (id=\"" + repository.getId() + "\")! baseDir="
                + getBaseDir( repository, request ).getAbsolutePath() + ", target=" + result.getAbsolutePath() );
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
    protected AbstractStorageItem retrieveItemFromFile( Repository repository, ResourceStoreRequest request, File target )
        throws ItemNotFoundException, StorageException
    {
        String path = request.getRequestPath();

        boolean mustBeACollection = path.endsWith( RepositoryItemUid.PATH_SEPARATOR );

        if ( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }

        if ( StringUtils.isEmpty( path ) )
        {
            path = RepositoryItemUid.PATH_ROOT;
        }

        RepositoryItemUid uid = repository.createUid( path );

        AbstractStorageItem result = null;
        if ( target.exists() && target.isDirectory() )
        {
            request.setRequestPath( path );

            DefaultStorageCollectionItem coll =
                new DefaultStorageCollectionItem( repository, request, target.canRead(), target.canWrite() );
            coll.setModified( target.lastModified() );
            coll.setCreated( target.lastModified() );
            getAttributesHandler().fetchAttributes( coll );
            result = coll;

        }
        else if ( target.exists() && target.isFile() && !mustBeACollection )
        {
            request.setRequestPath( path );

            if ( checkBeginOfFile( LINK_PREFIX, target ) )
            {
                try
                {
                    DefaultStorageLinkItem link =
                        new DefaultStorageLinkItem( repository, request, target.canRead(), target.canWrite(),
                            getLinkTarget( target ) );
                    getAttributesHandler().fetchAttributes( link );
                    link.setModified( target.lastModified() );
                    link.setCreated( target.lastModified() );
                    result = link;

                    touchItemLastRequested( System.currentTimeMillis(), repository, request, link );
                }
                catch ( NoSuchRepositoryException e )
                {
                    getLogger().warn( "Stale link object found on UID: " + uid.toString() + ", deleting it." );

                    target.delete();

                    throw new ItemNotFoundException( request, repository );
                }
            }
            else
            {
                DefaultStorageFileItem file =
                    new DefaultStorageFileItem( repository, request, target.canRead(), target.canWrite(),
                        new FileContentLocator( target, getMimeUtil().getMimeType( target ) ) );
                getAttributesHandler().fetchAttributes( file );
                file.setModified( target.lastModified() );
                file.setCreated( target.lastModified() );
                file.setLength( target.length() );
                result = file;

                touchItemLastRequested( System.currentTimeMillis(), repository, request, file );
            }
        }
        else
        {
            throw new ItemNotFoundException( request, repository );
        }

        return result;
    }

    public boolean isReachable( Repository repository, ResourceStoreRequest request )
        throws StorageException
    {
        File target = getBaseDir( repository, request );

        return target.exists() && target.canWrite();
    }

    public boolean containsItem( Repository repository, ResourceStoreRequest request )
        throws StorageException
    {
        return getFileFromBase( repository, request ).exists();
    }

    public AbstractStorageItem retrieveItem( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, StorageException
    {
        return retrieveItemFromFile( repository, request, getFileFromBase( repository, request ) );
    }

    private synchronized void mkParentDirs( Repository repository, File target )
        throws StorageException
    {
        if ( !target.getParentFile().exists() && !target.getParentFile().mkdirs() )
        {
            // recheck is it really a "good" parent?
            if ( !target.getParentFile().isDirectory() )
            {
                throw new StorageException( "Could not create the directory hiearchy in repository \""
                    + repository.getName() + "\" (id=\"" + repository.getId() + "\") to write "
                    + target.getAbsolutePath() );
            }
        }
    }

    public void storeItem( Repository repository, StorageItem item )
        throws UnsupportedStorageOperationException, StorageException
    {
        // set some sanity stuff
        item.setStoredLocally( System.currentTimeMillis() );
        item.setRemoteChecked( item.getStoredLocally() );
        item.setExpired( false );

        ResourceStoreRequest request = new ResourceStoreRequest( item );

        File target = null;

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            target = getFileFromBase( repository, request );

            // NXCM-966, to be replaced with Tx!
            File hiddenTarget = new File( target.getParentFile(), target.getName() + ".tmp" );

            try
            {
                mkParentDirs( repository, target );

                InputStream is = null;

                FileOutputStream os = null;

                try
                {
                    is = ( (StorageFileItem) item ).getInputStream();

                    os = new FileOutputStream( hiddenTarget );

                    IOUtil.copy( is, os );

                    os.flush();
                }
                finally
                {
                    IOUtil.close( is );

                    IOUtil.close( os );
                }
                
                handleRenameOperation( hiddenTarget, target );

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

                if ( hiddenTarget != null )
                {
                    hiddenTarget.delete();
                }

                throw new StorageException( "Got exception during storing on path "
                    + item.getRepositoryItemUid().toString(), e );
            }
        }
        else if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
        {
            target = getFileFromBase( repository, request );

            mkParentDirs( repository, target );

            target.mkdir();
            target.setLastModified( item.getModified() );
            getAttributesHandler().storeAttributes( item, null );
        }
        else if ( StorageLinkItem.class.isAssignableFrom( item.getClass() ) )
        {
            try
            {
                target = getFileFromBase( repository, request );

                mkParentDirs( repository, target );

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
    
    protected void handleRenameOperation( File hiddenTarget, File target )
        throws IOException
    {    
        // delete the target, this is required on windows
        if ( target.exists() )
        {
            target.delete();
        }
    
        // first try
        boolean success = hiddenTarget.renameTo( target );
    
        // if retries enabled go ahead and start the retry process
        for ( int i = 1; success == false && i <= renameRetryCount; i++ )
        {
            getLogger().debug(
                "Rename operation attempt " + i + "failed on " + hiddenTarget.getAbsolutePath() + " --> "
                    + target.getAbsolutePath() + " will wait " + renameRetryDelay + " milliseconds and try again" );
    
            try
            {
                Thread.sleep( renameRetryDelay );
            }
            catch ( InterruptedException e )
            {
            }
            
            success = hiddenTarget.renameTo( target );
        }
    
        if ( !success )
        {
            throw new IOException( "Cannot rename file \"" + hiddenTarget.getAbsolutePath() + "\" to \""
                + target.getAbsolutePath() + "\"!" );
        }
    }

    public void shredItem( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, UnsupportedStorageOperationException, StorageException
    {
        RepositoryItemUid uid = repository.createUid( request.getRequestPath() );

        getAttributesHandler().deleteAttributes( uid );

        File target = getFileFromBase( repository, request );

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
                    throw new StorageException( "Could not delete File in repository \"" + repository.getName()
                        + "\" (id=\"" + repository.getId() + "\") from path " + target.getAbsolutePath(), ex );
                }
            }
            else if ( target.isFile() )
            {
                if ( !target.delete() )
                {
                    throw new StorageException( "Could not delete File in repository \"" + repository.getName()
                        + "\" (id=\"" + repository.getId() + "\") from path " + target.getAbsolutePath() );
                }
            }
        }
        else
        {
            throw new ItemNotFoundException( request, repository );
        }
    }

    public Collection<StorageItem> listItems( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, StorageException
    {
        File target = getFileFromBase( repository, request );

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
                            String newPath = ItemPathUtils.concatPaths( request.getRequestPath(), files[i].getName() );

                            request.pushRequestPath( newPath );

                            result.add( retrieveItemFromFile( repository, request, files[i] ) );

                            request.popRequestPath();
                        }
                    }
                }
                else
                {
                    getLogger().warn( "Cannot list directory " + target.getAbsolutePath() );
                }
            }
            else if ( target.isFile() )
            {
                result.add( retrieveItemFromFile( repository, request, target ) );
            }
            else
            {
                throw new ItemNotFoundException( request, repository );
            }
        }
        else
        {
            throw new ItemNotFoundException( request, repository );
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
