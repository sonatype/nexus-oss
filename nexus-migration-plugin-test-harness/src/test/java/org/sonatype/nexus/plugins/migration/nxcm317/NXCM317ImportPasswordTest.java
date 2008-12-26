package org.sonatype.nexus.plugins.migration.nxcm317;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class NXCM317ImportPasswordTest
    extends AbstractMigrationIntegrationTest
{

    @BeforeClass
    public static void enableSecurity()
    {
        // enable security context
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }
    
    @Test
    public void migrateSecurityWithPassword()
        throws Exception
    {
        String userId = "admin1";
        String password = "password";
        
        TestContext testContext = TestContainer.getInstance().getTestContext();
        testContext.setUsername( userId );
        testContext.setPassword( password );

        // before migration, the user cannot login
        Status status = login();
        Assert.assertFalse( status.isSuccess() );

        // do migration( require login)
        testContext.useAdminForRequests();
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactory.zip" ) );
        commitMigration( migrationSummary );

        // after migration, artifactory user can login
        status = login();
        Assert.assertTrue( status.isSuccess() );
    }

    private Status login()
        throws IOException
    {
        String serviceURI = "service/local/authentication/login";

        return RequestFacade.doGetRequest( serviceURI ).getStatus();
    }

}
