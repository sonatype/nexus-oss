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

package org.sonatype.nexus.tools.repository;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.tools.cli.AbstractCli;

/**
 * @author Juven Xu
 */
public class RepositoryConvertorCli
    extends AbstractCli
{
    // ----------------------------------------------------------------------------
    // Options
    // ----------------------------------------------------------------------------
    public static final char REPO = 'r';

    public static final char OUTPUT = 'o';

    public static final char TYPE = 't';
    
    public static void main( String[] args )
        throws Exception
    {
        new RepositoryConvertorCli().execute( args );
    }

    @Override
    @SuppressWarnings( "static-access" )
    public Options buildCliOptions( Options options )
    {
        options.addOption( OptionBuilder.withLongOpt( "repository" ).hasArg().withDescription(
            "The repository to be converted." ).create( REPO ) );

        options.addOption( OptionBuilder.withLongOpt( "output" ).hasArg().withDescription(
            "where the converted repositoris locate." ).create( OUTPUT ) );

        options.addOption( OptionBuilder.withLongOpt( "type" ).hasArg().withDescription(
            "Type of the convertion, copy or move." ).create( TYPE ) );

        return options;
    }

    @Override
    public void invokePlexusComponent( CommandLine cli, PlexusContainer container )
        throws Exception
    {
        if ( cli.hasOption( HELP ))
        {
            displayHelp();
            
            return;
        }
        
        if ( cli.hasOption( REPO ) && cli.hasOption( OUTPUT ) )
        {
            convert( cli, container );
        }
        else
        {
            showError( "Missing options -r or -o", null, false );
            
            displayHelp();
        }

    }

    private void convert( CommandLine cli, PlexusContainer plexus )
        throws IOException,
            ComponentLookupException
    {
        if ( cli.hasOption( DEBUG ) )
        {
            plexus.getLoggerManager().setThresholds( Logger.LEVEL_DEBUG );
        }

        File repository = new File( cli.getOptionValue( REPO ) );

        if ( !repository.exists() || !repository.canRead() || !repository.isDirectory() )
        {
            System.err.printf( "Invalid options. Repository '" + repository.getCanonicalPath()
                + "' (the repository directory) must exists and be readable!" );

            return;
        }

        File output = new File( cli.getOptionValue( OUTPUT ) );

        if ( !output.exists() || !output.canWrite() || !output.isDirectory() )
        {
            System.err.printf( "Invalid options. Output '" + output.getCanonicalPath()
                + "' (where the output repositories locate) must exists and be writale!" );

            return;
        }

        RepositoryConvertor repositoryConvertor = (RepositoryConvertor) plexus.lookup( RepositoryConvertor.class );

        try
        {
            if ( cli.hasOption( TYPE ) && cli.getOptionValue( TYPE ).toLowerCase().equals( "move" ) )
            {
                repositoryConvertor.convertRepositoryWithMove( repository, output );
            }
            else
            {
                repositoryConvertor.convertRepositoryWithCopy( repository, output );
            }
        }
        catch ( IOException ioe )
        {
            showError( "Repository conversion failed!", ioe, true );
        }
        
        System.out.println( "Repository conversion is successful!" );
    }

}
