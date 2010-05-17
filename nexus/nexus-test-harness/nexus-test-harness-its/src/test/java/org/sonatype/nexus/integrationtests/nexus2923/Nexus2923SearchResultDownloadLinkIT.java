package org.sonatype.nexus.integrationtests.nexus2923;

import java.io.File;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.util.StringUtils;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ReindexTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Test the 'pom' and 'artifact' download link in the search result panel
 * 
 * @author juven
 */
public class Nexus2923SearchResultDownloadLinkIT
    extends AbstractNexusIntegrationTest
{
     public Nexus2923SearchResultDownloadLinkIT()
    {
        super( "nexus2923" );
    }

    @Override
    public void runOnce()
        throws Exception
    {
        File testRepo = new File( nexusWorkDir, "storage/" + this.getTestRepositoryId() );
        File testFiles = getTestFile( "repo" );
        FileUtils.copyDirectory( testFiles, testRepo );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( this.getTestRepositoryId() );

        ScheduledServiceListResource task = TaskScheduleUtil.runTask( ReindexTaskDescriptor.ID, prop );
        Assert.assertNotNull( task );
        Assert.assertEquals( "SUBMITTED", task.getStatus() );
    }

    @Test
    public void testDownnloadLinks()
        throws Exception
    {
        List<NexusArtifact> artifacts = getSearchMessageUtil().searchFor( "xbean-server" );
        Assert.assertEquals( "The artifact should be indexed", 3, artifacts.size() );

        for ( NexusArtifact artifact : artifacts )
        {
            if ( StringUtils.isNotEmpty( artifact.getPomLink() ) )
            {
                assertLinkAvailable( artifact.getPomLink() );
            }

            if ( StringUtils.isNotEmpty( artifact.getArtifactLink() ) )
            {
                assertLinkAvailable( artifact.getArtifactLink() );
            }
        }
    }

    private void assertLinkAvailable( String link )
        throws Exception
    {
        Response response = RequestFacade.sendMessage( new URL( link ), Method.GET, null );

        Assert.assertEquals(
            "Invalid link: '" + link + "' response code is '" + response.getStatus().getCode() + "'",
            301,
            response.getStatus().getCode() );
    }
}
