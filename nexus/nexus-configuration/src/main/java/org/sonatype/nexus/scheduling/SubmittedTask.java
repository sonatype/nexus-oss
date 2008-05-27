package org.sonatype.nexus.scheduling;

public interface SubmittedTask
{
    void cancel();

    boolean isCancelled();

    boolean isDone();
}
