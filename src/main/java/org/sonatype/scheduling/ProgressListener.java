package org.sonatype.scheduling;

public interface ProgressListener
{
    public void beginTask( String name, int toDo );

    public void working( int work );

    public void endTask( String message );

    boolean isCancelled();
}
