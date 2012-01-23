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
package org.sonatype.nexus.tools.metadata;

import java.io.File;
import java.net.URI;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

@Component( role = MetadataRebuilder.class )
public class DefaultMetadataRebuilder extends AbstractLogEnabled
    implements MetadataRebuilder
{

    private String repo;

    @Requirement
    private FSMetadataHelper fSMetadataHelper = new FSMetadataHelper( getLogger() );

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
