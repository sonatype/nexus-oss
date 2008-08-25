package org.sonatype.nexus.integrationtests.nexus511;

import java.io.File;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;

public class Nexus511MavenDeployTest
    extends AbstractMavenNexusIT
{

    static
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    private Verifier verifier;

    @Before
    public void createVerifier()
        throws Exception
    {
        File mavenProject = getTestFile( "maven-project" );
        File settings = getTestFile( "server.xml" );
        verifier = createVerifier( mavenProject, settings );
    }

    @Test
    public void deploy()
        throws Exception
    {
        try
        {
            verifier.executeGoal( "deploy" );
            verifier.verifyErrorFreeLog();
        }
        catch ( VerificationException e )
        {
            failTest( verifier );
        }
    }

    @Test
    public void privateDeploy()
        throws Exception
    {
        // try to deploy without servers authentication tokens
        verifier.getCliOptions().clear();

        try
        {
            verifier.executeGoal( "deploy" );
            verifier.verifyErrorFreeLog();
            failTest( verifier );
        }
        catch ( VerificationException e )
        {
            // Expected exception
        }
    }

}
