package org.sonatype.nexus.proxy.repository.threads;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.threads.NexusThreadFactory;
import org.sonatype.nexus.util.SystemPropertiesHelper;

@Component( role = PoolManager.class )
public class DefaultPoolManager
    implements PoolManager, Disposable
{
    private static final int REPOSITORY_POOL_SIZE = SystemPropertiesHelper.getInteger(
        "nexus.repositoryThreadPoolSize", 50 );

    private final HashMap<String, ExecutorService> pools;

    public DefaultPoolManager()
    {
        this.pools = new HashMap<String, ExecutorService>();
    }

    @Override
    public ExecutorService getExecutorService( Repository repository )
    {
        return pools.get( repository.getId() );
    }

    @Override
    public synchronized void createPool( Repository repository )
    {
        final ExecutorService pool =
            new ThreadPoolExecutor( 0, REPOSITORY_POOL_SIZE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                new NexusThreadFactory( repository.getId(), repository.getId() + " Pool" ) );

        final ExecutorService oldPool = pools.put( repository.getId(), pool );

        if ( oldPool != null )
        {
            terminatePool( oldPool );
        }
    }

    @Override
    public synchronized void removePool( Repository repository )
    {
        final ExecutorService oldPool = pools.get( repository.getId() );

        if ( oldPool != null )
        {
            terminatePool( oldPool );
        }
    }

    @Override
    public void dispose()
    {
        for ( ExecutorService pool : pools.values() )
        {
            terminatePool( pool );
        }
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
