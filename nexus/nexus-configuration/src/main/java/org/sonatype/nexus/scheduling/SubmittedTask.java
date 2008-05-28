package org.sonatype.nexus.scheduling;

public interface SubmittedTask
{
    TaskState getTaskState();
    
    boolean isDone();

    void cancel();
}
