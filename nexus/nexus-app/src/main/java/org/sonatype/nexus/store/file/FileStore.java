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
package org.sonatype.nexus.store.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.store.DefaultEntry;
import org.sonatype.nexus.store.Entry;
import org.sonatype.nexus.store.Store;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.BaseException;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * The file backed store.
 * 
 * @author cstamas
 * @plexus.component role-hint="file"
 */
public class FileStore
    implements Store, ConfigurationChangeListener, Initializable
{
    /** @plexus.requirement */
    private ApplicationConfiguration applicationConfiguration;

    private File baseDir;

    private XStream xstream;

    public FileStore()
    {
        super();

        xstream = new XStream( new XppDriver() );
    }

    public void initialize()
    {
        applicationConfiguration.addConfigurationChangeListener( this );
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        this.baseDir = null;
    }

    public void setBaseDir( File baseDir )
    {
        this.baseDir = baseDir;
    }

    public File getBaseDir()
    {
        if ( baseDir == null )
        {
            baseDir = applicationConfiguration.getWorkingDirectory( "template-store" );
        }

        if ( !baseDir.exists() )
        {
            baseDir.mkdirs();
        }

        return baseDir;
    }

    public void addEntry( Entry entry )
        throws IOException
    {
        File file = getFileForId( entry.getId() );

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream( file );

            xstream.toXML( entry.getContent(), fos );

            fos.flush();
        }
        finally
        {
            if ( fos != null )
            {
                fos.close();
            }
        }

    }

    public Collection<Entry> getEntries()
        throws IOException
    {
        File[] files = getBaseDir().listFiles();

        ArrayList<Entry> entries = new ArrayList<Entry>( files.length );
        
        for ( int i = 0; i < files.length; i++ )
        {
            entries.add( constructEntry( files[i] ) );
        }

        return entries;
    }

    public Entry getEntry( String id )
        throws IOException
    {
        File file = getFileForId( id );

        if ( file.exists() )
        {
            return constructEntry( file );
        }
        else
        {
            return null;
        }
    }

    public void removeEntry( String id )
        throws IOException
    {
        getFileForId( id ).delete();
    }

    public void updateEntry( Entry entry )
        throws IOException
    {
        removeEntry( entry.getId() );

        addEntry( entry );
    }

    protected File getFileForId( String id )
    {
        return new File( getBaseDir(), id + ".xml" );
    }

    protected Entry constructEntry( File file )
        throws IOException
    {
        Entry result = null;

        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream( file );

            Object content = xstream.fromXML( fis );

            result = new DefaultEntry( file.getName().substring( 0, file.getName().length() - 4 ), content );
        }
        catch ( BaseException e )
        {
            // file is corrupt
            file.delete();
        }
        finally
        {
            if ( fis != null )
            {
                fis.close();
            }
        }

        return result;
    }
}
