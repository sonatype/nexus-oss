package org.sonatype.nexus.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.maven.index.ArtifactScanningListener;
import org.apache.maven.index.NexusIndexer;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.util.CompositeException;

/**
 * Test for NEXUS-5249 and related ones (see linked issues). In general, we ensure that 404 happened during remote
 * update does not break the batch-processing of ALL repositories (task should not stop and should go on process other
 * repositories). Also, 401/403/50x errors will throw IOException at the processng end (and hence, make the task
 * failed), but again, the batch is not broken due to one repo being broken, the exceptions are supressed until batch
 * end.
 * 
 * @author cstamas
 */
public class Nexus5249IndexerManagerTest
    extends AbstractIndexerManagerTest
{
    protected CountingInvocationHandler cih;

    protected int getIndexableRepositories()
    {
        int result = 0;
        for ( Repository repository : repositoryRegistry.getRepositories() )
        {
            if ( !repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class )
                && !repository.getRepositoryKind().isFacetAvailable( GroupRepository.class )
                && repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) && repository.isIndexable() )
            {
                result++;
            }
        }
        return result;
    }

    protected void prepare( final IOException failure )
        throws Exception
    {
        final MavenProxyRepository failingRepository = apacheSnapshots.adaptToFacet( MavenProxyRepository.class );
        failingRepository.setDownloadRemoteIndexes( true );
        failingRepository.commitChanges();

        final IndexUpdater realUpdater = lookup( IndexUpdater.class );
        final IndexUpdater fakeUpdater = new FakeIndexUpdater( realUpdater, failingRepository.getId(), failure );

        final NexusIndexer realIndexer = lookup( NexusIndexer.class );
        cih =
            new CountingInvocationHandler( realIndexer, NexusIndexer.class.getMethod( "scan", new Class[] {
                IndexingContext.class, String.class, ArtifactScanningListener.class, boolean.class } ) );
        final NexusIndexer fakeIndexer =
            (NexusIndexer) Proxy.newProxyInstance( getClass().getClassLoader(), new Class[] { NexusIndexer.class }, cih );

        final DefaultIndexerManager dim = (DefaultIndexerManager) indexerManager;
        dim.setIndexUpdater( fakeUpdater );
        dim.setNexusIndexer( fakeIndexer );
    }

    @Test
    public void remote404ResponseDoesNotFailsProcessing()
        throws Exception
    {
        // HTTP 404 pops up as FileNotFoundEx
        prepare( new FileNotFoundException( "fluke" ) );

        try
        {
            // reindex all
            indexerManager.reindexAllRepositories( null, false );

            // we continue here as 404 should not end up with exception (is "swallowed")
            // ensure we scanned all the repositories, even the one having 404 on remote update
            Assert.assertEquals( getIndexableRepositories(), cih.getInvocationCount() );
        }
        catch ( IOException e )
        {
            Assert.fail( "There should be no exception thrown!" );
        }
    }

    @Test
    public void remoteNon404ResponseFailsProcessingAtTheEnd()
        throws Exception
    {
        // HTTP 401/403/etc boils down as some other IOException
        final IOException ex = new IOException( "something bad happened" );
        prepare( ex );

        try
        {
            // reindex all
            indexerManager.reindexAllRepositories( null, false );

            // the above line should throw IOex
            Assert.fail( "There should be exception thrown!" );
        }
        catch ( IOException e )
        {
            // ensure we scanned all the repositories (minus the one failed, as it failed _BEFORE_ scan invocation)
            Assert.assertEquals( getIndexableRepositories() - 1, cih.getInvocationCount() );
            // ensure we have composite exception
            Assert.assertEquals( CompositeException.class, e.getCause().getClass() );
            // ensure we got back our bad exception
            Assert.assertEquals( ex, ( (CompositeException) e.getCause() ).getCauses().iterator().next() );
        }
    }

    // ==

    public static class DelegatingInvocationHandler
        implements InvocationHandler
    {
        private final Object delegate;

        public DelegatingInvocationHandler( final Object delegate )
        {
            this.delegate = delegate;
        }

        @Override
        public Object invoke( final Object proxy, final Method method, final Object[] args )
            throws Throwable
        {
            return method.invoke( delegate, args );
        }
    }

    public static class CountingInvocationHandler
        extends DelegatingInvocationHandler
    {
        private final Method method;

        private int count;

        public CountingInvocationHandler( final Object delegate, final Method countedMethod )
        {
            super( delegate );
            this.method = countedMethod;
            this.count = 0;
        }

        @Override
        public Object invoke( final Object proxy, final Method method, final Object[] args )
            throws Throwable
        {
            if ( method.equals( this.method ) )
            {
                count++;
            }
            return super.invoke( proxy, method, args );
        }

        public int getInvocationCount()
        {
            return count;
        }
    }

    public static class FakeIndexUpdater
        implements IndexUpdater
    {
        private final IndexUpdater delegate;

        private final String failingRepositoryId;

        private final IOException failure;

        private FakeIndexUpdater( final IndexUpdater delegate, final String failingRepositoryId,
                                  final IOException failure )
        {
            this.delegate = delegate;
            this.failingRepositoryId = failingRepositoryId;
            this.failure = failure;
        }

        @Override
        public IndexUpdateResult fetchAndUpdateIndex( final IndexUpdateRequest updateRequest )
            throws IOException
        {
            // ctx is is "${repoId}-ctx"
            if ( updateRequest.getIndexingContext().getId().startsWith( failingRepositoryId ) )
            {
                throw failure;
            }
            else
            {
                return delegate.fetchAndUpdateIndex( updateRequest );
            }
        }
    }
}
