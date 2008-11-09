/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.scan;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactContextProducer;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;

/**
 * @author Jason Van Zyl
 * @author Tamas Cservenak
 * @plexus.component
 */
public class DefaultScanner
    extends AbstractLogEnabled
    implements Scanner
{
    /** @plexus.requirement */
    private ArtifactContextProducer artifactContextProducer;

    public ScanningResult scan( ScanningRequest request )
    {
        request.getArtifactScanningListener().scanningStarted( request.getIndexingContext() );
      
        ScanningResult result = new DefaultScanningResult();

        scanDirectory( request.getIndexingContext().getRepository(), request, result );

        request.getArtifactScanningListener().scanningFinished( request.getIndexingContext(), result );
        
        return result;
    }

    private void scanDirectory( File dir, ScanningRequest request, ScanningResult result )
    {
        if ( dir == null )
        {
            return;
        }

        File[] fileArray = dir.listFiles();

        if ( fileArray != null )
        {
            Set<File> files = new TreeSet<File>( Arrays.asList( fileArray ) );

            Set<String> uinfos = new HashSet<String>();
            
            for ( File f : files )
            {
                if ( f.getName().startsWith( "." ) )
                {
                    continue;  // skip all hidden files and directories
                }

                if ( f.isDirectory() )
                {
                    scanDirectory( f, request, result );
                }
                else if ( !AbstractIndexCreator.isIndexable( f ) )
                {
                    continue;  // skip non-indexable files
                }
                else
                {
                    scanFile( f, request, result, uinfos );
                }
            }
        }
    }

    private void scanFile( File file, ScanningRequest request, ScanningResult result, Set<String> uinfos ) 
    {
        String repoFile = file.getAbsolutePath().substring(
            request.getIndexingContext().getRepository().getAbsolutePath().length() + 1 );
        
        GavCalculator gavCalculator = request.getIndexingContext().getGavCalculator();
        
        Gav gav = gavCalculator.pathToGav( repoFile.replace( '\\', '/' ) );
        
        if ( gav != null )
        {
            String uinfo = AbstractIndexCreator.getGAV( gav.getGroupId(), //
                gav.getArtifactId(),
                gav.getBaseVersion(),
                gav.getClassifier(),
                gav.getExtension() );
            
            if( !uinfos.contains( uinfo ) )
            {
                uinfos.add( uinfo );  // skip multiple snapshots
          
                processFile( file, request, result );
            }
        }
    }

    private void processFile( File file, ScanningRequest request, ScanningResult result )
    {
        result.incrementCount();

        IndexingContext context = request.getIndexingContext();

        ArtifactContext ac = artifactContextProducer.getArtifactContext( context, file );

        if ( ac != null )
        {
            request.getArtifactScanningListener().artifactDiscovered( ac );
        }
    }
}
