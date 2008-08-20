package org.sonatype.nexus.integrationtests.nexus643;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.nexus603.ScheduleTaskUtil;

public class Nexus643EmptyTrashTaskTest
    extends AbstractNexusIntegrationTest
{
    @Test
    public void checkTask()
        throws Exception
    {

        delete( "nexus643" );

        File trashContent = new File( nexusBaseDir, "runtime/work/trash/nexus-test-harness-repo/nexus603" );
        Assert.assertTrue( "Something should be at trash!", trashContent.exists() );

        // This is THE important part
        ScheduleTaskUtil.runTask( "org.sonatype.nexus.tasks.EmptyTrashTask" );

        Assert.assertFalse( "Trash should be empty!", trashContent.exists() );
    }

    private void delete( String groupId )
        throws IOException
    {
        String serviceURI = "service/local/repositories/nexus-test-harness-repo/content/" + groupId + "/";
        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertTrue( "Unable to delete nexus603 artifacts", response.getStatus().isSuccess() );
    }
}
