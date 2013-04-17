package org.sonatype.nexus.maven.tasks;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.OpenAccessManager;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.target.Target;

/**
 * @since 2.5
 */
public class DefaultReleaseRemoverIT
    extends AbstractMavenRepoContentTests
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testDeletionOfReleases()
        throws Exception
    {
        fillInRepo();
        releases.setAccessManager( new OpenAccessManager() );
        ReleaseRemovalResult releaseRemovalResult =
            defaultNexus.removeReleases( new ReleaseRemovalRequest( releases.getId(), 2, "" ) );
        // pom + jar + sha1 for both
        assertThat( releaseRemovalResult.getDeletedFileCount(), is( 4 ) );
        assertThat( releaseRemovalResult.isSuccessful(), is( true ) );
        StorageItem item = null;
        try
        {
            releases.retrieveItem( new ResourceStoreRequest( "org/sonatype/test/1.0" ) );
        }
        catch ( ItemNotFoundException e )
        {
            //we expect this cannot be found
        }
        assertThat( item, nullValue() );

        item = releases.retrieveItem( new ResourceStoreRequest( "org/sonatype/test/1.0.1" ));
        assertThat( item, notNullValue());
    }

    @Test
    public void testDeletionOfReleasesWithTarget()
        throws Exception
    {
        fillInRepo();
        releases.setAccessManager( new OpenAccessManager() );
        targetRegistry.addRepositoryTarget(
            new Target( "test", "test", new Maven2ContentClass(), Lists.newArrayList( ".*/org/sonatype/.*" ) ) );
        targetRegistry.commitChanges();
        ReleaseRemovalResult releaseRemovalResult =
            defaultNexus.removeReleases( new ReleaseRemovalRequest( releases.getId(), 2, "test" ) );
        assertThat( releaseRemovalResult.getDeletedFileCount(), is( 4 ) );
        assertThat( releaseRemovalResult.isSuccessful(), is( true ) );
    }

    @Test
    public void testDeletionOfReleasesWithNonMatchingTarget()
        throws Exception
    {
        fillInRepo();
        releases.setAccessManager( new OpenAccessManager() );
        targetRegistry.addRepositoryTarget(
            new Target( "test", "test", new Maven2ContentClass(), Lists.newArrayList( ".*/com/sonatype/.*" ) ) );
        targetRegistry.commitChanges();
        ReleaseRemovalResult releaseRemovalResult =
            defaultNexus.removeReleases( new ReleaseRemovalRequest( releases.getId(), 2, "test" ) );
        assertThat( releaseRemovalResult.getDeletedFileCount(), is( 0 ) );
        assertThat( releaseRemovalResult.isSuccessful(), is( true ) );
        try
        {
            StorageItem storageItem = releases.retrieveItem( new ResourceStoreRequest( "org/sonatype/test/1.0" ) );
        }
        catch ( ItemNotFoundException e )
        {
            Assert.fail();
        }
    }

    @Test
    public void testDeletionOfReleasesWithMissingTarget()
        throws Exception
    {
        thrown.expect( IllegalStateException.class );
        defaultNexus.removeReleases( new ReleaseRemovalRequest( releases.getId(), 2, "test" ) );
    }
}
