/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
