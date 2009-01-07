/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.sonatype.nexus.oss;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.SystemStatus;

@Component( role = ApplicationStatusSource.class )
public class OSSApplicationStatusSource
    extends AbstractLogEnabled
    implements ApplicationStatusSource
{
    /**
     * System status.
     */
    private SystemStatus systemStatus = new SystemStatus();

    public OSSApplicationStatusSource()
    {
        systemStatus.setEdition( "Open Source" );
        systemStatus.setAppName( "Sonatype Nexus Maven Repository Manager" );
        systemStatus.setFormattedAppName( "Sonatype Nexus&trade;" );
        
        try
        {
            Properties props = new Properties();

            InputStream is = getClass().getResourceAsStream(
                "/META-INF/maven/org.sonatype.nexus/nexus-oss-edition/pom.properties" );

            if ( is != null )
            {
                props.load( is );
            }

            systemStatus.setVersion( props.getProperty( "version" ) );
        }
        catch ( IOException e )
        {
            getLogger().warn(
                "Could not load/read Nexus version from /META-INF/maven/org.sonatype.nexus/nexus-oss-edition/pom.properties",
                e );

            systemStatus.setVersion( "unknown" );
        }
    }

    public SystemStatus getSystemStatus()
    {
        return systemStatus;
    }

    public boolean setState( SystemState state )
    {
        systemStatus.setState( state );

        return true;
    }
}
