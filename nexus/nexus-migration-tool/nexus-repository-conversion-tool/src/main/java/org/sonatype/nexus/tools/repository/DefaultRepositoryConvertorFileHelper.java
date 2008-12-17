/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.tools.repository;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;

/**
 * @author Juven Xu
 */
@Component( role = RepositoryConvertorFileHelper.class )
public class DefaultRepositoryConvertorFileHelper
    implements RepositoryConvertorFileHelper
{

    public void copy( File file, File target, String basePath, FileFilter filter )
        throws IOException
    {
        copyOrMove( file, target, basePath, false, filter );
    }

    public void move( File file, File target, String basePath, FileFilter filter )
        throws IOException
    {
        copyOrMove( file, target, basePath, true, filter );
    }

    protected void copyOrMove( File file, File target, String basePath, boolean isMove, FileFilter filter )
        throws IOException
    {
        File targetFile = new File( target, basePath + file.getName() );

        buildDirectoryPath( targetFile.getParentFile() );

        if ( filter != null && !filter.accept( file ) )
        {
            return;
        }
        else if ( file.isDirectory() )
        {
            for ( File child : file.listFiles() )
            {
                copyOrMove( child, target, basePath + file.getName() + File.separatorChar, isMove, filter );
            }
        }
        else if ( file.isFile() && !isMetadataFile( file ) )
        {
            targetFile.getParentFile().mkdirs();
            moveFileContent( file, targetFile );
        }

        if ( isMove )
        {
            file.delete();
        }
    }

    private void moveFileContent( File from, File to )
        throws IOException
    {
        FileInputStream fis = null;

        FileOutputStream fos = null;

        try
        {
            fis = new FileInputStream( from );

            fos = new FileOutputStream( to );

            IOUtil.copy( fis, fos );

            fos.flush();
        }
        catch ( IOException ioe )
        {
            throw ioe;
        }
        finally
        {
            if ( fis != null )
            {
                IOUtil.close( fis );
            }
            if ( fos != null )
            {
                IOUtil.close( fos );
            }
        }

    }

    private void buildDirectoryPath( File directory )
    {
        if ( !directory.getParentFile().exists() )
        {
            buildDirectoryPath( directory.getParentFile() );
        }
        directory.mkdir();
    }

    /**
     * maven-metadata-<repoId>.xml, maven-matedata.xml, and their checksums md5/sha1
     *
     * @param file
     * @return
     */
    private boolean isMetadataFile( File file )
    {
        if ( !file.exists() || !file.isFile() )
        {
            return false;
        }
        if ( file.getName().startsWith( "maven-metadata" ) )
        {
            return true;
        }
        return false;
    }

    public void copy( File file, File target, String basePath )
        throws IOException
    {
        this.copy( file, target, basePath, null );
    }

    public void move( File file, File target, String basePath )
        throws IOException
    {
        this.move( file, target, basePath, null );
    }

}
