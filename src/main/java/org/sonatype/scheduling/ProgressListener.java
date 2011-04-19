package org.sonatype.scheduling;

public interface ProgressListener
{
    /**
     * Marks that the amount of work (work-units) are not known in advance.
     */
    int UNKNOWN_WORKUNITS = -1;

    /**
     * Starts a new (sub)task with {@link #UNKNOWN_WORKUNITS} to be done.
     * 
     * @param name
     */
    public void beginTask( String name );

    /**
     * Starts a new (sub)task with {@code toDo} work-units to be done.
     * 
     * @param name
     * @param toDo
     */
    public void beginTask( String name, int toDo );

    /**
     * Marks work is underway without a message. It is left to {@link ProgressListener} implementor what will happen
     * with this information (like update a progress bar for example). This is NOT a setter! Work unit count sent in
     * here are accumulated (summed up).
     * 
     * @param message
     */
    public void working( int workDone );

    /**
     * Marks work is underway with a message. It is left to {@link ProgressListener} implementor what will happen with
     * this message, will it be shown in log, in UI or whatever.
     * 
     * @param message
     */
    public void working( String message );

    /**
     * Marks work is underway and {@code workDone} work-units as done. This is NOT a setter! Work unit count sent in
     * here are accumulated (summed up). It is left to {@link ProgressListener} implementor what will happen with this
     * message, will it be shown in log, in UI or whatever.
     * 
     * @param workDone
     */
    public void working( String message, int workDone );

    /**
     * Ends a (sub)task with a message.
     * 
     * @param message
     */
    public void endTask( String message );

    /**
     * Returns true if the task-run to which this progress monitor belongs to should be canceled.
     * 
     * @return
     */
    boolean isCanceled();

    /**
     * Cancels the task-run to which this progress monitor belongs to. This call will return immediately (will not block
     * to wait actual task cancellation).
     */
    void cancel();
}
