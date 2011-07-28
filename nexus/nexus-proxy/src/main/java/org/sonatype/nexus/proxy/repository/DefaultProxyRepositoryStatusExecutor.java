package org.sonatype.nexus.proxy.repository;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.sonatype.nexus.threads.NexusThreadFactory;

@Component( role = ProxyRepositoryStatusExecutor.class )
public class DefaultProxyRepositoryStatusExecutor
    implements ProxyRepositoryStatusExecutor, Disposable
{
    private final ExecutorService executorService;

    public DefaultProxyRepositoryStatusExecutor()
    {
        executorService = Executors.newCachedThreadPool( new NexusThreadFactory( "nxproxy", "Remote Status Update" ) );
    }

    @Override
    public void dispose()
    {
        executorService.shutdownNow();
    }

    // ==

    public <T> Future<T> submit( Callable<T> task )
    {
        return executorService.submit( task );
    }
}
