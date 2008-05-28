package org.sonatype.scheduling;

import java.util.Date;

public interface SubmittedTask
{
    Class<?> getType();

    TaskState getTaskState();

    Date getScheduledAt();

    boolean isDone();

    void cancel();
}
