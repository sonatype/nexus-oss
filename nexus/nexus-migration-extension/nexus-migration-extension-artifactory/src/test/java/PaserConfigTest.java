import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryConfig;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryProxy;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryRepository;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryVirtualRepository;

public class PaserConfigTest
{

    @Test
    public void parseConfig125()
        throws Exception
    {

        InputStream input = getClass().getResourceAsStream( "/config.1.2.5.xml" );
        ArtifactoryConfig config = ArtifactoryConfig.read( input );

        // validate local repos
        List<ArtifactoryRepository> localRepositories = config.getLocalRepositories();
        Assert.assertNotNull( localRepositories );
        Assert.assertEquals( 6, localRepositories.size() );

        ArtifactoryRepository libsReleases = localRepositories.get( 0 );
        Assert.assertEquals( "libs-releases", libsReleases.getKey() );
        Assert.assertEquals( "Local repository for in-house libraries", libsReleases.getDescription() );
        Assert.assertTrue( libsReleases.getHandleReleases() );
        Assert.assertFalse( libsReleases.getHandleSnapshots() );
        Assert.assertNull( libsReleases.getUrl() );

        ArtifactoryRepository extSnapshots = localRepositories.get( 5 );
        Assert.assertEquals( "ext-snapshots", extSnapshots.getKey() );
        Assert.assertEquals( "Local repository for third party snapshots", extSnapshots.getDescription() );
        Assert.assertFalse( extSnapshots.getHandleReleases() );
        Assert.assertTrue( extSnapshots.getHandleSnapshots() );
        Assert.assertNull( extSnapshots.getUrl() );

        // validate remote repos
        List<ArtifactoryRepository> remoteRepositories = config.getRemoteRepositories();
        Assert.assertNotNull( remoteRepositories );
        Assert.assertEquals( 3, remoteRepositories.size() );

        ArtifactoryRepository repo1 = remoteRepositories.get( 0 );
        Assert.assertEquals( "repo1", repo1.getKey() );
        Assert.assertNull( repo1.getDescription() );
        Assert.assertTrue( repo1.getHandleReleases() );
        Assert.assertFalse( repo1.getHandleSnapshots() );
        Assert.assertEquals( "http://repo1.maven.org/maven2", repo1.getUrl() );

        ArtifactoryRepository codehausSnapshots = remoteRepositories.get( 1 );
        Assert.assertEquals( "codehaus-snapshots", codehausSnapshots.getKey() );
        Assert.assertNull( codehausSnapshots.getDescription() );
        Assert.assertFalse( codehausSnapshots.getHandleReleases() );
        Assert.assertTrue( codehausSnapshots.getHandleSnapshots() );
        Assert.assertEquals( "http://snapshots.repository.codehaus.org", codehausSnapshots.getUrl() );

        // validate virtual repos
        List<ArtifactoryVirtualRepository> virtualRepositories = config.getVirtualRepositories();
        Assert.assertNotNull( virtualRepositories );
        Assert.assertEquals( 1, virtualRepositories.size() );

        ArtifactoryVirtualRepository snapshotsOnly = virtualRepositories.get( 0 );
        Assert.assertEquals( "snapshots-only", snapshotsOnly.getKey() );
        Assert.assertEquals( 4, snapshotsOnly.getRepositories().size() );
        Assert.assertEquals( "plugins-snapshots", snapshotsOnly.getRepositories().get( 1 ) );

        // validate proxies
        Map<String, ArtifactoryProxy> proxies = config.getProxies();
        Assert.assertNotNull( proxies );
        Assert.assertEquals( 1, proxies.size() );

        ArtifactoryProxy unsuedProxy = proxies.values( ).iterator().next();
        Assert.assertEquals( "unused-proxy", unsuedProxy.getKey() );
        Assert.assertEquals( "host", unsuedProxy.getHost() );
        Assert.assertEquals( 8080, unsuedProxy.getPort() );
        Assert.assertEquals( "un", unsuedProxy.getUsername() );
        Assert.assertEquals( "pw", unsuedProxy.getPassword() );
        Assert.assertEquals( "mydomain", unsuedProxy.getDomain() );
    }
}
