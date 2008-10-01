package org.sonatype.scheduling;

/**
 * Exception used to wrap-up the non-exceptions (errors like OOM) occured during runtime of tasks into an Exception.
 * 
 * @author cstamas
 */
public class TaskExecutionException
    extends Exception
{
    public TaskExecutionException( String message )
    {
        super( message );
    }

    public TaskExecutionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public TaskExecutionException( Throwable cause )
    {
        super( cause );
    }
}
