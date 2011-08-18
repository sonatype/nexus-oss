package org.sonatype.nexus.proxy.repository.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.threads.NexusThreadFactory;
import org.sonatype.nexus.util.SystemPropertiesHelper;

@Component( role = ThreadPoolManager.class )
public class DefaultThreadPoolManager
    implements ThreadPoolManager, Disposable
{
    private static final int GROUP_REPOSITORY_THREAD_POOL_SIZE = SystemPropertiesHelper.getInteger(
        "nexus.groupRepositoryThreadPoolSize", 200 );

    private static final int PROXY_REPOSITORY_THREAD_POOL_SIZE = SystemPropertiesHelper.getInteger(
        "nexus.proxyRepositoryThreadPoolSize", 50 );

    private final ExecutorService groupRepositoryThreadPool;

    private final ExecutorService proxyRepositoryThreadPool;

    public DefaultThreadPoolManager()
    {
        // direct hand-off used! Proxy pool will use caller thread to execute the task when full!
        this.groupRepositoryThreadPool =
            new ThreadPoolExecutor( 0, GROUP_REPOSITORY_THREAD_POOL_SIZE, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new NexusThreadFactory( "group", "Group TPool" ),
                new CallerRunsPolicy() );

        // direct hand-off used! Proxy pool will use caller thread to execute the task when full!
        this.proxyRepositoryThreadPool =
            new ThreadPoolExecutor( 0, PROXY_REPOSITORY_THREAD_POOL_SIZE, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new NexusThreadFactory( "proxy", "Proxy TPool" ),
                new CallerRunsPolicy() );
    }

    @Override
    public ExecutorService getRepositoryThreadPool( Repository repository )
    {
        if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            return groupRepositoryThreadPool;
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            return proxyRepositoryThreadPool;
        }
        else
        {
            return null;
        }
    }

    @Override
    public synchronized void createPool( Repository repository )
    {
        // nop for now
    }

    @Override
    public synchronized void removePool( Repository repository )
    {
        // nop for now
    }

    @Override
    public void dispose()
    {
        terminatePool( groupRepositoryThreadPool );
    }

    // ==

    protected void terminatePool( final ExecutorService executorService )
    {
        if ( executorService != null )
        {
            executorService.shutdownNow();
        }
    }
}
