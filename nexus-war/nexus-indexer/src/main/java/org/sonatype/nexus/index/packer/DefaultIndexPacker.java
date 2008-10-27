/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
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
