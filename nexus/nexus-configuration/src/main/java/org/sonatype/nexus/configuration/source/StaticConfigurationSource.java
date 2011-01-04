/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.configuration.source;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.Configuration;

/**
 * A special "static" configuration source, that always return a factory provided defaults for Nexus configuration. It
 * is unmodifiable, since it actually reads the bundled config file from the module's JAR.
 * 
 * @author cstamas
 */
@Component( role = ApplicationConfigurationSource.class, hint = "static" )
public class StaticConfigurationSource
    extends AbstractApplicationConfigurationSource
{

    /**
     * Gets the configuration using getResourceAsStream from "/META-INF/nexus/nexus.xml".
     */
    public InputStream getConfigurationAsStream()
        throws IOException
    {
        InputStream result = getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" );

        if ( result != null )
        {
            return result;
        }
        else
        {
            getLogger().info( "No edition-specific configuration found, falling back to Core default configuration." );

            return getClass().getResourceAsStream( "/META-INF/nexus/default-oss-nexus.xml" );
        }
    }

    public Configuration loadConfiguration()
        throws ConfigurationException, IOException
    {
        loadConfiguration( getConfigurationAsStream() );

        return getConfiguration();
    }

    /**
     * This method will always throw UnsupportedOperationException, since NexusDefaultsConfigurationSource is read only.
     */
    public void storeConfiguration()
        throws IOException
    {
        throw new UnsupportedOperationException( "The NexusDefaultsConfigurationSource is static source!" );
    }

    /**
     * Static configuration has no default source, hence it cannot be defalted. Always returns false.
     */
    public boolean isConfigurationDefaulted()
    {
        return false;
    }

}
