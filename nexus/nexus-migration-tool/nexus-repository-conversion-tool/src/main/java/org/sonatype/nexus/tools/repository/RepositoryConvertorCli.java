/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.tools.repository;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.tools.cli.AbstractCli;

/**
 * @author Juven Xu
 */
public class RepositoryConvertorCli
    extends AbstractCli
{

    public static final String GROUP_ID = "org.sonatype.nexus.tools";

    public static final String ARTIFACT_ID = "nexus-repository-conversion-tool";

    // ----------------------------------------------------------------------------
    // Options
    // ----------------------------------------------------------------------------
    public static final char REPO = 'r';

    public static final char OUTPUT = 'o';

    public static final char MOVE = 'm';

    private Options options;
    
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
            "Where the converted repositories locate." ).create( OUTPUT ) );

        options.addOption( OptionBuilder.withLongOpt( "move" ).withDescription(
            "Move the repository (old repository will be deleted )." ).create( MOVE ) );

        this.options = options;
        
        return options;
    }

    @Override
    public void invokePlexusComponent( CommandLine cli, PlexusContainer container )
        throws Exception
    {
        if ( cli.hasOption( HELP ) )
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
            try
            {
                LoggerManager mgr = (LoggerManager) plexus.lookup( LoggerManager.class );
                mgr.setThresholds( Logger.LEVEL_DEBUG );
            }
            catch ( ComponentLookupException e )
            {
                // too bad we can't change log level
            }
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
            if ( cli.hasOption( MOVE ) )
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

    @Override
    public String getPomPropertiesPath()
    {
        return "META-INF/maven/" + GROUP_ID + "/" + ARTIFACT_ID + "/pom.properties";
    }
    
    public void displayHelp()
    {
        System.out.println();

        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp( "java -jar cli-jar-name [options]", "\nOptions:", options, "\n" );
    }

}
