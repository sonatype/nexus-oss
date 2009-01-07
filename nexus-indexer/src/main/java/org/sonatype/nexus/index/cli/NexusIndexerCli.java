/**
 * Copyright © 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 * Eugene Kuleshov (Sonatype)
 * Tamas Cservenak (Sonatype)
 * Brian Fox (Sonatype)
 * Jason Van Zyl (Sonatype)
 */
package org.sonatype.nexus.index.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.tools.cli.AbstractCli;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactScanningListener;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.IndexCreator;
import org.sonatype.nexus.index.packer.IndexChunker;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.packer.IndexPackingRequest;
import org.sonatype.nexus.index.scan.ScanningResult;

public class NexusIndexerCli
    extends AbstractCli
{
    // Command line options

    public static final char REPO = 'r';

    public static final char INDEX = 'i';

    public static final char NAME = 'n';

    public static final char TYPE = 't';

    public static final char TARGET_DIR = 'd';

    public static final char CHUNK_RESOLUTION = 'c';

    public static final String CHUNK_RESOLUTION_NONE = "none";

    private static final long MB = 1024 * 1024;

    public static void main( String[] args )
        throws Exception
    {
        new NexusIndexerCli().execute( args );
    }

    @Override
    public String getPomPropertiesPath()
    {
        return "META-INF/maven/org.sonatype.nexus/nexus-indexer/pom.properties";
    }

    @Override
    @SuppressWarnings( "static-access" )
    public Options buildCliOptions( Options options )
    {
        options.addOption( OptionBuilder.withLongOpt( "index" ).hasArg() //
        .withDescription( "Path to the index folder." ).create( INDEX ) );

        options.addOption( OptionBuilder.withLongOpt( "target" ) //
        .withDescription( "Target folder." ).create( TARGET_DIR ) );

        options.addOption( OptionBuilder.withLongOpt( "repository" ).hasArg() //
        .withDescription( "Path to the Maven repository." ).create( REPO ) );

        options.addOption( OptionBuilder.withLongOpt( "name" ).hasArg() //
        .withDescription( "Repository name." ).create( NAME ) );

        options.addOption( OptionBuilder.withLongOpt( "type" ).hasArg() //
        .withDescription( "Indexer type (default, min, full or coma separated list of custom types)." ).create( TYPE ) );

        options.addOption( OptionBuilder
            .withLongOpt( "resolution" ).hasArg()
            //
            .withDescription(
                "Incremental index chunk resolution: 'day' (default), 'week', 'month' and 'none'." ).create(
                CHUNK_RESOLUTION ) );

        return options;
    }

    @Override
    public void invokePlexusComponent( final CommandLine cli, PlexusContainer plexus )
        throws Exception
    {
        if ( cli.hasOption( INDEX ) )
        {
            index( cli, plexus );
        }
        else
        {
            displayHelp();
        }
    }

    private void index( final CommandLine cli, PlexusContainer plexus )
        throws ComponentLookupException,
            IOException,
            UnsupportedExistingLuceneIndexException
    {
        String indexDirectoryName = cli.getOptionValue( INDEX );

        File indexFolder = new File( indexDirectoryName );

        File outputFolder = new File( cli.hasOption( TARGET_DIR ) ? cli.getOptionValue( TARGET_DIR ) : "." );

        File repositoryFolder = new File( cli.getOptionValue( REPO ) );

        String repositoryName = cli.hasOption( NAME ) ? cli.getOptionValue( NAME ) : indexFolder.getName();

        List<IndexCreator> indexers = getIndexers( cli, plexus );

        System.err.printf( "Repository Folder: %s\n", repositoryFolder.getAbsolutePath() );
        System.err.printf( "Index Folder:      %s\n", indexFolder.getAbsolutePath() );
        System.err.printf( "Output Folder:     %s\n", outputFolder.getAbsolutePath() );
        System.err.printf( "Repository name:   %s\n", repositoryName );
        System.err.printf( "Indexers: %s\n", indexers.toString() );

        NexusIndexer indexer = (NexusIndexer) plexus.lookup( NexusIndexer.class );

        long tstart = System.currentTimeMillis();

        IndexingContext context = indexer.addIndexingContext( //
            repositoryName, // context id
            repositoryName, // repository id
            repositoryFolder, // repository folder
            indexFolder, // index folder
            null, // repositoryUrl
            null, // index update url
            indexers );

        IndexPacker packer = (IndexPacker) plexus.lookup( IndexPacker.class );

        boolean debug = cli.hasOption( DEBUG );

        ArtifactScanningListener listener = new IndexerListener( context, debug );

        indexer.scan( context, listener, true );

        IndexPackingRequest request = new IndexPackingRequest( context, outputFolder );

        if ( cli.hasOption( CHUNK_RESOLUTION ) )
        {
            String resolution = cli.getOptionValue( CHUNK_RESOLUTION );

            if ( CHUNK_RESOLUTION_NONE.equalsIgnoreCase( resolution ) )
            {
                request.setCreateIncrementalChunks( false );
            }
            else
            {
                IndexChunker indexChunker = (IndexChunker) plexus.lookup( IndexChunker.class, resolution );

                request.setIndexChunker( indexChunker );
            }
        }

        packIndex( packer, request, debug );

        // print stats

        long t = System.currentTimeMillis() - tstart;

        long s = t / 1000L;

        if ( t > 60 * 1000 )
        {
            long m = t / 1000L / 60L;

            System.err.printf( "Total time:   %d min %d sec\n", m, s - ( m * 60 ) );
        }
        else
        {
            System.err.printf( "Total time:   %d sec\n", s );
        }

        Runtime r = Runtime.getRuntime();

        System.err.printf( "Final memory: %dM/%dM\n", //
            ( r.totalMemory() - r.freeMemory() ) / MB,
            r.totalMemory() / MB );
    }

    private List<IndexCreator> getIndexers( final CommandLine cli, PlexusContainer plexus )
        throws ComponentLookupException
    {
        String type = "default";

        if ( cli.hasOption( TYPE ) )
        {
            type = cli.getOptionValue( TYPE );
        }

        List<IndexCreator> indexers = new ArrayList<IndexCreator>(); // NexusIndexer.DEFAULT_INDEX;

        if ( "default".equals( type ) )
        {
            indexers.add( (IndexCreator) plexus.lookup( IndexCreator.class, "min" ) );
            indexers.add( (IndexCreator) plexus.lookup( IndexCreator.class, "jarContent" ) );
        }
        else if ( "full".equals( type ) )
        {
            for ( Object component : plexus.lookupList( IndexCreator.class ) )
            {
                indexers.add( (IndexCreator) component );
            }
        }
        else
        {
            for ( String hint : type.split( "," ) )
            {
                indexers.add( (IndexCreator) plexus.lookup( IndexCreator.class, hint ) );
            }
        }
        return indexers;
    }

    private void packIndex( IndexPacker packer, IndexPackingRequest request, boolean debug )
    {
        try
        {
            packer.packIndex( request );
        }
        catch ( IOException e )
        {
            System.err.printf( "Cannot zip index; \n", e.getMessage() );

            if ( debug )
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Scanner listener
     */
    private static final class IndexerListener
        implements ArtifactScanningListener
    {
        private final IndexingContext context;

        private final boolean debug;

        private long ts = System.currentTimeMillis();

        private int count;

        IndexerListener( IndexingContext context, boolean debug )
        {
            this.context = context;
            this.debug = debug;
        }

        public void scanningStarted( IndexingContext context )
        {
            System.err.println( "Scanning started" );
        }

        public void artifactDiscovered( ArtifactContext ac )
        {
            count++;

            long t = System.currentTimeMillis();

            ArtifactInfo ai = ac.getArtifactInfo();

            if ( debug && "maven-plugin".equals( ai.packaging ) )
            {
                System.err.printf( "Plugin: %s:%s:%s - %s %s\n", //
                    ai.groupId,
                    ai.artifactId,
                    ai.version,
                    ai.prefix,
                    "" + ai.goals );
            }

            if ( ( t - ts ) > 2000L )
            {
                System.err.printf( "  %6d %s\n", count, formatFile( ac.getPom() ) );
                ts = t;
            }
        }

        public void artifactError( ArtifactContext ac, Exception e )
        {
            System.err.printf( "! %6d %s - %s\n", count, formatFile( ac.getPom() ), e.getMessage() );

            System.err.printf( "         %s\n", formatFile( ac.getArtifact() ) );

            if ( debug )
            {
                e.printStackTrace();
            }

            ts = System.currentTimeMillis();
        }

        private String formatFile( File file )
        {
            return file.getAbsolutePath().substring( context.getRepository().getAbsolutePath().length() + 1 );
        }

        public void scanningFinished( IndexingContext context, ScanningResult result )
        {
            if ( result.hasExceptions() )
            {
                System.err.printf( "Scanning errors:   %s\n", result.getExceptions().size() );
            }

            System.err.printf( "Artifacts added:   %s\n", result.getTotalFiles() );
            System.err.printf( "Artifacts deleted: %s\n", result.getDeletedFiles() );
        }
    }

}
