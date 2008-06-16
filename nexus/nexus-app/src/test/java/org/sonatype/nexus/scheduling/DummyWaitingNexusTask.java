package org.sonatype.nexus.scheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.scheduling.ScheduledTask;

public class DummyWaitingNexusTask
    implements NexusTask<Object>
{
    private boolean allowConcurrentExecution = false;

    private long sleepTime = 10000;

    private Map<String, String> parameters;

    public void addParameter( String key, String value )
    {
        getParameters().put( key, value );
    }

    public String getParameter( String key )
    {
        return getParameters().get( key );
    }

    public Map<String, String> getParameters()
    {
        if ( parameters == null )
        {
            parameters = new HashMap<String, String>();
        }

        return parameters;
    }

    public boolean allowConcurrentExecution( List<ScheduledTask<?>> existingTasks )
    {
        return allowConcurrentExecution;
    }

    public void setAllowConcurrentExecution( boolean allowConcurrentExecution )
    {
        this.allowConcurrentExecution = allowConcurrentExecution;
    }

    public long getSleepTime()
    {
        return sleepTime;
    }

    public void setSleepTime( long sleepTime )
    {
        this.sleepTime = sleepTime;
    }

    public Object call()
        throws Exception
    {
        System.out.println( "BEFORE SLEEP" );
        Thread.sleep( getSleepTime() );
        System.out.println( "AFTER SLEEP" );

        return null;
    }

    protected String getAction()
    {
        return "DUMMY";
    }

    protected String getMessage()
    {
        return "A Dummy task, waits for some time";
    }

}
