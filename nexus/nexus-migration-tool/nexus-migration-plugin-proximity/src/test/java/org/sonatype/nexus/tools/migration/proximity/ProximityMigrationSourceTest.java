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
package org.sonatype.nexus.tools.migration.proximity;

import java.io.File;
import java.io.PrintWriter;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.configuration.model.CHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRestApiSettings;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSecurity;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.tools.migration.LoggingMigrationMonitor;
import org.sonatype.nexus.tools.migration.MigrationRequest;
import org.sonatype.nexus.tools.migration.MigrationResult;
import org.sonatype.nexus.tools.migration.MigrationSource;

public class ProximityMigrationSourceTest
    extends PlexusTestCase
{
    public static final String NEXUS_HOME_KEY = "nexus.home";

    public static final String NEXUS_CONFIGURATION_KEY = "nexus.configuration";

    protected static final File PLEXUS_HOME = new File( getBasedir(), "/target/plexus-home" );

    protected void customizeContext( Context ctx )
    {
        ctx.put( NEXUS_HOME_KEY, PLEXUS_HOME.getAbsolutePath() );
        ctx.put( NEXUS_CONFIGURATION_KEY, new File( PLEXUS_HOME, "/conf/nexus.xml" ).getAbsolutePath() );
    }

    protected String getNexusHome()
    {
        try
        {
            return (String) getContainer().getContext().get( NEXUS_HOME_KEY );
        }
        catch ( ContextException e )
        {
            fail( "JUNit environment problem: " + NEXUS_CONFIGURATION_KEY + " not found in plexus context?" );

            return null;
        }
    }

    protected String getNexusConfiguration()
    {
        try
        {
            return (String) getContainer().getContext().get( NEXUS_CONFIGURATION_KEY );
        }
        catch ( ContextException e )
        {
            fail( "JUNit environment problem: " + NEXUS_CONFIGURATION_KEY + " not found in plexus context?" );

            return null;
        }
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        FileUtils.deleteDirectory( PLEXUS_HOME );
    }

    public Logger getLogger()
    {
        return new ConsoleLogger( Logger.LEVEL_DEBUG, "MigrationTool" );
    }

    protected Configuration getDefaultConfiguration()
    {
        Configuration conf = new Configuration();
        conf.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        conf.setGlobalHttpProxySettings( new CRemoteHttpProxySettings() );
        conf.setHttpProxy( new CHttpProxySettings() );
        conf.setRestApi( new CRestApiSettings() );
        conf.setRouting( new CRouting() );
        conf.setSecurity( new CSecurity() );
        return conf;
    }

    public void testRC9()
        throws Exception
    {
        ProximityMigrationSource pms = (ProximityMigrationSource) lookup( MigrationSource.ROLE, "proximity" );

        MigrationRequest req = new MigrationRequest( "proximity", new File(
            getBasedir(),
            "src/test/resources/proximity-rc9-webapp/WEB-INF/web.xml" ) );
        
        MigrationResult res = new MigrationResult( getDefaultConfiguration() );
        
        pms.migrateConfiguration( req, res, new LoggingMigrationMonitor( getLogger() ) );

        for ( Exception e : res.getExceptions() )
        {
            e.printStackTrace();
        }
        assertTrue( res.isSuccesful() );

        NexusConfigurationXpp3Writer w = new NexusConfigurationXpp3Writer();
        w.write( new PrintWriter( System.out ), res.getConfiguration() );

    }

}
