package org.sonatype.nexus.proxy.repository;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ProxyRepositoryStatusExecutor
{
    <T> Future<T> submit( Callable<T> task );
}
