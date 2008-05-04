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
package org.sonatype.nexus.proxy.attributes.inspectors;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.sonatype.nexus.proxy.attributes.AbstractStorageFileItemInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * The Class JarFileInspector inspects and collects classes from JAR files. The findings are stored into item
 * attributes.. Turned OFF, since nexus-indexer is doing this too.
 * 
 * @author cstamas #plexus.component role-hint="jar"
 */
public class JarFileInspector
    extends AbstractStorageFileItemInspector
{

    /** The jar mf key. */
    public static String JAR_MF = "jar.mf";

    /** The jar classes key. */
    public static String JAR_CLASSES = "jar.classes";

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.StorageItemInspector#isHandled(org.sonatype.nexus.item.StorageItem)
     */
    public boolean isHandled( StorageItem item )
    {
        return StorageFileItem.class.isAssignableFrom( item.getClass() )
            && item.getName().toLowerCase().endsWith( "jar" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.StorageItemInspector#getIndexableKeywords()
     */
    public Set<String> getIndexableKeywords()
    {
        Set<String> result = new HashSet<String>( 3 );
        result.add( JAR_CLASSES );
        result.add( JAR_MF );
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.StorageFileItemInspector#processStorageFileItem(org.sonatype.nexus.item.StorageFileItem,
     *      java.io.File)
     */
    public void processStorageFileItem( StorageFileItem item, File file )
        throws IOException
    {
        JarFile jFile = new JarFile( file );
        try
        {
            StringBuffer classes = new StringBuffer( jFile.size() );

            for ( Enumeration<JarEntry> e = jFile.entries(); e.hasMoreElements(); )
            {
                JarEntry entry = (JarEntry) e.nextElement();
                String name = entry.getName();

                if ( name.endsWith( ".class" ) )
                {
                    int i = name.lastIndexOf( "$" );
                    if ( i == -1 )
                    {
                        classes.append( name.substring( 0, name.length() - 6 ) ).append( "\n" );
                    }
                }
            }

            item.getAttributes().put( JAR_CLASSES, classes.toString() );
            // result.setBoolean( LocalStorageFileItem.LOCAL_FILE_IS_CONTAINER_KEY, true );

            Manifest mf = jFile.getManifest();
            if ( mf != null )
            {
                StringBuffer mfEntries = new StringBuffer( jFile.getManifest().getMainAttributes().size() );
                Attributes mAttr = mf.getMainAttributes();
                for ( Iterator<Object> i = mAttr.keySet().iterator(); i.hasNext(); )
                {
                    Attributes.Name atrKey = (Attributes.Name) i.next();
                    mfEntries
                        .append( atrKey.toString() ).append( "=" ).append( mAttr.getValue( atrKey ) ).append( "\n" );
                }
                item.getAttributes().put( JAR_MF, mfEntries.toString() );
            }
        }
        finally
        {
            jFile.close();
        }
    }

}
