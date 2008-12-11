import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryConfig;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryRepository;

public class PaserConfigTest
{

    @Test
    public void parseConfig125()
        throws Exception
    {

        InputStream input = getClass().getResourceAsStream( "/config.1.2.5.xml" );
        ArtifactoryConfig config = ArtifactoryConfig.read( input );
        List<ArtifactoryRepository> localRepositories = config.getLocalRepositories();
        Assert.assertNotNull( localRepositories );
        Assert.assertEquals( 6, localRepositories.size() );

        ArtifactoryRepository libsReleases = localRepositories.get( 0 );
        Assert.assertEquals( "libs-releases", libsReleases.getKey() );
        Assert.assertEquals( "Local repository for in-house libraries", libsReleases.getDescription() );
        Assert.assertTrue( libsReleases.getHandleReleases() );
        Assert.assertFalse( libsReleases.getHandleSnapshots() );

        ArtifactoryRepository extSnapshots = localRepositories.get( 0 );
        Assert.assertEquals( "ext-snapshots", extSnapshots.getKey() );
        Assert.assertEquals( "Local repository for third party snapshots", extSnapshots.getDescription() );
        Assert.assertFalse( extSnapshots.getHandleReleases() );
        Assert.assertTrue( extSnapshots.getHandleSnapshots() );
    }
}
