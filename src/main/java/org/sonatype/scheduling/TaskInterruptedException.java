package org.sonatype.scheduling;

/**
 * Runtime exception thrown in cases when thread is interrupted. Semantical meaning is almost same as
 * {@link InterruptedException} except this one is unchecked exception.
 * 
 * @author cstamas
 */
public class TaskInterruptedException
    extends RuntimeException
{
    private static final long serialVersionUID = 5758132070000732555L;

    private final boolean cancelled;

    public TaskInterruptedException( String message, boolean cancelled )
    {
        super( message );

        this.cancelled = cancelled;
    }

    public TaskInterruptedException( String message, Throwable cause )
    {
        super( message, cause );

        this.cancelled = false;
    }

    public TaskInterruptedException( Throwable cause )
    {
        super( cause );

        this.cancelled = false;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }
}
