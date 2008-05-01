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
package org.sonatype.nexus.index.archive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Jason van Zyl
 * @plexus.component
 */
public class DefaultArchiver
    implements Archiver
{
    public void zip( File sourceDirectory, File archive )
        throws IOException
    {
        String name = sourceDirectory.getName();

        ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( new File( sourceDirectory.getParent(), name
            + ".zip" ) ) );

        zos.setLevel( 9 );

        File[] files = sourceDirectory.listFiles();

        for ( int i = 0; i < files.length; i++ )
        {
            ZipEntry e = new ZipEntry( files[i].getName() );

            zos.putNextEntry( e );

            FileInputStream is = new FileInputStream( files[i] );

            byte[] buf = new byte[4096];

            int n;

            while ( ( n = is.read( buf ) ) > 0 )
            {
                zos.write( buf, 0, n );
            }

            is.close();

            zos.flush();

            zos.closeEntry();
        }

        zos.close();
    }

    public void unzip( File archive, File targetDirectory )
        throws IOException
    {
        InputStream in = new BufferedInputStream( new FileInputStream( archive ) );

        ZipInputStream zin = new ZipInputStream( in );

        ZipEntry e;

        while ( ( e = zin.getNextEntry() ) != null )
        {
            FileOutputStream out = new FileOutputStream( new File( targetDirectory, e.getName() ) );

            byte[] b = new byte[512];

            int len;

            while ( ( len = zin.read( b ) ) != -1 )
            {
                out.write( b, 0, len );
            }

            out.close();
        }

        zin.close();
    }
}
