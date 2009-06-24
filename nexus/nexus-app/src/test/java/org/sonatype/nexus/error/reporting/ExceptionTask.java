package org.sonatype.nexus.error.reporting;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.SchedulerTask;

@Component( role = SchedulerTask.class, hint = "ExceptionTask" )
public class ExceptionTask
    extends AbstractNexusTask<Object>
{
    @Override
    protected Object doRun()
        throws Exception
    {
        throw new RuntimeException( "Runtime exception" );
    }
    
    @Override
    protected String getAction()
    {
        return "Exception Task";
    }
    
    @Override
    protected String getMessage()
    {
        return "Exception Task";
    }
}