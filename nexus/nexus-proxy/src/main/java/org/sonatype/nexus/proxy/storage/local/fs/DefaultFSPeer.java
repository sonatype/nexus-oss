/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.storage.local.fs;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.util.ItemPathUtils;
import org.sonatype.nexus.util.SystemPropertiesHelper;

@Component( role = FSPeer.class )
public class DefaultFSPeer
    implements FSPeer
{
    private static final String HIDDEN_TARGET_SUFFIX = ".nx-upload";

    @Requirement
    private Logger logger;

    protected Logger getLogger()
    {
        return logger;
    }

    public boolean isReachable( Repository repository, ResourceStoreRequest request, File target )
        throws LocalStorageException
    {
        return target.exists() && target.canWrite();
    }

    public boolean containsItem( Repository repository, ResourceStoreRequest request, File target )
        throws LocalStorageException
    {
        return target.exists();
    }

    public File retrieveItem( Repository repository, ResourceStoreRequest request, File target )
        throws ItemNotFoundException, LocalStorageException
    {
        return target;
    }

    public void storeItem( Repository repository, StorageItem item, File target, ContentLocator cl )
        throws UnsupportedStorageOperationException, LocalStorageException
    {
        // create parents down to the file itself (this will make those if needed, otherwise return silently)
        mkParentDirs( repository, target );

        if ( cl != null )
        {
            // we have _content_ (content or link), hence we store a file
            File hiddenTarget = getHiddenTarget( target );

            try
            {
                FileOutputStream os = new FileOutputStream( hiddenTarget );

                InputStream is = cl.getContent();

                try
                {
                    IOUtil.copy( is, os, getCopyStreamBufferSize() );

                    os.flush();
                }
                finally
                {
                    IOUtil.close( is );

                    IOUtil.close( os );
                }

                handleRenameOperation( hiddenTarget, target );

                target.setLastModified( item.getModified() );
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

                throw new LocalStorageException( "Got exception during storing on path "
                    + item.getRepositoryItemUid().toString(), e );
            }
        }
        else
        {
            // we have no content, we talk about directory
            target.mkdir();

            target.setLastModified( item.getModified() );
        }
    }

    public void shredItem( Repository repository, ResourceStoreRequest request, File target )
        throws ItemNotFoundException, UnsupportedStorageOperationException, LocalStorageException
    {
        if ( target.isDirectory() )
        {
            try
            {
                FileUtils.deleteDirectory( target );
            }
            catch ( IOException ex )
            {
                throw new LocalStorageException( "Could not delete File in repository \"" + repository.getName()
                    + "\" (id=\"" + repository.getId() + "\") from path " + target.getAbsolutePath(), ex );
            }
        }
        else if ( target.isFile() )
        {
            if ( !target.delete() )
            {
                throw new LocalStorageException( "Could not delete File in repository \"" + repository.getName()
                    + "\" (id=\"" + repository.getId() + "\") from path " + target.getAbsolutePath() );
            }
        }
        else
        {
            throw new ItemNotFoundException( request, repository );
        }
    }

    public void moveItem( Repository repository, ResourceStoreRequest from, File fromTarget, ResourceStoreRequest to,
                          File toTarget )
        throws ItemNotFoundException, UnsupportedStorageOperationException, LocalStorageException
    {
        if ( fromTarget.exists() )
        {
            // create parents down to the file itself (this will make those if needed, otherwise return silently)
            mkParentDirs( repository, toTarget );

            try
            {
                org.sonatype.nexus.util.FileUtils.move( fromTarget, toTarget );
            }
            catch ( IOException e )
            {
                logger.warn( "Unable to move item, falling back to copy+delete: " + toTarget.getPath(),
                    logger.isDebugEnabled() ? e : null );

                if ( fromTarget.isDirectory() )
                {
                    try
                    {
                        FileUtils.copyDirectoryStructure( fromTarget, toTarget );
                    }
                    catch ( IOException ioe )
                    {
                        throw new LocalStorageException( "Error during moveItem", ioe );
                    }
                }
                else if ( fromTarget.isFile() )
                {
                    try
                    {
                        FileUtils.copyFile( fromTarget, toTarget );
                    }
                    catch ( IOException ioe )
                    {
                        throw new LocalStorageException( "Error during moveItem", ioe );
                    }
                }
                else
                {
                    // TODO throw exception?
                    logger.error( "Unexpected item kind: " + toTarget.getClass() );
                }
                shredItem( repository, from, fromTarget );
            }
        }
        else
        {
            throw new ItemNotFoundException( from, repository );
        }
    }

    public Collection<File> listItems( Repository repository, ResourceStoreRequest request, File target )
        throws ItemNotFoundException, LocalStorageException
    {
        if ( target.isDirectory() )
        {
            List<File> result = new ArrayList<File>();

            File[] files = target.listFiles( new FileFilter()
            {
                @Override
                public boolean accept( File pathname )
                {
                    return !pathname.getName().endsWith( HIDDEN_TARGET_SUFFIX );
                }
            } );

            if ( files != null )
            {
                for ( int i = 0; i < files.length; i++ )
                {
                    if ( files[i].isFile() || files[i].isDirectory() )
                    {
                        String newPath = ItemPathUtils.concatPaths( request.getRequestPath(), files[i].getName() );

                        request.pushRequestPath( newPath );

                        result.add( retrieveItem( repository, request, files[i] ) );

                        request.popRequestPath();
                    }
                }
            }
            else
            {
                getLogger().warn( "Cannot list directory " + target.getAbsolutePath() );
            }

            return result;
        }
        else if ( target.isFile() )
        {
            return null;
        }
        else
        {
            throw new ItemNotFoundException( request, repository );
        }
    }

    // ==

    protected File getHiddenTarget( File target )
    {
        File hiddenTarget = new File( target.getParentFile(), target.getName() + HIDDEN_TARGET_SUFFIX );

        return hiddenTarget;
    }

    protected void mkParentDirs( Repository repository, File target )
        throws LocalStorageException
    {
        if ( !target.getParentFile().exists() && !target.getParentFile().mkdirs() )
        {
            // re-check is it really a "good" parent?
            if ( !target.getParentFile().isDirectory() )
            {
                throw new LocalStorageException( "Could not create the directory hiearchy in repository \""
                    + repository.getName() + "\" (id=\"" + repository.getId() + "\") to write "
                    + target.getAbsolutePath() );
            }
        }
    }

    // ==

    public static final String FILE_COPY_STREAM_BUFFER_SIZE_KEY = "upload.stream.bufferSize";

    private int copyStreamBufferSize = -1;

    protected int getCopyStreamBufferSize()
    {
        if ( copyStreamBufferSize == -1 )
        {
            copyStreamBufferSize = SystemPropertiesHelper.getInteger( FILE_COPY_STREAM_BUFFER_SIZE_KEY, 4096 );
        }

        return this.copyStreamBufferSize;
    }

    // ==

    public static final String RENAME_RETRY_COUNT_KEY = "rename.retry.count";

    public static final String RENAME_RETRY_DELAY_KEY = "rename.retry.delay";

    private int renameRetryCount = -1;

    private int renameRetryDelay = -1;

    protected int getRenameRetryCount()
    {
        if ( renameRetryCount == -1 )
        {
            renameRetryCount = SystemPropertiesHelper.getInteger( RENAME_RETRY_COUNT_KEY, 0 );
        }

        return renameRetryCount;
    }

    protected int getRenameRetryDelay()
    {
        if ( renameRetryDelay == -1 )
        {
            renameRetryDelay = SystemPropertiesHelper.getInteger( RENAME_RETRY_DELAY_KEY, 0 );
        }

        return renameRetryDelay;
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
        for ( int i = 1; success == false && i <= getRenameRetryCount(); i++ )
        {
            getLogger().debug(
                "Rename operation attempt " + i + "failed on " + hiddenTarget.getAbsolutePath() + " --> "
                    + target.getAbsolutePath() + " will wait " + getRenameRetryDelay() + " milliseconds and try again" );

            try
            {
                Thread.sleep( getRenameRetryDelay() );
            }
            catch ( InterruptedException e )
            {
            }

            // try to delete again...
            if ( target.exists() )
            {
                target.delete();
            }

            // and rename again...
            success = hiddenTarget.renameTo( target );

            if ( success )
            {
                getLogger().info(
                    "Rename operation succeeded after " + i + " retries on " + hiddenTarget.getAbsolutePath() + " --> "
                        + target.getAbsolutePath() );
            }
        }

        if ( !success )
        {
            try
            {
                FileUtils.rename( hiddenTarget, target );
            }
            catch ( IOException e )
            {
                getLogger().error(
                    "Rename operation failed after " + getRenameRetryCount() + " retries in " + getRenameRetryDelay()
                        + " ms intervals " + hiddenTarget.getAbsolutePath() + " --> " + target.getAbsolutePath() );

                throw new IOException( "Cannot rename file \"" + hiddenTarget.getAbsolutePath() + "\" to \""
                    + target.getAbsolutePath() + "\"! " + e.getMessage() );
            }
        }
    }
}
