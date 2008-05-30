package org.sonatype.scheduling;

import java.util.Date;
import java.util.concurrent.ExecutionException;

public interface SubmittedTask<T>
{
    /**
     * Returns a unique ID of the task.
     * 
     * @return
     */
    String getId();

    /**
     * Returns the "type" of the task.
     * 
     * @return
     */
    String getType();

    /**
     * Returns the task state.
     * 
     * @return
     */
    TaskState getTaskState();

    /**
     * Returns the date when the task is scheduled.
     * 
     * @return
     */
    Date getScheduledAt();

    /**
     * Cancels the task and removes it from queue.
     */
    void cancel();

    /**
     * Returns an exception is TaskState is BROKEN, null in any other case.
     * 
     * @return null, if task in not in BROKEN status, otherwise the exception that broke it.
     */
    Exception getBrokenCause();

    /**
     * Gets the result of Callable, or null if it is "converted" from Runnable. It behaves just like Future.get(), if
     * the task is not finished, it will block.
     * 
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    T get()
        throws ExecutionException,
            InterruptedException;

    /**
     * Gets the result of Callable, or null if it is "converted" from Runnable.
     * 
     * @return
     */
    T getIfDone();

}
