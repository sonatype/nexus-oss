package org.sonatype.scheduling;

public interface ProgressListener
{
    int UNKNOWN = -1;

    public void beginTask( String name, int toDo );

    public void working( int workDone );

    public void working( String message, int workDone );

    public void endTask( String message );

    boolean isCancelled();
    
    void cancel();
}
