package org.sonatype.scheduling;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSchedulerTask<T>
    implements SchedulerTask<T>
{
    protected Logger logger = LoggerFactory.getLogger(getClass());

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
