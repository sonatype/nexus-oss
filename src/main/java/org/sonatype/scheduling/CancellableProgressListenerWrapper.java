package org.sonatype.scheduling;

public class CancellableProgressListenerWrapper
    extends ProgressListenerWrapper
{
    private volatile boolean cancelled;

    public CancellableProgressListenerWrapper( final ProgressListener wrapped )
    {
        super( wrapped );
    }

    public boolean isCanceled()
    {
        return cancelled || super.isCanceled();
    }

    public void cancel()
    {
        super.cancel();

        cancelled = true;
    }
}
