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

package org.sonatype.nexus.tools.metadata;

import java.io.File;
import java.net.URI;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;

@Component( role = MetadataRebuilder.class )
public class DefaultMetadataRebuilder
    implements MetadataRebuilder
{

    private String repo;

    @Requirement
    private FSMetadataHelper fSMetadataHelper = new FSMetadataHelper();

    public void rebuildMetadata( String repo )
    {
        this.repo = repo;

        if ( !FileUtils.fileExists( repo ) )
        {
            throw new RuntimeException( "Repository not found: " + repo );
        }

        fSMetadataHelper.setRepo( repo );

        try
        {
            walk( new File( repo ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Can't walk through the repository: " + repo, e );
        }

    }

    private void walk( File file )
        throws Exception
    {
        if ( file.isFile() )
        {
            fSMetadataHelper.processFile( getRelativePathInRepo( file ) );

            return;
        }

        fSMetadataHelper.onDirEnter( getRelativePathInRepo( file ) );

        for ( File subFile : file.listFiles() )
        {
            walk( subFile );
        }

        fSMetadataHelper.onDirExit( getRelativePathInRepo( file ) );
    }

    private String getRelativePathInRepo( File file )
    {
        URI repoURI = new File( repo ).toURI();

        URI fileURI = file.toURI();

        String repoPath = repoURI.toString();

        String filePath = fileURI.toString();

        // have slash at the begining
        String relativePath = "/" + filePath.substring( repoPath.length() );

        // no slash at the end
        if ( relativePath.length() > 1 && relativePath.endsWith( "/" ) )
        {
            return relativePath.substring( 0, relativePath.length() - 1 );
        }
        else
        {
            return relativePath;
        }

    }

}
