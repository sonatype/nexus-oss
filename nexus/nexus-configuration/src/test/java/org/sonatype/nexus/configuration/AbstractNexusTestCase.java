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
package org.sonatype.nexus.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.util.IOUtil;

/**
 * Abstract test case for nexus tests. It is customizing the context and helps with nexus configurations.
 * 
 * @author cstamas
 */
public abstract class AbstractNexusTestCase
    extends PlexusTestCase
{
    public static final String NEXUS_CONFIGURATION_KEY = "nexus.configuration";
    public static final String SECURITY_CONFIGURATION_KEY = "nexus.security.configuration";
    public static final String APPS_CONFIGURATION_KEY = "apps";

    protected static final File PLEXUS_HOME = new File( getBasedir(), "target/plexus-home" );

    protected void customizeContext( Context ctx )
    {
        File nexusConfigFile = new File( PLEXUS_HOME, "/conf/nexus.xml" );

        nexusConfigFile.getParentFile().mkdirs();
        
        File securityConfigFile = new File( PLEXUS_HOME, "/conf/security.xml" );

        ctx.put( NEXUS_CONFIGURATION_KEY, nexusConfigFile.getAbsolutePath() );
        ctx.put( SECURITY_CONFIGURATION_KEY, securityConfigFile.getAbsolutePath() );
        ctx.put( APPS_CONFIGURATION_KEY, PLEXUS_HOME.getAbsolutePath() );
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
    
    protected String getSecurityConfiguration()
    {
        try
        {
            return (String) getContainer().getContext().get( SECURITY_CONFIGURATION_KEY );
        }
        catch ( ContextException e )
        {
            fail( "JUNit environment problem: " + SECURITY_CONFIGURATION_KEY + " not found in plexus context?" );

            return null;
        }
    }

    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        IOUtil.copy( getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" ), new FileOutputStream(
            getNexusConfiguration() ) );
    }
    
    protected void copyDefaultSecurityConfigToPlace()
    throws IOException
    {
        IOUtil.copy( getClass().getResourceAsStream( "/META-INF/nexus/security.xml" ), new FileOutputStream(
            getSecurityConfiguration() ) );
    }

}
