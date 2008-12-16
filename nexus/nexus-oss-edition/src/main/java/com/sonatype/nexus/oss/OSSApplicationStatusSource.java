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
        systemStatus.setEdition( "OSS" );
        systemStatus.setAppName( "Sonatype Nexus Maven Repository Manager" );
        systemStatus.setFormattedAppName( "Sonatype&trade; Nexus&trade;" );
        
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
