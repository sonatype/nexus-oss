package org.sonatype.nexus.plugins.migration.nxcm281;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class NXCM281DownloadRedirectSecurityTest
    extends AbstractMigrationIntegrationTest
{

    @BeforeClass
    public static void start()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );

        TaskScheduleUtil.waitForTasks( 40 );
        Thread.sleep( 2000 );
    }

    @Test
    public void downloadWithPermition()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        Assert.assertTrue( "Unable to download artifact", Status.isSuccess( download() ) );
    }

    @Test
    public void downloadWithoutPermition()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "dummy" );

        Assert.assertEquals( "Unable to download artifact", 401, download() );

    }

    private int download()
        throws Exception
    {
        URL url =
            new URL( "http://localhost:" + nexusApplicationPort
                + "/artifactory/main-local/nxcm281/released/1.0/released-1.0.jar" );

        Status status = RequestFacade.sendMessage( url, Method.GET, null ).getStatus();
        return status.getCode();
    }

}
