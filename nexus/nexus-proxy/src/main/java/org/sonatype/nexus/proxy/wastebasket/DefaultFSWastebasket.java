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
package org.sonatype.nexus.proxy.wastebasket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.ApplicationEventMulticaster;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

/**
 * A default FS based implementation.
 * 
 * @author cstamas
 */
@Component( role = Wastebasket.class )
public class DefaultFSWastebasket
    implements SmartWastebasket, EventListener, Initializable
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    private File wastebasketDirectory;

    private DeleteOperation deleteOperation = DeleteOperation.MOVE_TO_TRASH;

    public void initialize()
        throws InitializationException
    {
        applicationEventMulticaster.addProximityEventListener( this );
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( ConfigurationChangeEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            wastebasketDirectory = null;
        }
    }

    protected File getWastebasketDirectory()
    {
        if ( wastebasketDirectory == null )
        {
            wastebasketDirectory = applicationConfiguration.getWastebasketDirectory();

            wastebasketDirectory.mkdirs();
        }

        return wastebasketDirectory;
    }

    protected File getFileForItem( AbstractStorageItem item )
    {
        StringBuffer basketPath = new StringBuffer( item.getRepositoryId() );

        basketPath.append( File.separatorChar );

        basketPath.append( item.getPath() );

        return new File( getWastebasketDirectory(), basketPath.toString() );
    }

    // ==============================
    // Wastebasket iface

    public DeleteOperation getDeleteOperation()
    {
        return deleteOperation;
    }

    public void setDeleteOperation( DeleteOperation deleteOperation )
    {
        this.deleteOperation = deleteOperation;
    }

    public long getItemCount()
        throws IOException
    {
        return org.sonatype.nexus.util.FileUtils.filesInDirectory( getWastebasketDirectory() );
    }

    public long getSize()
        throws IOException
    {
        return FileUtils.sizeOfDirectory( getWastebasketDirectory() );
    }

    public void purge()
        throws IOException
    {
        FileUtils.cleanDirectory( getWastebasketDirectory() );
    }

    public void purge( long age )
        throws IOException
    {
        removeForever( getWastebasketDirectory(), age );
    }

    public void delete( LocalRepositoryStorage ls, Repository repository, ResourceStoreRequest request )
        throws StorageException
    {
        try
        {
            if ( DeleteOperation.MOVE_TO_TRASH.equals( getDeleteOperation() ) )
            {
                AbstractStorageItem item = ls.retrieveItem( repository, request );

                // not deleting virtual items
                if ( item.isVirtual() )
                {
                    return;
                }

                File basketFile = getFileForItem( item );

                if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
                {
                    basketFile.getParentFile().mkdirs();

                    // a file or link, it is a simple File
                    InputStream is = null;

                    FileOutputStream fos = null;

                    try
                    {
                        is = ( (StorageFileItem) item ).getInputStream();

                        fos = new FileOutputStream( basketFile );

                        IOUtil.copy( is, fos );

                        fos.flush();
                    }
                    finally
                    {
                        IOUtil.close( is );

                        IOUtil.close( fos );
                    }
                }
                else if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
                {
                    basketFile.mkdirs();

                    // a collection (dir)
                    if ( DefaultFSLocalRepositoryStorage.class.isAssignableFrom( ls.getClass() ) )
                    {
                        // an easy way, we have a File
                        File itemFile = ( (DefaultFSLocalRepositoryStorage) ls ).getFileFromBase( repository, request );

                        FileUtils.copyDirectory( itemFile, basketFile );
                    }
                    else
                    {
                        // a hard way
                        // TODO: walker and recursive deletions?
                        // Currently, we have only the DefaultFSLocalRepositoryStorage implementation :)
                    }
                }
            }

            ls.shredItem( repository, request );
        }
        catch ( ItemNotFoundException e )
        {
            // silent
        }
        catch ( IOException e )
        {
            // yell
            throw new StorageException( "Got IOException during wastebasket handling!", e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // yell
            throw new StorageException( "Deletion operation is unsupported!", e );
        }
    }

    // ==============================
    // SmartWastebasket iface
    public void setMaxSizeInBytes( long bytes )
    {
        // TODO Auto-generated method stub
    }

    public void deleteRepositoryFolders( Repository repository )
        throws IOException
    {
        File defaultRepoStorageFolder = new File(
            new File( applicationConfiguration.getWorkingDirectory(), "storage" ),
            repository.getId() );

        // only remove the storage folder when in default storage case
        if ( defaultRepoStorageFolder.toURI().toURL().toString().equals( repository.getLocalUrl() + "/" ) )
        {
            delete( defaultRepoStorageFolder, false );
        }

        File repoProxyAttributesFolder = new File( new File( new File(
            applicationConfiguration.getWorkingDirectory(),
            "proxy" ), "attributes" ), repository.getId() );

        delete( repoProxyAttributesFolder, true );

        if ( !repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            File indexerFolder = new File( applicationConfiguration.getWorkingDirectory(), "indexer" );

            delete( new File( indexerFolder, repository.getId() + "-local" ), true );

            delete( new File( indexerFolder, repository.getId() + "-remote" ), true );
        }
    }

    /**
     * Move the file to trash, or simply delete it forever
     * 
     * @param file file to be deleted
     * @param deleteForever if it's true, delete the file forever, if it's false, move the file to trash
     * @throws IOException
     */
    protected void delete( File file, boolean deleteForever )
        throws IOException
    {
        if ( !deleteForever )
        {
            File basketFile = new File( getWastebasketDirectory(), file.getName() );

            if ( file.isDirectory() )
            {
                FileUtils.mkdir( basketFile.getAbsolutePath() );

                FileUtils.copyDirectoryStructure( file, basketFile );
            }
            else
            {
                FileUtils.copyFile( file, basketFile );
            }
        }

        FileUtils.forceDelete( file );
    }

    protected void removeForever( File file, long age )
        throws IOException
    {
        if ( file.isFile() )
        {
            if ( isOlderThan( file, age ) )
            {
                FileUtils.forceDelete( file );
            }
        }
        else
        {
            for ( File subFile : file.listFiles() )
            {
                removeForever( subFile, age );
            }
            if ( file.list().length == 0 )
            {
                FileUtils.forceDelete( file );
            }
        }
    }

    private boolean isOlderThan( File file, long age )
    {
        if ( System.currentTimeMillis() - file.lastModified() > age )
        {
            return true;
        }
        return false;
    }

}
