package org.sonatype.nexus.plugins.tasks.api;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;


@Component( role = SchedulerTask.class, hint = "SleepRepositoryTask", instantiationStrategy = "per-lookup" )
public class SleepRepositoryTask
    extends AbstractNexusRepositoriesTask<Object>
{

    @Override
    protected Object doRun()
        throws Exception
    {
        getLogger().debug( getMessage() );

        final int time = getTime();
        Thread.sleep( time * 1000 / 2 );
        getRepositoryRegistry().getRepository( getRepositoryId() );
        Thread.sleep( time * 1000 / 2 );
        return null;
    }

    private int getTime()
    {
        String t = getParameter( "time" );

        if ( StringUtils.isEmpty( t ) )
        {
            return 5;
        }
        else
        {
            return new Integer( t );
        }
    }

    @Override
    protected String getAction()
    {
        return "Sleeping";
    }

    @Override
    protected String getMessage()
    {
        return "Sleeping for " + getTime() + " seconds!";
    }

}
