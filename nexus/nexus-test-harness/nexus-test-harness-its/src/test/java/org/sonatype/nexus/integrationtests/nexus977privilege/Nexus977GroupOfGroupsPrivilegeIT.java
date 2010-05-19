package org.sonatype.nexus.integrationtests.nexus977privilege;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class Nexus977GroupOfGroupsPrivilegeIT
    extends AbstractPrivilegeTest
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        RepositoryMessageUtil.updateIndexes( "g4" );
    }

    @Test
    public void testReadAll()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        giveUserRole( TEST_USER_NAME, "repo-all-read" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Gav gav = GavUtil.newGav( this.getTestId(), "project", "1.0.1" );

        File artifact = downloadArtifactFromGroup( "g4", gav, "./target/downloaded-jars" );

        assertTrue( artifact.exists() );

        File originalFile = this.getTestResourceAsFile( "projects/p1/project.jar" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, artifact ) );
    }

    @Test
    public void testReadG4()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        addPriv( TEST_USER_NAME, "g4" + "-read-priv", TargetPrivilegeDescriptor.TYPE, "1", null, "g4", "read" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Gav gav = GavUtil.newGav( this.getTestId(), "project", "0.8" );

        File artifact = downloadArtifactFromGroup( "g4", gav, "./target/downloaded-jars" );

        assertTrue( artifact.exists() );

        File originalFile = this.getTestResourceAsFile( "projects/p5/project.jar" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, artifact ) );
    }

    @Test
    public void testNoAccess()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Gav gav = GavUtil.newGav( this.getTestId(), "project", "2.1" );

        try
        {
            downloadArtifactFromGroup( "g4", gav, "./target/downloaded-jars" );
            Assert.fail();
        }
        catch ( FileNotFoundException e )
        {
            Assert.assertTrue( e.getMessage().contains( "403" ) );
        }

    }
}
