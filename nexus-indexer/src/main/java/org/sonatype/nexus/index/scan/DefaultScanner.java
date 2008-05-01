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
