package org.sonatype.nexus.integrationtests.proxy.nexus603;

import static org.sonatype.nexus.test.utils.FileTestingUtils.compareFileSHA1s;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.MavenDeployer;

public class Nexus603ClearCacheTaskTest
    extends AbstractNexusProxyIntegrationTest
{

    private static final Gav GAV =
        new Gav( "nexus603", "artifact", "1.0-SNAPSHOT", null, "jar", 0, 0L, null, true, false, null, false, null );

    public Nexus603ClearCacheTaskTest()
    {
        super( "tasks-snapshot-repo" );
    }

    public void addSnapshotArtifactToProxy( File fileToDeploy )
        throws Exception
    {
        String repositoryUrl = "file://" + localStorageDir + "/tasks-snapshot-repo";
        MavenDeployer.deploy( GAV, repositoryUrl, fileToDeploy, null );
    }

    @Test
    public void checkTask()
        throws Exception
    {
        /*
         * fetch something from a remote repo, run clearCache from root, on _remote repo_ put a newer timestamped file,
         * and rerequest again the same (the filenames will be the same, only the content/timestamp should change),
         * nexus should refetch it. BUT, this works for snapshot nexus reposes only, release reposes do not refetch!
         */
        File artifact1 = getTestFile( "artifact-1.jar" );
        addSnapshotArtifactToProxy( artifact1 );

        File firstDownload = resolveArtifact( GAV );
        Assert.assertTrue( "First time, should download artifact 1", // 
                           compareFileSHA1s( firstDownload, artifact1 ) );

        File artifact2 = getTestFile( "artifact-2.jar" );
        addSnapshotArtifactToProxy( artifact2 );
        File secondDownload = resolveArtifact( GAV );
        Assert.assertTrue( "Before ClearCache should download artifact 1",// 
                           compareFileSHA1s( secondDownload, artifact1 ) );

        // This is THE important part
        ScheduleTaskUtil.runTask( "org.sonatype.nexus.tasks.ClearCacheTask" );

        File thirdDownload = resolveArtifact( GAV );
        Assert.assertTrue( "After ClearCache should download artifact 2", //
                           compareFileSHA1s( thirdDownload, artifact2 ) );
    }

    private File resolveArtifact( Gav gav )
        throws IOException
    {
        return ScheduleTaskUtil.resolve( "tasks-snapshot-repo", gav, new File( "target/download" ) );
    }

}
