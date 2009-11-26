package org.sonatype.appcontext;

import java.util.ArrayList;
import java.util.List;

public class DefaultAppContextRequest
    implements AppContextRequest
{
    private String name = "plexus";

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

    public List<ContextFiller> getContextFillers()
    {
        if ( contextFillers == null )
        {
            contextFillers = new ArrayList<ContextFiller>( 3 );

            // the order is important! 1st env variables
            contextFillers.add( new SystemEnvironmentContextFiller() );

            // the order is important! 2nd sysprops (to override env vars)
            contextFillers.add( new SystemPropertiesContextFiller() );
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

            // 2nd the terminal
            contextPublishers.add( new TerminalContextPublisher() );
        }

        return contextPublishers;
    }

    public void setContextPublishers( List<ContextPublisher> contextPublishers )
    {
        this.contextPublishers = contextPublishers;
    }

}
