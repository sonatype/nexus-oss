package org.sonatype.nexus.scheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;

public class DummyWaitingNexusTask
    implements SchedulerTask<Object>
{
    private boolean allowConcurrentSubmission = false;

    private boolean allowConcurrentExecution = false;

    private long sleepTime = 10000;

    private Map<String, String> parameters;
    
    public boolean isExposed()
    {
        return true;
    }

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

    public boolean allowConcurrentSubmission( Map<String, List<ScheduledTask<?>>> activeTasks )
    {
        return allowConcurrentSubmission;
    }

    public boolean allowConcurrentExecution( Map<String, List<ScheduledTask<?>>> activeTasks )
    {
        return allowConcurrentExecution;
    }

    public void setAllowConcurrentSubmission( boolean allowConcurrentSubmission )
    {
        this.allowConcurrentSubmission = allowConcurrentSubmission;
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
