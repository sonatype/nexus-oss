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
package org.sonatype.nexus.security;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.util.StringDigester;

/**
 * The Class SimpleAuthenticationSource that uses simple props file for storing username pwds.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.security.AuthenticationSource" instantiation-strategy="per-lookup" role-hint="simple"
 */
public class SimpleAuthenticationSource
    extends PropertiesFileBasedAuthenticationSource
    implements MutableAuthenticationSource
{
    public static final String ADMIN_USERNAME = "admin";

    public static final String DEPLOYMENT_USERNAME = "deployment";

    public void initialize()
        throws InitializationException
    {
        super.initialize();

        try
        {
            // check for old nexus pwd
            File oldSecretFile = new File( applicationConfiguration.getConfigurationDirectory(), "secret.txt" );

            if ( oldSecretFile.exists() )
            {
                getLogger().info( "Found pre-beta3 secret.txt file with 'admin' password, trying to upgrade it." );

                String oldAdminHash = FileUtils.fileRead( oldSecretFile ).trim();

                loadSecrets();

                secrets.put( ADMIN_USERNAME, oldAdminHash );

                saveSecrets();

                oldSecretFile.delete();
            }

            loadSecrets();
        }
        catch ( IOException e )
        {
            throw new InitializationException( "Error loading secret.properties!", e );
        }
    }

    protected void loadSecrets()
        throws IOException
    {
        synchronized ( this )
        {
            super.loadSecrets();

            if ( secrets.isEmpty() )
            {
                secrets.put( ADMIN_USERNAME, StringDigester.getSha1Digest( "admin123" ) );

                secrets.put( DEPLOYMENT_USERNAME, "" );
            }

            saveSecrets();
        }
    }

}
