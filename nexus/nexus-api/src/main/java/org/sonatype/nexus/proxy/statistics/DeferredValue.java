package org.sonatype.nexus.proxy.statistics;

public interface DeferredValue<V>
{
    boolean isDone();

    V getValue();
}
