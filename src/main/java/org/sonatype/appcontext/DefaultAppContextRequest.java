package org.sonatype.appcontext;

import java.util.ArrayList;
import java.util.List;

public class DefaultAppContextRequest
    implements AppContextRequest
{
    private String name = "plexus";

    private BasedirDiscoverer basedirDiscoverer;

    private List<ContextFiller> contextFillers;

    private List<ContextPublisher> contextPublishers;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public BasedirDiscoverer getBasedirDiscoverer()
    {
        if ( basedirDiscoverer == null )
        {
            basedirDiscoverer = new DefaultBasedirDiscoverer();
        }

        return basedirDiscoverer;
    }

    public void setBasedirDiscoverer( BasedirDiscoverer basedirDiscoverer )
    {
        this.basedirDiscoverer = basedirDiscoverer;
    }

    public List<ContextFiller> getContextFillers()
    {
        if ( contextFillers == null )
        {
            contextFillers = new ArrayList<ContextFiller>( 3 );

            // the order is important! 1st env variables
            contextFillers.add( new SystemEnvironmentContextFiller() );

            // the order is important! 2nd sysprops (to override env vars)
            contextFillers.add( new SystemPropertiesContextFiller() );

            // and last, the basedir
            contextFillers.add( new BasedirContextFiller() );
        }

        return contextFillers;
    }

    public void setContextFillers( List<ContextFiller> contextFillers )
    {
        this.contextFillers = contextFillers;
    }

    public List<ContextPublisher> getContextPublishers()
    {
        if ( contextPublishers == null )
        {
            contextPublishers = new ArrayList<ContextPublisher>( 2 );

            // the order is important! 1st system properties
            contextPublishers.add( new SystemPropertiesContextPublisher() );

            // 2nd the logger (since Slf4j is the standard for Sonatype apps now)
            contextPublishers.add( new Slf4jLoggerContextPublisher() );
        }

        return contextPublishers;
    }

    public void setContextPublishers( List<ContextPublisher> contextPublishers )
    {
        this.contextPublishers = contextPublishers;
    }

}
