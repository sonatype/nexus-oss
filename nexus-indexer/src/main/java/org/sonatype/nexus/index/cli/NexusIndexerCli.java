/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.cli;

import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Proxy;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.tools.cli.AbstractCli;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactScanningListener;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.ScanningResult;
import org.sonatype.nexus.index.updater.DefaultIndexUpdater;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.JarFileContentsIndexCreator;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.packer.IndexPackingRequest;
import org.sonatype.nexus.index.packer.IndexPackingRequest.IndexFormat;

/**
 * A command line tool that can be used to index local Maven repository.
 * <p>
 * The following command line options are supported:
 * <ul>
 * <li>-repository <path> : required path to repository to be indexed</li>
 * <li>-index <path> : required index folder used to store created index or where previously created index is stored</li>
 * <li>-name <path> : required repository name/id</li>
 * <li>-target <path> : optional folder name where to save produced index files</li>
 * <li>-type <path> : optional indexer types</li>
 * <li>-format <path> : optional indexer formats</li>
 * </ul>
 * When index folder contains previously created index, the tool will use it as a base line and will generate chunks for
 * the incremental updates.
 * <p>
 * The indexer types could be one of default, min or full. You can also specify list of coma-separated custom index
 * creators. An index creator should be a regular Plexus component, see {@link MinimalArtifactInfoIndexCreator} and
 * {@link JarFileContentsIndexCreator}.
 */
public class NexusIndexerCli
    extends AbstractCli
{
    // Command line options

    public static final char REPO = 'r';

    public static final char INDEX = 'i';

    public static final char NAME = 'n';

    public static final char TYPE = 't';

    public static final char TARGET_DIR = 'd';

    public static final char CREATE_INCREMENTAL_CHUNKS = 'c';

    public static final char CREATE_FILE_CHECKSUMS = 's';

    public static final char INCREMENTAL_CHUNK_KEEP_COUNT = 'k';

    public static final char LEGACY = 'l';

    public static final char UNPACK = 'u';

    private static final long MB = 1024 * 1024;

    private Options options;

    private int status = 0;

    public static void main( String[] args )
        throws Exception
    {
        NexusIndexerCli cli = new NexusIndexerCli();

        cli.execute( args );

        System.exit( cli.status );
    }

    @Override
    public int execute( String[] arg0, ClassWorld arg1 )
    {
        int value = super.execute( arg0, arg1 );

        if ( status == 0 )
        {
            status = value;
        }

        return status;
    }

    @Override
    public int execute( String[] args )
    {
        int value = super.execute( args );

        if ( status == 0 )
        {
            status = value;
        }

        return status;
    }

    @Override
    protected void showError( String message, Exception e, boolean show )
    {
        status = 1;
        super.showError( message, e, show );
    }

    @Override
    protected int showFatalError( String message, Exception e, boolean show )
    {
        status = 1;
        return super.showFatalError( message, e, show );
    }

    @Override
    public CommandLine parse( String[] args )
        throws ParseException
    {
        try
        {
            return super.parse( args );
        }
        catch ( ParseException e )
        {
            status = 1;
            throw e;
        }
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
        this.options = options;

        options.addOption( OptionBuilder.withLongOpt( "index" ).hasArg() //
        .withDescription( "Path to the index folder." ).create( INDEX ) );

        options.addOption( OptionBuilder.withLongOpt( "destination" ).hasArg() //
        .withDescription( "Target folder." ).create( TARGET_DIR ) );

        options.addOption( OptionBuilder.withLongOpt( "repository" ).hasArg() //
        .withDescription( "Path to the Maven repository." ).create( REPO ) );

        options.addOption( OptionBuilder.withLongOpt( "name" ).hasArg() //
        .withDescription( "Repository name." ).create( NAME ) );

        options.addOption( OptionBuilder.withLongOpt( "chunks" ) //
        .withDescription( "Create incremental chunks." ).create( CREATE_INCREMENTAL_CHUNKS ) );

        options.addOption( OptionBuilder.withLongOpt( "keep" ).hasArg()
        .withDescription( "Number of incremental chunks to keep." ).create( INCREMENTAL_CHUNK_KEEP_COUNT ) );

        options.addOption( OptionBuilder.withLongOpt( "checksums" ) //
        .withDescription( "Create checksums for all files (sha1, md5)." ).create( CREATE_FILE_CHECKSUMS ) );

        options.addOption( OptionBuilder.withLongOpt( "type" ).hasArg() //
        .withDescription( "Indexer type (default, min, full or coma separated list of custom types)." ).create( TYPE ) );

        options.addOption( OptionBuilder.withLongOpt( "legacy" ) //
        .withDescription( "Build legacy .zip index file" ).create( LEGACY ) );

        options.addOption( OptionBuilder.withLongOpt( "unpack" ) //
        .withDescription( "Unpack an index file" ).create( UNPACK ) );

        return options;
    }

    @Override
    public void displayHelp()
    {
        System.out.println();

        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp( "nexus-indexer [options]", "\nOptions:", options, "\n" );
    }

    @Override
    public void invokePlexusComponent( final CommandLine cli, PlexusContainer plexus )
        throws Exception
    {
        if ( cli.hasOption( QUIET ) )
        {
            setLogLevel( plexus, Logger.LEVEL_DISABLED );
        }
        else if ( cli.hasOption( DEBUG ) )
        {
            setLogLevel( plexus, Logger.LEVEL_DEBUG );
        }
        else if ( cli.hasOption( ERRORS ) )
        {
            setLogLevel( plexus, Logger.LEVEL_ERROR );
        }

        if( cli.hasOption( UNPACK ) )
        {
            unpack( cli, plexus );
        }
        else if ( cli.hasOption( INDEX ) )
        {
            index( cli, plexus );
        }
        else
        {
            status = 1;
            displayHelp();
        }
    }

    private void setLogLevel( PlexusContainer plexus, int logLevel )
        throws ComponentLookupException
    {
        plexus.lookup( LoggerManager.class ).setThreshold( logLevel );
    }

    private void index( final CommandLine cli, PlexusContainer plexus )
        throws ComponentLookupException,
            IOException,
            UnsupportedExistingLuceneIndexException
    {
        String indexDirectoryName = cli.getOptionValue( INDEX );

        File indexFolder = new File( indexDirectoryName );

        String outputDirectoryName = cli.getOptionValue( TARGET_DIR, "." );

        File outputFolder = new File( outputDirectoryName );

        File repositoryFolder = new File( cli.getOptionValue( REPO ) );

        String repositoryName = cli.getOptionValue( NAME, indexFolder.getName() );

        List<IndexCreator> indexers = getIndexers( cli, plexus );

        boolean createChecksums = cli.hasOption( CREATE_FILE_CHECKSUMS );

        boolean createIncrementalChunks = cli.hasOption( CREATE_INCREMENTAL_CHUNKS );

        boolean createLegacyIndex = cli.hasOption( LEGACY );

        boolean debug = cli.hasOption( DEBUG );

        boolean quiet = cli.hasOption( QUIET );

        Integer chunkCount = cli.hasOption( INCREMENTAL_CHUNK_KEEP_COUNT ) ? Integer.parseInt( cli.getOptionValue( INCREMENTAL_CHUNK_KEEP_COUNT ) ) : null;

        if ( !quiet )
        {
            System.err.printf( "Repository Folder: %s\n", repositoryFolder.getAbsolutePath() );
            System.err.printf( "Index Folder:      %s\n", indexFolder.getAbsolutePath() );
            System.err.printf( "Output Folder:     %s\n", outputFolder.getAbsolutePath() );
            System.err.printf( "Repository name:   %s\n", repositoryName );
            System.err.printf( "Indexers: %s\n", indexers.toString() );

            if ( createChecksums )
            {
                System.err.printf( "Will create checksum files for all published files (sha1, md5).\n" );
            }
            else
            {
                System.err.printf( "Will not create checksum files.\n" );
            }

            if ( createIncrementalChunks )
            {
                System.err.printf( "Will create incremental chunks for changes, along with baseline file.\n" );
            }
            else
            {
                System.err.printf( "Will create baseline file.\n" );
            }

            if ( createLegacyIndex )
            {
                System.err.printf( "Will also create legacy .zip index file.\n" );
            }
        }

        NexusIndexer indexer = plexus.lookup( NexusIndexer.class );

        long tstart = System.currentTimeMillis();

        IndexingContext context = indexer.addIndexingContext( //
            repositoryName, // context id
            repositoryName, // repository id
            repositoryFolder, // repository folder
            indexFolder, // index folder
            null, // repositoryUrl
            null, // index update url
            indexers );

        IndexPacker packer = plexus.lookup( IndexPacker.class );

        ArtifactScanningListener listener = new IndexerListener( context, debug, quiet );

        indexer.scan( context, listener, true );

        IndexPackingRequest request = new IndexPackingRequest( context, outputFolder );

        request.setCreateChecksumFiles( createChecksums );

        request.setCreateIncrementalChunks( createIncrementalChunks );

        if ( createLegacyIndex )
        {
            request.setFormats( Arrays.asList( IndexFormat.FORMAT_LEGACY, IndexFormat.FORMAT_V1 ) );
        }
        else
        {
            request.setFormats( Arrays.asList( IndexFormat.FORMAT_V1 ) );
        }

        if ( chunkCount != null )
        {
            request.setMaxIndexChunks( chunkCount.intValue() );
        }

        packIndex( packer, request, debug, quiet );

        if( !quiet )
        {
            printStats( tstart );
        }
    }

    private void unpack( CommandLine cli, PlexusContainer plexus )
        throws ComponentLookupException, IOException
    {
        final String indexDirectoryName = cli.getOptionValue( INDEX, "." );
        final File indexFolder = new File( indexDirectoryName ).getCanonicalFile();
        final File indexArchive = new File( indexFolder, IndexingContext.INDEX_FILE + ".gz" );

        final String outputDirectoryName = cli.getOptionValue( TARGET_DIR, "." );
        final File outputFolder = new File( outputDirectoryName ).getCanonicalFile();

        final boolean quiet = cli.hasOption( QUIET );
        if( !quiet )
        {
            System.err.printf( "Index Folder:      %s\n", indexFolder.getAbsolutePath() );
            System.err.printf( "Output Folder:     %s\n", outputFolder.getAbsolutePath() );
        }

        long tstart = System.currentTimeMillis();

        final FSDirectory directory = FSDirectory.getDirectory( outputFolder );

        final List<IndexCreator> indexers = getIndexers( cli, plexus );

        BufferedInputStream is = null;
        try
        {
            is = new BufferedInputStream( new FileInputStream( indexArchive ) );
            DefaultIndexUpdater.unpackIndexData(
                is,
                directory,
                (IndexingContext) Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{ IndexingContext.class },
                    new PartialImplementation()
                    {
                        public List<IndexCreator> getIndexCreators()
                        {
                            return indexers;
                        }
                    }
                )

            );
        }
        finally
        {
            IOUtil.close( is );
            if( directory != null )
            {
                directory.close();
            }
        }

        if( !quiet )
        {
            printStats( tstart );
        }
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
            indexers.add( plexus.lookup( IndexCreator.class, "min" ) );
            indexers.add( plexus.lookup( IndexCreator.class, "jarContent" ) );
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
                indexers.add( plexus.lookup( IndexCreator.class, hint ) );
            }
        }
        return indexers;
    }

    private void packIndex( IndexPacker packer, IndexPackingRequest request, boolean debug, boolean quiet )
    {
        try
        {
            packer.packIndex( request );
        }
        catch ( IOException e )
        {
            if ( !quiet )
            {
                System.err.printf( "Cannot zip index; \n", e.getMessage() );

                if ( debug )
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printStats( final long startTimeInMillis )
    {
        long t = System.currentTimeMillis() - startTimeInMillis;

        long s = t / 1000L;
        if( t > 60 * 1000 )
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
                           r.totalMemory() / MB
        );
    }

    /**
     * Scanner listener
     */
    private static final class IndexerListener
        implements ArtifactScanningListener
    {
        private final IndexingContext context;

        private final boolean debug;

        private boolean quiet;

        private long ts = System.currentTimeMillis();

        private int count;

        IndexerListener( IndexingContext context, boolean debug, boolean quiet )
        {
            this.context = context;
            this.debug = debug;
            this.quiet = quiet;
        }

        public void scanningStarted( IndexingContext context )
        {
            if ( !quiet )
            {
                System.err.println( "Scanning started" );
            }
        }

        public void artifactDiscovered( ArtifactContext ac )
        {
            count++;

            long t = System.currentTimeMillis();

            ArtifactInfo ai = ac.getArtifactInfo();

            if ( !quiet && debug && "maven-plugin".equals( ai.packaging ) )
            {
                System.err.printf( "Plugin: %s:%s:%s - %s %s\n", //
                    ai.groupId,
                    ai.artifactId,
                    ai.version,
                    ai.prefix,
                    "" + ai.goals );
            }

            if ( !quiet && ( debug || ( t - ts ) > 2000L ) )
            {
                System.err.printf( "  %6d %s\n", count, formatFile( ac.getPom() ) );
                ts = t;
            }
        }

        public void artifactError( ArtifactContext ac, Exception e )
        {
            if ( !quiet )
            {
                System.err.printf( "! %6d %s - %s\n", count, formatFile( ac.getPom() ), e.getMessage() );

                System.err.printf( "         %s\n", formatFile( ac.getArtifact() ) );

                if ( debug )
                {
                    e.printStackTrace();
                }
            }

            ts = System.currentTimeMillis();
        }

        private String formatFile( File file )
        {
            return file.getAbsolutePath().substring( context.getRepository().getAbsolutePath().length() + 1 );
        }

        public void scanningFinished( IndexingContext context, ScanningResult result )
        {
            if ( !quiet )
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

}
