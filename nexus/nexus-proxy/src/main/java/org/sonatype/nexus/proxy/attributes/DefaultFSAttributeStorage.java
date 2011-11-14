/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.Slf4jPlexusLogger;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCompositeFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsMetadataMaintainedAttribute;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * AttributeStorage implementation driven by XStream. This implementation uses it's own FS storage to store attributes
 * in separate place then LocalStorage. This is the "old" default storage.
 * 
 * @author cstamas
 */
@Component( role = AttributeStorage.class )
public class DefaultFSAttributeStorage
    implements AttributeStorage, EventListener, Initializable, Disposable
{
    private Logger logger = Slf4jPlexusLogger.getPlexusLogger( getClass() );

    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    /** The xstream. */
    private final XStream xstream;

    /**
     * The base dir.
     */
    private volatile File workingDirectory;

    /**
     * Instantiates a new FSX stream attribute storage.
     */
    public DefaultFSAttributeStorage()
    {
        super();
        this.xstream = new XStream();
        this.xstream.alias( "file", DefaultStorageFileItem.class );
        this.xstream.alias( "compositeFile", DefaultStorageCompositeFileItem.class );
        this.xstream.alias( "collection", DefaultStorageCollectionItem.class );
        this.xstream.alias( "link", DefaultStorageLinkItem.class );
    }

    protected Logger getLogger()
    {
        return logger;
    }

    public void initialize()
    {
        applicationEventMulticaster.addEventListener( this );

        initializeWorkingDirectory();
    }

    public void dispose()
    {
        applicationEventMulticaster.removeEventListener( this );
    }

    public void onEvent( final Event<?> evt )
    {
        if ( ConfigurationChangeEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            initializeWorkingDirectory();
        }
    }

    /**
     * Gets the base dir.
     * 
     * @return the base dir
     */
    public File getWorkingDirectory()
        throws IOException
    {
        return workingDirectory;
    }

    public synchronized File initializeWorkingDirectory()
    {
        if ( workingDirectory == null )
        {
            workingDirectory = applicationConfiguration.getWorkingDirectory( "proxy/attributes" );

            if ( workingDirectory.exists() )
            {
                if ( !workingDirectory.isDirectory() )
                {
                    throw new IllegalArgumentException( "The attribute storage exists and is not a directory: "
                        + workingDirectory.getAbsolutePath() );
                }
            }
            else
            {
                getLogger().info( "Attribute storage directory does not exists, creating it here: " + workingDirectory );

                if ( !workingDirectory.mkdirs() )
                {
                    if ( !workingDirectory.isDirectory() )
                    {
                        throw new IllegalArgumentException( "Could not create the attribute storage directory on path "
                            + workingDirectory.getAbsolutePath() );
                    }
                }
            }
        }

        return workingDirectory;
    }

    public synchronized void setWorkingDirectory( final File baseDir )
    {
        this.workingDirectory = baseDir;
    }

    protected boolean IsMetadataMaintained( final RepositoryItemUid uid )
    {
        Boolean isMetadataMaintained = uid.getAttributeValue( IsMetadataMaintainedAttribute.class );

        if ( isMetadataMaintained != null )
        {
            return isMetadataMaintained.booleanValue();
        }
        else
        {
            // safest
            return true;
        }
    }

    public boolean deleteAttributes( final RepositoryItemUid uid )
    {
        if ( !IsMetadataMaintained( uid ) )
        {
            // do nothing
            return false;
        }

        final RepositoryItemUidLock uidLock = uid.getAttributeLock();

        uidLock.lock( Action.delete );

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Deleting attributes on UID=" + uid.toString() );
            }

            boolean result = false;

            try
            {
                File ftarget = getFileFromBase( uid );

                result = ftarget.exists() && ftarget.isFile() && ftarget.delete();
            }
            catch ( IOException e )
            {
                getLogger().warn( "Got IOException during delete of UID=" + uid.toString(), e );
            }

            return result;
        }
        finally
        {
            uidLock.unlock();
        }
    }

    public AbstractStorageItem getAttributes( final RepositoryItemUid uid )
    {
        if ( !IsMetadataMaintained( uid ) )
        {
            // do nothing
            return null;
        }

        final RepositoryItemUidLock uidLock = uid.getAttributeLock();

        uidLock.lock( Action.read );

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Loading attributes on UID=" + uid.toString() );
            }
            
            try
            {
                AbstractStorageItem result = null;

                result = doGetAttributes( uid );

                return result;
            }
            catch ( IOException ex )
            {
                getLogger().error( "Got IOException during reading of UID=" + uid.toString(), ex );

                return null;
            }
        }
        finally
        {
            uidLock.unlock();
        }
    }

    public void putAttribute( StorageItem item )
    {
        if ( !IsMetadataMaintained( item.getRepositoryItemUid() ) )
        {
            // do nothing
            return;
        }

        final RepositoryItemUid origUid = item.getRepositoryItemUid();

        final RepositoryItemUidLock uidLock = origUid.getAttributeLock();

        uidLock.lock( Action.create );

        try
        {
            if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
            {
                // not saving attributes for directories anymore
                return;
            }

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Storing attributes on UID=" + item.getRepositoryItemUid() );
            }

            try
            {
                AbstractStorageItem onDisk = doGetAttributes( item.getRepositoryItemUid() );

                if ( onDisk != null && ( onDisk.getGeneration() > item.getGeneration() ) )
                {
                    // change detected, overlay the to be saved onto the newer one and swap
                    onDisk.setResourceStoreRequest( item.getResourceStoreRequest() );

                    onDisk.overlay( item );

                    // and overlay other things too
                    onDisk.setRepositoryItemUid( item.getRepositoryItemUid() );
                    onDisk.setReadable( item.isReadable() );
                    onDisk.setWritable( item.isWritable() );

                    item = onDisk;
                }

                File target = getFileFromBase( item.getRepositoryItemUid() );

                target.getParentFile().mkdirs();

                if ( target.getParentFile().exists() && target.getParentFile().isDirectory() )
                {
                    FileOutputStream fos = null;

                    try
                    {
                        fos = new FileOutputStream( target );

                        item.incrementGeneration();

                        xstream.toXML( item, fos );

                        fos.flush();
                    }
                    finally
                    {
                        IOUtil.close( fos );
                    }
                }
                else
                {
                    getLogger().error(
                        "Could not store attributes on UID=" + item.getRepositoryItemUid()
                            + ", parent exists but is not a directory!" );
                }
            }
            catch ( IOException ex )
            {
                getLogger().error( "Got IOException during store of UID=" + item.getRepositoryItemUid(), ex );
            }
        }
        finally
        {
            uidLock.unlock();
        }
    }

    // ==

    /**
     * Gets the attributes.
     * 
     * @param uid the uid
     * @param isCollection the is collection
     * @return the attributes
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected AbstractStorageItem doGetAttributes( final RepositoryItemUid uid )
        throws IOException
    {
        final File target = getFileFromBase( uid );

        AbstractStorageItem result = null;

        boolean corrupt = false;

        if ( target.exists() && target.isFile() )
        {
            FileInputStream fis = null;

            try
            {
                fis = new FileInputStream( target );

                result = (AbstractStorageItem) xstream.fromXML( fis );

                result.setRepositoryItemUid( uid );

                // fixing remoteChecked
                if ( result.getRemoteChecked() == 0 || result.getRemoteChecked() == 1 )
                {
                    result.setRemoteChecked( System.currentTimeMillis() );

                    result.setExpired( true );
                }

                // fixing lastRequested
                if ( result.getLastRequested() == 0 )
                {
                    result.setLastRequested( System.currentTimeMillis() );
                }
            }
            catch ( IOException e )
            {
                getLogger().info( "While reading attributes of " + uid + " we got IOException:", e );

                throw e;
            }
            catch ( NullPointerException e )
            {
                // NEXUS-3911: seems that on malformed XML the XMLpull parser throws NPE?
                // org.xmlpull.mxp1.MXParser.fillBuf(MXParser.java:3020) : NPE
                // it is corrupt
                if ( getLogger().isDebugEnabled() )
                {
                    // we log the stacktrace
                    getLogger().info( "Attributes of " + uid + " are corrupt, deleting it.", e );
                }
                else
                {
                    // just remark about this
                    getLogger().info( "Attributes of " + uid + " are corrupt, deleting it." );
                }

                corrupt = true;
            }
            catch ( XStreamException e )
            {
                // it is corrupt -- so says XStream, but see above and NEXUS-3911
                if ( getLogger().isDebugEnabled() )
                {
                    // we log the stacktrace
                    getLogger().info( "Attributes of " + uid + " are corrupt, deleting it.", e );
                }
                else
                {
                    // just remark about this
                    getLogger().info( "Attributes of " + uid + " are corrupt, deleting it." );
                }

                corrupt = true;
            }
            finally
            {
                IOUtil.close( fis );
            }
        }

        if ( corrupt )
        {
            deleteAttributes( uid );
        }

        return result;
    }

    /**
     * Gets the file from base.
     * 
     * @param uid the uid
     * @param isCollection the is collection
     * @return the file from base
     */
    protected File getFileFromBase( final RepositoryItemUid uid )
        throws IOException
    {
        final File repoBase = new File( getWorkingDirectory(), uid.getRepository().getId() );

        File result = null;

        String path = FilenameUtils.getPath( uid.getPath() );

        String name = FilenameUtils.getName( uid.getPath() );

        result = new File( repoBase, path + "/" + name );

        // to be foolproof
        // 2007.11.09. - Believe or not, Nexus deleted my whole USB rack! (cstamas)
        // ok, now you may laugh :)
        if ( !result.getAbsolutePath().startsWith( getWorkingDirectory().getAbsolutePath() ) )
        {
            throw new IOException( "FileFromBase evaluated directory wrongly! baseDir="
                + getWorkingDirectory().getAbsolutePath() + ", target=" + result.getAbsolutePath() );
        }
        else
        {
            return result;
        }
    }
}
