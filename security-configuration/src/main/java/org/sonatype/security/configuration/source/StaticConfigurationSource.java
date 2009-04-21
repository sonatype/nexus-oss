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
package org.sonatype.security.configuration.source;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.security.configuration.ConfigurationException;

/**
 * A special "static" configuration source, that always return a factory provided defaults for Security configuration. It
 * is unmodifiable, since it actually reads the bundled config file from the module's JAR.
 * 
 * @author cstamas
 */
@Component( role = SecurityConfigurationSource.class, hint = "static" )
public class StaticConfigurationSource
    extends AbstractSecurityConfigurationSource
{
    
    private static final String STATIC_SECURITY_RESOURCE = "/META-INF/security/security.xml";
    
    @Requirement
    private Logger logger;
    
    /**
     * Gets the configuration using getResourceAsStream from "/META-INF/security/security.xml".
     */
    public InputStream getConfigurationAsStream()
        throws IOException
    {
        return getClass().getResourceAsStream( STATIC_SECURITY_RESOURCE );
    }

    public Configuration loadConfiguration()
        throws ConfigurationException,
            IOException
    {
        if( getClass().getResource( STATIC_SECURITY_RESOURCE ) != null )
        {
            loadConfiguration( getConfigurationAsStream() );
        }
        else
        {
            this.logger.warn( "Default static security configuration not found in classpath: "+ STATIC_SECURITY_RESOURCE );
        }
        
        Configuration configuration = getConfiguration();

        return configuration;
    }

    /**
     * This method will always throw UnsupportedOperationException, since NexusDefaultsConfigurationSource is read only.
     */
    public void storeConfiguration()
        throws IOException
    {
        throw new UnsupportedOperationException( "The SecurityDefaultsConfigurationSource is static source!" );
    }

    /**
     * Static configuration has no default source, hence it cannot be defalted. Always returns false.
     */
    public boolean isConfigurationDefaulted()
    {
        return false;
    }

}
