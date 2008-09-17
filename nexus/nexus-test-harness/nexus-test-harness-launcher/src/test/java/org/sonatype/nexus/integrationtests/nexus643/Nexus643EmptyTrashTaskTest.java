package org.sonatype.nexus.integrationtests.nexus643;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.tasks.EmptyTrashTask;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Tests empty trash task.
 */
public class Nexus643EmptyTrashTaskTest
    extends AbstractNexusIntegrationTest
{
    @Test
    public void emptyTrashTask()
        throws Exception
    {

        delete( "nexus643" );

        File trashContent = new File( nexusBaseDir, "runtime/work/trash/nexus-test-harness-repo/nexus643" );
        Assert.assertTrue( "Something should be at trash!", trashContent.exists() );

        // This is THE important part
        TaskScheduleUtil.runTask( EmptyTrashTask.HINT );

        Assert.assertFalse( "Trash should be empty!", trashContent.exists() );
    }

    private void delete( String groupId )
        throws IOException
    {
        String serviceURI = "service/local/repositories/nexus-test-harness-repo/content/" + groupId + "/";
        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertTrue( "Unable to delete nexus643 artifacts", response.getStatus().isSuccess() );
    }
}
