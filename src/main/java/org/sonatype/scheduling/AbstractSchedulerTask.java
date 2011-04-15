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

    private ProgressListenerWrapper progressListener = ProgressListenerWrapper.DEVNULL;

    public ProgressListener getProgressListener()
    {
        return progressListener;
    }

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

    public final T call()
        throws Exception
    {
        // set the listener to make access possible from legacy code
        TaskUtil.setCurrent( progressListener );

        return doCall();
    }

    // ==

    protected abstract T doCall()
        throws Exception;

    protected Logger getLogger()
    {
        return logger;
    }

    protected void checkInterruption()
        throws TaskInterruptedException
    {
        TaskUtil.checkInterruption();
    }

    protected void setProgressListener( final ProgressListener progressListener )
    {
        if ( progressListener == null )
        {
            this.progressListener = ProgressListenerWrapper.DEVNULL;
        }
        else
        {
            this.progressListener = new ProgressListenerWrapper( progressListener );
        }
    }
}
