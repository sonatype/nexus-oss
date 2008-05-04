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
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.sonatype.nexus.proxy.attributes.AbstractStorageFileItemInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * The Class ZipFileInspector inspects ZIP files and collects directory listings from it. The findings are stored as
 * attributes. Turned OFF, since nexus-indexer is doing this too.
 * 
 * @author cstamas #plexus.component role-hint="zip"
 */
public class ZipFileInspector
    extends AbstractStorageFileItemInspector
{

    /** The ZIP_FILES. */
    public static String ZIP_FILES = "zip.files";

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.StorageItemInspector#isHandled(org.sonatype.nexus.item.StorageItem)
     */
    public boolean isHandled( StorageItem item )
    {
        return StorageFileItem.class.isAssignableFrom( item.getClass() )
            && item.getName().toLowerCase().endsWith( "zip" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.StorageItemInspector#getIndexableKeywords()
     */
    public Set<String> getIndexableKeywords()
    {
        Set<String> result = new HashSet<String>( 1 );
        result.add( ZIP_FILES );
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.StorageFileItemInspector#processStorageFileItem(org.sonatype.nexus.item.StorageFileItem,
     *      java.io.File)
     */
    @SuppressWarnings( "unchecked" )
    public void processStorageFileItem( StorageFileItem item, File file )
        throws IOException
    {
        ZipFile zFile = new ZipFile( file );
        try
        {
            StringBuffer files = new StringBuffer( zFile.size() );

            for ( Enumeration e = zFile.entries(); e.hasMoreElements(); )
            {
                ZipEntry entry = (ZipEntry) e.nextElement();
                if ( !entry.isDirectory() )
                {
                    files.append( entry.getName() );
                    files.append( "\n" );
                }
            }

            item.getAttributes().put( ZIP_FILES, files.toString() );
            // result.setBoolean( LocalStorageFileItem.LOCAL_FILE_IS_CONTAINER_KEY, true );
        }
        finally
        {
            zFile.close();
        }
    }

}
