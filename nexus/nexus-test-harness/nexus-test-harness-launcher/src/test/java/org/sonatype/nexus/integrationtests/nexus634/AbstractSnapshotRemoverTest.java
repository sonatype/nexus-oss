package org.sonatype.nexus.integrationtests.nexus634;

import java.io.File;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.nexus533.TaskScheduleUtil;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.MavenDeployer;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class AbstractSnapshotRemoverTest
    extends AbstractNexusIntegrationTest
{

    @SuppressWarnings( "unchecked" )
    public static Collection<File> listFiles( File directory, String[] extensions, boolean recursive )
    {
        return FileUtils.listFiles( directory, extensions, recursive );
    }

    protected File artifactFolder;

    protected File repositoryPath;

    public AbstractSnapshotRemoverTest()
    {
        super( "nexus-test-harness-snapshot-repo" );
    }

    @Before
    public void deploySnapshotArtifacts()
        throws Exception
    {
        initFolders();

        File oldSnapshot = getTestFile( "repo" );

        // Copying to keep an old timestamp
        FileUtils.copyDirectory( oldSnapshot, repositoryPath );

        RepositoryMessageUtil.updateIndexes( "nexus-test-harness-snapshot-repo" );

        Gav gav =
            new Gav( "nexus634", "artifact", "1.0-SNAPSHOT", null, "jar", 0, 0L, null, true, false, null, false, null );
        File fileToDeploy = getTestFile( "artifact-1.jar" );

        // Deploying a fresh timestamp artifact
        MavenDeployer.deploy( gav, getNexusTestRepoUrl(), fileToDeploy, null );

        // Artifacts should be deployed here
        Assert.assertTrue( "nexus643:artifact:1.0-SNAPSHOT folder doesn't exists!", artifactFolder.isDirectory() );
    }

    public void initFolders()
        throws Exception
    {
        repositoryPath = new File( nexusBaseDir, "runtime/work/storage/nexus-test-harness-snapshot-repo" );
        artifactFolder = new File( repositoryPath, "nexus634/artifact/1.0-SNAPSHOT" );
    }

    protected void runSnapshotRemover( String repositoryOrGroupId, int minSnapshotsToKeep, int removeOlderThanDays,
                                       boolean removeIfReleaseExists )
        throws Exception
    {
        ScheduledServicePropertyResource repositoryProp = new ScheduledServicePropertyResource();
        repositoryProp.setId( "repositoryOrGroupId" );
        repositoryProp.setValue( repositoryOrGroupId );

        ScheduledServicePropertyResource keepSnapshotsProp = new ScheduledServicePropertyResource();
        keepSnapshotsProp.setId( "minSnapshotsToKeep" );
        keepSnapshotsProp.setValue( String.valueOf( minSnapshotsToKeep ) );

        ScheduledServicePropertyResource ageProp = new ScheduledServicePropertyResource();
        ageProp.setId( "removeOlderThanDays" );
        ageProp.setValue( String.valueOf( removeOlderThanDays ) );

        ScheduledServicePropertyResource removeReleasedProp = new ScheduledServicePropertyResource();
        removeReleasedProp.setId( "removeIfReleaseExists" );
        removeReleasedProp.setValue( String.valueOf( removeIfReleaseExists ) );

        TaskScheduleUtil.runTask( "org.sonatype.nexus.maven.tasks.SnapshotRemoverTask",// 
                                  repositoryProp, keepSnapshotsProp, ageProp, removeReleasedProp );
    }

}