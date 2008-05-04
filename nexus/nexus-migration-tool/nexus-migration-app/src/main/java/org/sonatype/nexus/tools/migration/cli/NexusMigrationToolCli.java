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
package org.sonatype.nexus.tools.migration.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.tools.cli.AbstractCli;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.tools.migration.MigrationRequest;
import org.sonatype.nexus.tools.migration.MigrationResult;
import org.sonatype.nexus.tools.migration.MigrationTool;

public class NexusMigrationToolCli
    extends AbstractCli
{
    // ----------------------------------------------------------------------------
    // Options
    // ----------------------------------------------------------------------------

    public static final char SRC = 's';

    public static final char PATH = 'p';

    public static final char OUTPUT = 'o';

    // ----------------------------------------------------------------------------
    // Properties controlling Repository conversion
    // ----------------------------------------------------------------------------

    public static void main( String[] args )
        throws Exception
    {
        new NexusMigrationToolCli().execute( args );
    }

    @Override
    @SuppressWarnings( "static-access" )
    public Options buildCliOptions( Options options )
    {
        options.addOption( OptionBuilder.withLongOpt( "source" ).hasArg().withDescription(
            "Migration source type (one of the 'archiva', 'artifactory', 'proximity')." ).isRequired( true ).create( SRC ) );

        options.addOption( OptionBuilder
            .withLongOpt( "path" ).hasArg().withDescription(
                "Path to the source configuration. For Proximity, the path to web.xml of the webapp." ).isRequired(
                true ).create( PATH ) );

        options.addOption( OptionBuilder
            .withLongOpt( "output" ).hasArg().withDescription(
                "Where to write the Nexus configuration file (defaults to nexus.xml in current dir)" ).isRequired(
                false ).create( OUTPUT ) );

        return options;
    }

    @Override
    public void invokePlexusComponent( final CommandLine cli, PlexusContainer plexus )
        throws Exception
    {
        if ( cli.hasOption( SRC ) && cli.hasOption( PATH ) )
        {
            migrate( cli, plexus );
        }
        else
        {
            displayHelp();
        }
    }

    private void migrate( final CommandLine cli, PlexusContainer plexus )
        throws ComponentLookupException,
            IOException

    {
        if (cli.hasOption( DEBUG )) 
        {
            plexus.getLoggerManager().setThresholds( Logger.LEVEL_DEBUG );
        }
        
        File srcPath = new File( cli.getOptionValue( PATH ) );

        if ( !srcPath.exists() || !srcPath.canRead() )
        {
            System.err.printf( "Invalid options. Path '" + srcPath.getCanonicalPath()
                + "' (the file or dir) must exists and be readable!" );

            return;
        }
       
        MigrationRequest req = new MigrationRequest( cli.getOptionValue( SRC ), srcPath );

        MigrationTool mt = (MigrationTool) plexus.lookup( MigrationTool.ROLE );

        MigrationResult res = mt.migrate( req, null );

        if ( res.isSuccesful() )
        {
            System.out.println( "Migration is succesful." );
        }
        else
        {
            System.out.println( "Migration failed." );

            for ( Exception e : res.getExceptions() )
            {
                e.printStackTrace();
            }
        }

        File output = null;

        if ( cli.hasOption( OUTPUT ) )
        {
            output = new File( cli.getOptionValue( OUTPUT ) );

            if ( !output.getParentFile().exists() )
            {
                output.getParentFile().mkdirs();
            }
        }
        else
        {
            output = new File( "nexus.xml" );
        }

        NexusConfigurationXpp3Writer w = new NexusConfigurationXpp3Writer();

        w.write( new PrintWriter( output ), res.getConfiguration() );
    }
}
