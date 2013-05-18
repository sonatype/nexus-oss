/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 * 
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.mac.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.index.context.IndexingContext;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.tools.cli.AbstractCli;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.plugins.mac.MacPlugin;
import org.sonatype.nexus.plugins.mac.MacRequest;

public class NexusArchetypeCli
    extends AbstractCli
{
    public static final String GROUP_ID = "org.sonatype.nexus.plugins";

    public static final String ARTIFACT_ID = "nexus-archetype-common";

    private Options options;

    public static void main( String[] args )
        throws Exception
    {
        new NexusArchetypeCli().execute( args );
    }

    @Override
    public Options buildCliOptions( Options options )
    {
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

        String[] args = cli.getArgs();

        if ( args == null || args.length != 1 )
        {
            System.err.println( "Please specify the repository location." );

            displayHelp();

            return;
        }

        File repo = new File( args[0] );

        if ( !repo.exists() || !repo.canRead() || !repo.canWrite() )
        {
            System.err.println( "Repository '" + repo.getAbsolutePath() + "' must be readable and writable!" );

            return;
        }

        CliIndexInitializer initializer = container.lookup( CliIndexInitializer.class );

        MacPlugin macPlugin = container.lookup( MacPlugin.class );

        File indexDir = new File( repo, ".index" );
        File tempDir = new File( repo, ".tmp" );

        if ( !indexDir.exists() )
        {
            System.err.println( "Repository '" + repo.getAbsolutePath() + "' does not have an index" );

            return;
        }

        if ( !tempDir.exists() )
        {
            tempDir.mkdir();
        }

        System.out.println( "Initialzing repository index ..." );

        IndexingContext ctx = initializer.initializeIndex( indexDir, tempDir );

        System.out.println( "Generating archetype catalog ..." );

        ArchetypeCatalog catalog = macPlugin.listArcherypesAsCatalog( new MacRequest( "tmp" ), ctx );

        // serialize it to XML
        ArchetypeCatalogXpp3Writer cw = new ArchetypeCatalogXpp3Writer();

        StringWriter sw = new StringWriter();

        cw.write( sw, catalog );

        File catalogFile = new File( repo, "archetype-catalog.xml" );

        FileWriter writer = new FileWriter( catalogFile );

        try
        {
            writer.write( sw.toString() );
        }
        finally
        {
            writer.close();
        }

        System.out.println( "Deleting temporary files ..." );

        FileUtils.deleteDirectory( tempDir );

        System.out.println( "Archetype Catalog '" + catalogFile.getAbsolutePath() + "' is generated." );
    }

    public void displayHelp()
    {
        System.out.println();

        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp( "java -jar cli.jar repo-location [options]", "\nOptions:", options, "\n" );
    }

    @Override
    public String getPomPropertiesPath()
    {
        return "META-INF/maven/" + GROUP_ID + "/" + ARTIFACT_ID + "/pom.properties";
    }
}
