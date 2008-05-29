package org.sonatype.scheduling;

import java.util.Date;

public interface SubmittedTask
{
    String getType();

    TaskState getTaskState();

    Date getScheduledAt();

    boolean isDone();

    void cancel();
}
