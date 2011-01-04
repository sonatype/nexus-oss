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
package org.sonatype.nexus.rt.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preferences implementation that stores to a user-defined file. See FilePreferencesFactory. Modified by cstamas,
 * switched to SLF4J logging, and exposed preferences file property.
 * 
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @version $Id: FilePreferences.java 283 2009-06-18 17:06:58Z david $
 */
public class FilePreferences
    extends AbstractPreferences
{
    private static final Logger log = LoggerFactory.getLogger( FilePreferences.class.getName() );

    private Map<String, String> root;

    private Map<String, FilePreferences> children;

    private boolean isRemoved = false;

    public FilePreferences( AbstractPreferences parent, String name )
    {
        super( parent, name );

        log.debug( "Instantiating node {}", name );

        root = new TreeMap<String, String>();
        children = new TreeMap<String, FilePreferences>();

        try
        {
            sync();
        }
        catch ( BackingStoreException e )
        {
            log.error( "Unable to sync on creation of node " + name, e );
        }
    }

    @Override
    protected void putSpi( String key, String value )
    {
        root.put( key, value );
        try
        {
            flush();
        }
        catch ( BackingStoreException e )
        {
            log.error( "Unable to flush after putting " + key, e );
        }
    }

    @Override
    protected String getSpi( String key )
    {
        return root.get( key );
    }

    @Override
    protected void removeSpi( String key )
    {
        root.remove( key );
        try
        {
            flush();
        }
        catch ( BackingStoreException e )
        {
            log.error( "Unable to flush after removing " + key, e );
        }
    }

    @Override
    protected void removeNodeSpi()
        throws BackingStoreException
    {
        isRemoved = true;
        flush();
    }

    @Override
    protected String[] keysSpi()
        throws BackingStoreException
    {
        return root.keySet().toArray( new String[root.keySet().size()] );
    }

    @Override
    protected String[] childrenNamesSpi()
        throws BackingStoreException
    {
        return children.keySet().toArray( new String[children.keySet().size()] );
    }

    @Override
    protected FilePreferences childSpi( String name )
    {
        FilePreferences child = children.get( name );
        if ( child == null || child.isRemoved() )
        {
            child = new FilePreferences( this, name );
            children.put( name, child );
        }
        return child;
    }

    @Override
    protected void syncSpi()
        throws BackingStoreException
    {
        if ( isRemoved() )
        {
            return;
        }

        final File file = FilePreferencesFactory.getPreferencesFile();

        if ( !file.exists() )
        {
            return;
        }

        synchronized ( file )
        {
            Properties p = new Properties();
            try
            {
                final FileInputStream in = new FileInputStream( file );
                try
                {
                    p.load( in );
                }
                finally
                {
                    IOUtils.closeQuietly( in );
                }

                StringBuilder sb = new StringBuilder();
                getPath( sb );
                String path = sb.toString();

                final Enumeration<?> pnen = p.propertyNames();
                while ( pnen.hasMoreElements() )
                {
                    String propKey = (String) pnen.nextElement();
                    if ( propKey.startsWith( path ) )
                    {
                        String subKey = propKey.substring( path.length() );
                        // Only load immediate descendants
                        if ( subKey.indexOf( '.' ) == -1 )
                        {
                            root.put( subKey, p.getProperty( propKey ) );
                        }
                    }
                }
            }
            catch ( IOException e )
            {
                throw new BackingStoreException( e );
            }
        }
    }

    private void getPath( StringBuilder sb )
    {
        final FilePreferences parent = (FilePreferences) parent();
        if ( parent == null )
        {
            return;
        }

        parent.getPath( sb );
        sb.append( name() ).append( '.' );
    }

    @Override
    protected void flushSpi()
        throws BackingStoreException
    {
        final File file = FilePreferencesFactory.getPreferencesFile();

        synchronized ( file )
        {
            Properties p = new Properties();
            try
            {

                StringBuilder sb = new StringBuilder();
                getPath( sb );
                String path = sb.toString();

                if ( file.exists() )
                {
                    final FileInputStream in = new FileInputStream( file );
                    try
                    {
                        p.load( in );
                    }
                    finally
                    {
                        IOUtils.closeQuietly( in );
                    }

                    List<String> toRemove = new ArrayList<String>();

                    // Make a list of all direct children of this node to be removed
                    final Enumeration<?> pnen = p.propertyNames();
                    while ( pnen.hasMoreElements() )
                    {
                        String propKey = (String) pnen.nextElement();
                        if ( propKey.startsWith( path ) )
                        {
                            String subKey = propKey.substring( path.length() );
                            // Only do immediate descendants
                            if ( subKey.indexOf( '.' ) == -1 )
                            {
                                toRemove.add( propKey );
                            }
                        }
                    }

                    // Remove them now that the enumeration is done with
                    for ( String propKey : toRemove )
                    {
                        p.remove( propKey );
                    }
                }

                // If this node hasn't been removed, add back in any values
                if ( !isRemoved )
                {
                    for ( String s : root.keySet() )
                    {
                        p.setProperty( path + s, root.get( s ) );
                    }
                }

                final FileOutputStream out = new FileOutputStream( file );
                try
                {
                    p.store( out, "FilePreferences" );
                }
                finally
                {
                    IOUtils.closeQuietly( out );
                }
            }
            catch ( IOException e )
            {
                throw new BackingStoreException( e );
            }
        }
    }
}