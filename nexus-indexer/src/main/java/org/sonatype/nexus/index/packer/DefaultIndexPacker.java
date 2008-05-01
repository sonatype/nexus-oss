/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
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
package org.sonatype.nexus.index.packer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.index.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * Default provider for IndexPacker. Creates the Peoperties and ZIP files.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultIndexPacker
    extends AbstractLogEnabled
    implements IndexPacker
{

    private SimpleDateFormat df = new SimpleDateFormat( IndexingContext.INDEX_TIME_FORMAT );

    public void packIndex( IndexingContext context, File targetDir )
        throws IOException,
            IllegalArgumentException
    {
        if ( targetDir == null )
        {
            throw new IllegalArgumentException( "The supplied targetDir is null!" );
        }

        if ( !targetDir.exists() )
        {
            targetDir.mkdirs();
        }
        else
        {
            if ( !targetDir.isDirectory() || !targetDir.canWrite() )
            {
                throw new IllegalArgumentException( "The supplied targetDir (" + targetDir.getAbsolutePath()
                    + ") is not directory or is not writable!" );
            }
        }

        writeIndexProperties( context, targetDir );

        writeIndexArchive( context, targetDir );
    }

    private void writeIndexProperties( IndexingContext context, File targetDir )
        throws IOException
    {
        Properties info = new Properties();

        info.setProperty( IndexingContext.INDEX_ID, context.getId() );

        info.setProperty( IndexingContext.INDEX_TIMESTAMP, df.format( context.getTimestamp() ) );

        File indexInfo = new File( targetDir, IndexingContext.INDEX_FILE + ".properties" );

        OutputStream os = null;

        try
        {
            os = new FileOutputStream( indexInfo );

            info.store( os, null );
        }
        finally
        {
            IOUtil.close( os );
        }
    }

    private void writeIndexArchive( IndexingContext context, File targetDir )
        throws IOException
    {
        File indexArchive = new File( targetDir, IndexingContext.INDEX_FILE + ".zip" );

        OutputStream os = null;

        try
        {
            os = new BufferedOutputStream( new FileOutputStream( indexArchive ), 4096 );

            IndexUtils.packIndexArchive( context, os );
        }
        finally
        {
            IOUtil.close( os );
        }
    }
}
