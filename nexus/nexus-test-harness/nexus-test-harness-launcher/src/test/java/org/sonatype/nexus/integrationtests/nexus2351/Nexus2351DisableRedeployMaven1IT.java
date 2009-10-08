package org.sonatype.nexus.integrationtests.nexus2351;

import java.io.File;
import java.io.IOException;

import org.apache.maven.wagon.TransferFailedException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class Nexus2351DisableRedeployMaven1IT
    extends AbstractNexusIntegrationTest
{

    private RepositoryMessageUtil repoUtil = null;

    private File artifact;

    private File artifactMD5;

    public Nexus2351DisableRedeployMaven1IT()
        throws ComponentLookupException
    {
        this.repoUtil =
            new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML,
                                       this.getRepositoryTypeRegistry() );
    }

    @Before
    public void create()
    {
        artifact = this.getTestFile( "artifact.jar" );
        artifactMD5 = this.getTestFile( "artifact.jar.md5" );
    }

    @Test
    public void testM1ReleaseAllowRedeploy()
        throws Exception
    {

        String repoId = this.getTestId() + "-testM1ReleaseAllowRedeploy";

        this.createM1Repo( repoId, RepositoryWritePolicy.ALLOW_WRITE, RepositoryPolicy.RELEASE );

        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                     "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-1.0.0.jar" );
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                     "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-1.0.0.jar" );
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                     "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-1.0.0.jar" );

        // now test checksums
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                     "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-1.0.0.jar.md5" );
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                     "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-1.0.0.jar.md5" );
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                     "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-1.0.0.jar.md5" );
    }

    @Test
    public void testM1ReleaseNoRedeploy()
        throws Exception
    {
        String repoId = this.getTestId() + "-testM1ReleaseNoRedeploy";

        this.createM1Repo( repoId, RepositoryWritePolicy.ALLOW_WRITE_ONCE, RepositoryPolicy.RELEASE );

        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                     "testM1Repo.group/jars/testM1ReleaseNoRedeploy-1.0.0.jar" );

        // checksum should work
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                     "testM1Repo.group/jars/testM1ReleaseNoRedeploy-1.0.0.jar.md5" );

        try
        {
            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                         "testM1Repo.group/jars/testM1ReleaseNoRedeploy-1.0.0.jar" );
            Assert.fail( "expected TransferFailedException" );
        }
        catch ( TransferFailedException e )
        {
            // expected
        }

        try
        {
            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                         "testM1Repo.group/jars/testM1ReleaseNoRedeploy-1.0.0.jar" );
            Assert.fail( "expected TransferFailedException" );
        }
        catch ( TransferFailedException e )
        {
            // expected
        }

        try
        {
            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                         "testM1Repo.group/jars/testM1ReleaseNoRedeploy-1.0.0.jar.md5" );
            Assert.fail( "expected TransferFailedException" );
        }
        catch ( TransferFailedException e )
        {
            // expected
        }
    }

    @Test
    public void testM1ReleaseReadOnly()
        throws Exception
    {
        String repoId = this.getTestId() + "-testM1ReleaseReadOnly";

        this.createM1Repo( repoId, RepositoryWritePolicy.READ_ONLY, RepositoryPolicy.RELEASE );

        try
        {

            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                         "testM1Repo.group/jars/testM1ReleaseReadOnly-1.0.0.jar" );
            Assert.fail( "expected TransferFailedException" );

        }
        catch ( TransferFailedException e )
        {
            // expected
        }

        try
        {

            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                         "testM1Repo.group/jars/testM1ReleaseReadOnly-1.0.0.jar.md5" );
            Assert.fail( "expected TransferFailedException" );
        }
        catch ( TransferFailedException e )
        {
            // expected
        }

    }

    @Test
    public void testM1SnapshotAllowRedeploy()
        throws Exception
    {
        String repoId = this.getTestId() + "-testM1SnapshotAllowRedeploy";

        this.createM1Repo( repoId, RepositoryWritePolicy.ALLOW_WRITE, RepositoryPolicy.SNAPSHOT );

        // ONLY SUPPORT -SNAPSHOT
        // DeployUtils.deployWithWagon(
        // this.getContainer(),
        // "http",
        // this.getRepositoryUrl( repoId ),
        // artifact,
        // "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-20090101.jar" );
        // DeployUtils.deployWithWagon(
        // this.getContainer(),
        // "http",
        // this.getRepositoryUrl( repoId ),
        // artifact,
        // "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-20090102.jar" );
        // DeployUtils.deployWithWagon(
        // this.getContainer(),
        // "http",
        // this.getRepositoryUrl( repoId ),
        // artifact,
        // "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-20090103.jar" );
        //
        // // now for the MD5
        // DeployUtils.deployWithWagon(
        // this.getContainer(),
        // "http",
        // this.getRepositoryUrl( repoId ),
        // artifactMD5,
        // "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-20090103.jar.md5" );
        //
        // DeployUtils.deployWithWagon(
        // this.getContainer(),
        // "http",
        // this.getRepositoryUrl( repoId ),
        // artifactMD5,
        // "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-20090103.jar.md5" );

        // now for just the -SNAPSHOT

        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                     "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-SNAPSHOT.jar" );

        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                     "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-SNAPSHOT.jar" );

        // MD5
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                     "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-SNAPSHOT.jar.md5" );

        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                     "testM1Repo.group/jars/testM1ReleaseAllowRedeploy-SNAPSHOT.jar.md5" );

    }

    @Test
    public void testM1SnapshotNoRedeploy()
        throws Exception
    {
        String repoId = this.getTestId() + "-testM1SnapshotNoRedeploy";

        this.createM1Repo( repoId, RepositoryWritePolicy.ALLOW_WRITE_ONCE, RepositoryPolicy.SNAPSHOT );

        // ONLY SUPPORT -SNAPSHOT for M1
        // DeployUtils.deployWithWagon(
        // this.getContainer(),
        // "http",
        // this.getRepositoryUrl( repoId ),
        // artifact,
        // "testM1Repo.group/jars/testM1ReleaseNoRedeploy-20090101.jar" );
        //
        // DeployUtils.deployWithWagon(
        // this.getContainer(),
        // "http",
        // this.getRepositoryUrl( repoId ),
        // artifact,
        // "testM1Repo.group/jars/testM1ReleaseNoRedeploy-20090102.jar" );
        //
        // DeployUtils.deployWithWagon(
        // this.getContainer(),
        // "http",
        // this.getRepositoryUrl( repoId ),
        // artifact,
        // "testM1Repo.group/jars/testM1ReleaseNoRedeploy-20090102.jar" );

        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                     "testM1Repo.group/jars/testM1ReleaseNoRedeploy-SNAPSHOT.jar" );

        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                     "testM1Repo.group/jars/testM1ReleaseNoRedeploy-SNAPSHOT.jar" );

        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                     "testM1Repo.group/jars/testM1ReleaseNoRedeploy-SNAPSHOT.jar.md5" );

        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                     "testM1Repo.group/jars/testM1ReleaseNoRedeploy-SNAPSHOT.jar.md5" );
    }

    @Test
    public void testM1SnapshotReadOnly()
        throws Exception
    {

        String repoId = this.getTestId() + "-testM1SnapshotReadOnly";

        this.createM1Repo( repoId, RepositoryWritePolicy.READ_ONLY, RepositoryPolicy.SNAPSHOT );

        try
        {

            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                         "testM1Repo.group/jars/testM1ReleaseReadOnly-20090102.jar" );
            Assert.fail( "expected TransferFailedException" );
        }
        catch ( TransferFailedException e )
        {
            // expected
        }
        try
        {

            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                         "testM1Repo.group/jars/testM1ReleaseReadOnly-20090102.jar.md5" );
            Assert.fail( "expected TransferFailedException" );

        }
        catch ( TransferFailedException e )
        {
            // expected
        }
        try
        {

            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifactMD5,
                                         "testM1Repo.group/jars/testM1ReleaseReadOnly-SNAPSHOT.jar.md5" );
            Assert.fail( "expected TransferFailedException" );

        }
        catch ( TransferFailedException e )
        {
            // expected
        }
        try
        {

            DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryUrl( repoId ), artifact,
                                         "testM1Repo.group/jars/testM1ReleaseReadOnly-SNAPSHOT.jar" );
            Assert.fail( "expected TransferFailedException" );

        }
        catch ( TransferFailedException e )
        {
            // expected
        }
    }

    private void createM1Repo( String repoId, RepositoryWritePolicy writePolicy, RepositoryPolicy releasePolicy )
        throws IOException
    {
        RepositoryResource repo = new RepositoryResource();

        repo.setId( repoId );
        repo.setBrowseable( true );
        repo.setExposed( true );
        repo.setRepoType( "hosted" );
        repo.setName( repoId );
        repo.setRepoPolicy( releasePolicy.name() );
        repo.setWritePolicy( writePolicy.name() );
        repo.setProvider( "maven1" );
        repo.setFormat( "maven1" );

        this.repoUtil.createRepository( repo );
    }

    @BeforeClass
    public static void clean()
        throws Exception
    {
        cleanWorkDir();
    }

}
