package org.sonatype.nexus.integrationtests.nxcm1327;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

public class NXCM1327AuthenticateIT
    extends AbstractPrivilegeTest
{

    private static final Integer NOT_AUTORIZED = Status.CLIENT_ERROR_FORBIDDEN.getCode();

    private static final Integer NO_AUTHENTICATION = Status.CLIENT_ERROR_UNAUTHORIZED.getCode();

    static
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Before
    public void enableSecurity()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    private static Gav GAV;

    @BeforeClass
    public static void init()
        throws IllegalArtifactCoordinateException
    {
        GAV = GavUtil.newGav( "nxcm1327", "artifact", "1.0.0" );
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        settings.setSecurityAnonymousAccessEnabled( false );
        SettingsMessageUtil.save( settings );
    }

    @Test
    public void contentNotAuthorized()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        try
        {
            downloadArtifactFromRepository( REPO_TEST_HARNESS_RELEASE_REPO, GAV, "target/downloads" );
            Assert.fail( "It should deny access!!!" );
        }
        catch ( FileNotFoundException e )
        {
            Assert.assertTrue( e.getMessage(), e.getMessage().contains( NOT_AUTORIZED.toString() ) );
        }
    }

    @Test
    public void serviceNotAuthorized()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        try
        {
            downloadSnapshotArtifact( REPO_TEST_HARNESS_RELEASE_REPO, GAV, new File( "target/downloads" ) );

            Assert.fail( "It should deny access!!!" );
        }
        catch ( FileNotFoundException e )
        {
            Assert.assertTrue( "The exception message should contain " + NOT_AUTORIZED.toString() + ", but was '"
                + e.getMessage() + "'.", e.getMessage().contains( NOT_AUTORIZED.toString() ) );
        }
    }

    @Test
    public void contentNoAuthentication()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setSecureTest( false );
        try
        {
            downloadArtifactFromRepository( REPO_TEST_HARNESS_RELEASE_REPO, GAV, "target/downloads" );
            Assert.fail( "It should deny access!!!" );
        }
        catch ( FileNotFoundException e )
        {
            Assert.assertTrue( e.getMessage(), e.getMessage().contains( NO_AUTHENTICATION.toString() ) );
        }
    }

    @Test
    public void serviceNoAuthentication()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setSecureTest( false );
        try
        {
            downloadSnapshotArtifact( REPO_TEST_HARNESS_RELEASE_REPO, GAV, new File( "target/downloads" ) );

            Assert.fail( "It should deny access!!!" );
        }
        catch ( FileNotFoundException e )
        {
            Assert.assertTrue( e.getMessage(), e.getMessage().contains( NO_AUTHENTICATION.toString() ) );
        }
    }

    @Test
    public void contentAdmin()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        downloadArtifactFromRepository( REPO_TEST_HARNESS_RELEASE_REPO, GAV, "target/downloads" );
    }

    @Test
    public void serviceAdmin()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        downloadSnapshotArtifact( REPO_TEST_HARNESS_RELEASE_REPO, GAV, new File( "target/downloads" ) );
    }

}
