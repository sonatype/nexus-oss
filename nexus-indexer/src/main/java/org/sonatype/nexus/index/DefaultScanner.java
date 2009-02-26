/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A default repository scanner for Maven 2 repository.
 * 
 * @author Jason Van Zyl
 * @author Tamas Cservenak
 */
@Component( role = Scanner.class )
public class DefaultScanner
    extends AbstractLogEnabled
    implements Scanner
{
    @Requirement
    private ArtifactContextProducer artifactContextProducer;

    public ScanningResult scan( ScanningRequest request )
    {
        request.getArtifactScanningListener().scanningStarted( request.getIndexingContext() );
      
        ScanningResult result = new ScanningResult();

        scanDirectory( request.getIndexingContext().getRepository(), request );

        request.getArtifactScanningListener().scanningFinished( request.getIndexingContext(), result );
        
        return result;
    }

    private void scanDirectory( File dir, ScanningRequest request )
    {
        if ( dir == null )
        {
            return;
        }

        File[] fileArray = dir.listFiles();

        if ( fileArray != null )
        {
            Set<File> files = new TreeSet<File>( Arrays.asList( fileArray ) );

            for ( File f : files )
            {
                if ( f.getName().startsWith( "." ) )
                {
                    continue;  // skip all hidden files and directories
                }

                if ( f.isDirectory() )
                {
                    scanDirectory( f, request );
                }
//                else if ( !AbstractIndexCreator.isIndexable( f ) )
//                {
//                    continue;  // skip non-indexable files
//                }
                else
                {
                    processFile( f, request );
                }
            }
        }
    }

    private void processFile( File file, ScanningRequest request )
    {
        IndexingContext context = request.getIndexingContext();

        ArtifactContext ac = artifactContextProducer.getArtifactContext( context, file );

        if ( ac != null )
        {
            request.getArtifactScanningListener().artifactDiscovered( ac );
        }
    }
}
