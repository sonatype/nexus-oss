package org.sonatype.nexus.integrationtests.nexus3233;

import java.io.File;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus3233IndexPomSha1IT
    extends AbstractNexusIntegrationTest
{
    protected SearchMessageUtil messageUtil;

    protected GroupMessageUtil groupMessageUtil;

    private String sha1;

    public Nexus3233IndexPomSha1IT()
    {
        this.messageUtil = new SearchMessageUtil();

        this.groupMessageUtil = new GroupMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Override
    protected void deployArtifacts()
        throws Exception
    {
        File pom = getTestResourceAsFile( "projects/pom/pom.xml" );
        sha1 = FileTestingUtils.createSHA1FromFile( pom );

        File sha1File = new File( pom.getParentFile(), "pom.xml.sha1" );
        FileUtils.fileWrite( sha1File.getAbsolutePath(), sha1 );

        super.deployArtifacts();

        RepositoryMessageUtil.updateIndexes( REPO_TEST_HARNESS_REPO );

        TaskScheduleUtil.waitForAllTasksToStop();
    }

    @Test
    public void searchForPomSHA1()
        throws Exception
    {
        Assert.assertNotNull( sha1 );
        NexusArtifact result = messageUtil.searchForSHA1( sha1 );
        Assert.assertNotNull( "Pom with " + sha1 + " not found", result );
    }
}
