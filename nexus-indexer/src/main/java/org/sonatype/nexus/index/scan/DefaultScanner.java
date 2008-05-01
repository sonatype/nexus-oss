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
package org.sonatype.nexus.index.scan;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.M2GavCalculator;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactContextProducer;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;

/**
 * @author Jason Van Zyl
 * @author cstamas
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
        ScanningResult result = new DefaultScanningResult();

        scanDirectory( request.getIndexingContext().getRepository(), request, result );

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

            TreeSet<File> files = new TreeSet<File>( Arrays.asList( fileArray ) );

            for ( File f : files )
            {
                if ( f.getName().startsWith( "." ) )
                {
                    continue; // skip all hidden files and directories
                }

                if ( f.isDirectory() )
                {
                    scanDirectory( f, request, result );
                }
                else
                {
                    try
                    {
                        String fileName = f.getName();

                        if ( fileName.startsWith( "maven-metadata" ) || fileName.endsWith( "-javadoc.jar" )
                            || fileName.endsWith( ".properties" ) || fileName.endsWith( "-javadocs.jar" )
                            || fileName.endsWith( "-sources.jar" ) || fileName.endsWith( ".pom" )
                            && files.contains( new File( f.getParent(), fileName.replaceAll( "\\.pom$", ".jar" ) ) )
                            || fileName.endsWith( ".xml" ) || fileName.endsWith( ".asc" ) || fileName.endsWith( ".md5" )
                            || fileName.endsWith( ".sha1" ) )
                        {
                            continue;
                        }

                        if ( request.getInfos() != null )
                        {
                            String repoFile = f.getAbsolutePath().substring(
                                request.getIndexingContext().getRepository().getAbsolutePath().length() + 1 );

                            Gav gav = M2GavCalculator.calculate( repoFile.replace( '\\', '/' ) );

                            if ( gav != null )
                            {
                                String uinfo = AbstractIndexCreator.getGAV( gav.getGroupId(), //
                                    gav.getArtifactId(),
                                    gav.getVersion(),
                                    gav.getClassifier() );

                                if ( request.getInfos().contains( uinfo ) )
                                {
                                    continue; // skip already indexed file
                                }
                            }
                        }

                        processFile( f, request, result );
                    }
                    catch ( IOException e )
                    {
                        result.addException( e );
                    }
                }
            }
        }
    }

    private void processFile( File file, ScanningRequest request, ScanningResult result )
        throws IOException
    {
        result.incrementCount();

        IndexingContext context = request.getIndexingContext();

        ArtifactContext ac = artifactContextProducer.getArtifactContext( context, file );

        if ( ac != null )
        {
            try
            {
                request.getNexusIndexer().artifactDiscovered( ac, context );

                if ( request.getArtifactScanningListener() != null )
                {
                    request.getArtifactScanningListener().artifactDiscovered( ac );
                }
            }
            catch ( IOException e )
            {
                if ( request.getArtifactScanningListener() != null )
                {
                    request.getArtifactScanningListener().artifactError( ac, e );
                }
                throw e;
            }
        }
    }
}
