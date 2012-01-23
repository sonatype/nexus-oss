/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.tools.metadata;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.tools.cli.AbstractCli;

/**
 * Command Line Tool for rebuilding maven-metadata.xml files.
 * 
 * @author Juven Xu
 */
public class MetadataRebuildCli
    extends AbstractCli
{

    // ----------------------------------------------------------------------------
    // Options
    // ----------------------------------------------------------------------------
    public static final char REPO = 'r';

    public static void main( String[] args )
        throws Exception
    {
        new MetadataRebuildCli().execute( args );
    }

    @Override
    @SuppressWarnings( "static-access" )
    public Options buildCliOptions( Options options )
    {
        options.addOption( OptionBuilder.withLongOpt( "repository" ).hasArg().withDescription(
            "The repository where maven-metadata.xml will be rebuilt" ).isRequired().create( REPO ) );

        return options;
    }

    @Override
    public void invokePlexusComponent( CommandLine cli, PlexusContainer container )
        throws Exception
    {
        if ( cli.hasOption( REPO ) )
        {
            rebuildMetadata( cli, container );
        }
        else
        {
            displayHelp();
        }
    }

    private void rebuildMetadata( CommandLine cli, PlexusContainer plexus )
        throws IOException
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
            System.err.printf( "Invalid options. Repository '" + repository.getAbsolutePath()
                + "' (the repository directory) must exists and be readable!" );

            return;
        }

        try
        {
            MetadataRebuilder metadataRebuilder = (MetadataRebuilder) plexus.lookup( MetadataRebuilder.class );

            metadataRebuilder.rebuildMetadata( repository.getAbsolutePath() + "/" );
        }
        catch ( Exception e )
        {
            System.out.println( "Metadata rebuild failed! " );

            e.printStackTrace( System.err );
            
            return;
        }

        System.out.println( "Metadata rebuild is successful!" );
    }
    
}
