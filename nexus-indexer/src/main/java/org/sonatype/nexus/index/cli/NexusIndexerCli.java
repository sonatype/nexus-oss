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
package org.sonatype.nexus.index.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.tools.cli.AbstractCli;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactScanningListener;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.IndexCreator;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.scan.ScanningResult;

public class NexusIndexerCli
    extends AbstractCli
{
    // ----------------------------------------------------------------------------
    // Options
    // ----------------------------------------------------------------------------

    public static final char REPO = 'r';

    public static final char INDEX = 'i';

    public static final char NAME = 'n';

    public static final char TYPE = 't';

    public static final char ZIP = 'z';

    public static final char UPDATE = 'u';

    public static final char OVERWRITE = 'o';

    // ----------------------------------------------------------------------------
    // Properties controlling Repository conversion
    // ----------------------------------------------------------------------------

    public static void main( String[] args )
        throws Exception
    {
        new NexusIndexerCli().execute( args );
    }

    @Override
    public String getPomPropertiesPath()
    {
        return "META-INF/maven/org.sonatype.nexus/nexus-core/pom.properties";
    }

    @Override
    @SuppressWarnings( "static-access" )
    public Options buildCliOptions( Options options )
    {
        options.addOption( OptionBuilder
            .withLongOpt( "index" ).hasArg().withDescription( "Path to the index folder." ).create( INDEX ) );

        options.addOption( OptionBuilder.withLongOpt( "repository" ).hasArg().withDescription(
            "Path to the Maven repository." ).create( REPO ) );

        options.addOption( OptionBuilder.withLongOpt( "name" ).hasArg().withDescription( "Repository name." ).create(
            NAME ) );

        options.addOption( OptionBuilder.withLongOpt( "type" ).hasArg().withDescription(
            "Index type (default, min or full)." ).create( TYPE ) );

        options.addOption( OptionBuilder.withLongOpt( "zip" ).withDescription( "Create index archive." ).create( ZIP ) );

        options.addOption( OptionBuilder
            .withLongOpt( "overwrite" ).withDescription( "Overwrite existing index." ).create( OVERWRITE ) );

        options.addOption( OptionBuilder.withLongOpt( "update" ).withDescription( "Create update." ).create( UPDATE ) );

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
        if ( cli.hasOption( OVERWRITE ) && cli.hasOption( UPDATE ) )
        {
            System.err.printf( "Invalid options. Should use either 'overwrite' or 'update' but not both" );

            return;
        }

        String indexDirectoryName = cli.getOptionValue( INDEX );

        File indexDirectory = new File( indexDirectoryName );

        if ( indexDirectory.exists() )
        {
            if ( cli.hasOption( OVERWRITE ) )
            {
                FileUtils.deleteDirectory( indexDirectory );
            }
            else if ( !cli.hasOption( UPDATE ) )
            {
                System.err.printf(
                    "Index folder '%s' already exists. Use 'overwrite' or 'update' option\n",
                    indexDirectoryName );

                return;
            }
        }

        List<? extends IndexCreator> indexers = NexusIndexer.DEFAULT_INDEX;

        if ( cli.hasOption( TYPE ) )
        {
            String type = cli.getOptionValue( TYPE );

            if ( "min".equals( type ) )
            {
                indexers = NexusIndexer.MINIMAL_INDEX;
            }
            else if ( "full".equals( type ) )
            {
                indexers = NexusIndexer.FULL_INDEX;
            }
        }

        String repositoryName = cli.hasOption( NAME ) ? cli.getOptionValue( NAME ) : indexDirectory.getName();

        File repository = new File( cli.getOptionValue( REPO ) );

        NexusIndexer indexer = (NexusIndexer) plexus.lookup( NexusIndexer.class );

        IndexPacker packer = (IndexPacker) plexus.lookup( IndexPacker.class );

        IndexingContext indexingContext = indexer.addIndexingContext( //
            repositoryName, // context id
            repositoryName, // repository id
            repository, // repository folder
            indexDirectory, // index folder
            null, // repositoryUrl
            null, // index update url
            indexers,
            false);

        boolean createZip = cli.hasOption( ZIP );

        boolean debug = cli.hasOption( DEBUG );

        boolean update = cli.hasOption( UPDATE );

        ArtifactScanningListener listener = new IndexerListener( indexingContext, packer, createZip, debug, update );

        indexer.scan( indexingContext, listener, update );
    }

    // Listener

    private static final class IndexerListener
        implements ArtifactScanningListener
    {
        private static final long MB = 1024 * 1024;

        private final IndexingContext context;

        private final IndexPacker packer;

        private final boolean createZip;

        private final boolean debug;

        private final boolean update;

        private long tstart;

        private long ts = System.currentTimeMillis();

        private int count;

        IndexerListener( IndexingContext context, IndexPacker packer, boolean createZip, boolean debug, boolean update )
        {
            this.context = context;
            this.packer = packer;
            this.createZip = createZip;
            this.debug = debug;
            this.update = update;
        }

        public void scanningStarted( IndexingContext context )
        {
            System.err.println( "Scanning started" );
            tstart = System.currentTimeMillis();
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

            if ( update )
            {
                System.err.printf( "  %6d %s\n", count, formatFile( ac.getPom() ) );
            }
            else if ( ( t - ts ) > 2000L )
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
                System.err.printf( "Total scanning errors: %s\n", result.getExceptions().size() );
            }

            System.err.printf( "Total files scanned: %s\n", result.getTotalFiles() );

            long t = System.currentTimeMillis() - tstart;

            long s = t / 1000L;

            if ( t > 60 * 1000 )
            {
                long m = t / 1000L / 60L;

                System.err.printf( "Total time: %d min %d sec\n", m, s - ( m * 60 ) );
            }
            else
            {
                System.err.printf( "Total time: %d sec\n", s );
            }

            Runtime r = Runtime.getRuntime();

            System.err.printf( "Final memory: %dM/%dM\n", ( r.totalMemory() - r.freeMemory() ) / MB, r.totalMemory()
                / MB );

            if ( createZip )
            {
                try
                {
                    packer.packIndex( context, new File( "." ) );
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
        }
    }
}
