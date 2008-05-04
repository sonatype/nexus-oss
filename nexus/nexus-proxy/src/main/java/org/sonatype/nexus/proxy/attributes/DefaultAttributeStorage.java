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
package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.LoggingComponent;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.BaseException;

/**
 * AttributeStorage implementation driven by XStream.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultAttributeStorage
    extends LoggingComponent
    implements AttributeStorage, Initializable
{

    /**
     * @plexus.requirement
     */
    private ApplicationConfiguration applicationConfiguration;

    /**
     * The base dir.
     */
    private File workingDirectory;

    /** The xstream. */
    private XStream xstream;

    /**
     * Instantiates a new FSX stream attribute storage.
     */
    public DefaultAttributeStorage()
    {
        super();
        this.xstream = new XStream();
        this.xstream.alias( "file", DefaultStorageFileItem.class );
        this.xstream.alias( "collection", DefaultStorageCollectionItem.class );
        this.xstream.alias( "link", DefaultStorageLinkItem.class );
    }

    public void initialize()
    {
        applicationConfiguration.addConfigurationChangeListener( this );
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        this.workingDirectory = null;
    }

    /**
     * Gets the base dir.
     * 
     * @return the base dir
     */
    public File getWorkingDirectory()
        throws IOException
    {
        if ( workingDirectory == null )
        {
            workingDirectory = applicationConfiguration.getWorkingDirectory( "proxy/attributes" );

            if ( workingDirectory.exists() )
            {
                if ( workingDirectory.isFile() )
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
                    throw new IllegalArgumentException( "Could not create the attribute storage directory on path "
                        + workingDirectory.getAbsolutePath() );
                }
            }
        }

        return workingDirectory;
    }

    public void setWorkingDirectory( File baseDir )
    {
        this.workingDirectory = baseDir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.AttributeStorage#deleteAttributes(org.sonatype.nexus.item.RepositoryItemUid)
     */
    public boolean deleteAttributes( RepositoryItemUid uid )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Deleting attributes on UID=" + uid.toString() );
        }
        boolean result = false;
        try
        {
            File ftarget = getFileFromBase( uid, false );
            result = ftarget.exists() && ftarget.isFile() && ftarget.delete();
            if ( !result )
            {
                File dtarget = getFileFromBase( uid, true );
                result = dtarget.exists() && dtarget.isFile() && dtarget.delete();
                FileUtils.deleteDirectory( dtarget.getParentFile() );
            }
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IOException during delete of UID=" + uid.toString(), e );
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.AttributeStorage#getAttributes(org.sonatype.nexus.item.RepositoryItemUid)
     */
    public AbstractStorageItem getAttributes( RepositoryItemUid uid )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Loading attributes on UID=" + uid.toString() );
        }
        try
        {
            AbstractStorageItem result = null;

            result = getAttributes( uid, false );

            if ( result == null )
            {
                result = getAttributes( uid, true );
            }
            return result;
        }
        catch ( IOException ex )
        {
            getLogger().error( "Got IOException during store of UID=" + uid.toString(), ex );
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.AttributeStorage#putAttribute(org.sonatype.nexus.item.AbstractStorageItem)
     */
    public void putAttribute( AbstractStorageItem item )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Storing attributes on UID=" + item.getRepositoryItemUid() );
        }
        try
        {
            File target = getFileFromBase( item.getRepositoryItemUid(), StorageCollectionItem.class
                .isAssignableFrom( item.getClass() ) );
            target.getParentFile().mkdirs();
            if ( target.getParentFile().exists() && target.getParentFile().isDirectory() )
            {
                FileOutputStream fos = null;
                try
                {
                    fos = new FileOutputStream( target );
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

    /**
     * Gets the attributes.
     * 
     * @param uid the uid
     * @param isCollection the is collection
     * @return the attributes
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected AbstractStorageItem getAttributes( RepositoryItemUid uid, boolean isCollection )
        throws IOException
    {
        File target = getFileFromBase( uid, isCollection );
        AbstractStorageItem result = null;
        if ( target.exists() && target.isFile() )
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream( target );
                result = (AbstractStorageItem) xstream.fromXML( fis );
            }
            catch ( BaseException e )
            {
                // it is corrupt
                getLogger().info( "Attributes of " + uid + " are corrupt, deleting it." );

                deleteAttributes( uid );
            }
            finally
            {
                IOUtil.close( fis );
            }
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
    protected File getFileFromBase( RepositoryItemUid uid, boolean isCollection )
        throws IOException
    {
        File repoBase = new File( getWorkingDirectory(), uid.getRepository().getId() );

        File result = null;

        String path = FilenameUtils.getPath( uid.getPath() );

        String name = FilenameUtils.getName( uid.getPath() );

        if ( isCollection )
        {
            result = new File( repoBase, path + "/" + name + ".directory" );
        }
        else
        {
            result = new File( repoBase, path + "/" + name );
        }

        // to be foolproof
        // 2007.11.09. - Believe or not, Nexus deleted my whole USB rack! (cstamas)
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
