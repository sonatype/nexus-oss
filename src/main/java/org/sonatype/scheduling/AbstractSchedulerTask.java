package org.sonatype.scheduling;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;

public abstract class AbstractSchedulerTask<T>
    implements SchedulerTask<T>
{
    @Requirement
    protected Logger logger;

    private Map<String, String> parameters;

    public void addParameter( String key, String value )
    {
        getParameters().put( key, value );
    }

    public String getParameter( String key )
    {
        return getParameters().get( key );
    }

    public synchronized Map<String, String> getParameters()
    {
        if ( parameters == null )
        {
            parameters = new HashMap<String, String>();
        }

        return parameters;
    }

    public abstract T call()
        throws Exception;

    // ==

    protected Logger getLogger()
    {
        return logger;
    }

    protected void checkInterruption()
        throws TaskInterruptedException
    {
        TaskUtil.checkInterruption();
    }
}
