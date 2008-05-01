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
