package org.sonatype.nexus.scheduling;

public class DummyWaitingNexusRepositoriesTask
    extends AbstractNexusRepositoriesTask<Object>
{
    private long sleepTime = 10000;

    public long getSleepTime()
    {
        return sleepTime;
    }

    public void setSleepTime( long sleepTime )
    {
        this.sleepTime = sleepTime;
    }

    public Object doRun()
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
