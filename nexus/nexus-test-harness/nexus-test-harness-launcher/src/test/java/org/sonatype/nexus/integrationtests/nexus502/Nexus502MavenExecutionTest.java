package org.sonatype.nexus.integrationtests.nexus502;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.UserMessageUtil;

public class Nexus502MavenExecutionTest
    extends AbstractNexusIntegrationTest
{

    static
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        printKnownErrorButDoNotFail( Nexus502MavenExecutionTest.class, "dependencyDownloadProtectedServer" );
    }

    private Verifier verifier;

    @Before
    public void createVerifier()
        throws VerificationException, IOException
    {
        verifier = new Verifier( getTestFile( "maven-project" ).getAbsolutePath(), false );

        verifier.deleteArtifact( "nexus502", "artifact-1", "1.0.0", "jar" );
        verifier.deleteArtifact( "nexus502", "artifact-1", "1.0.0", "pom" );
        verifier.deleteArtifact( "nexus502", "artifact-2", "1.0.0", "jar" );
        verifier.deleteArtifact( "nexus502", "artifact-2", "1.0.0", "pom" );
        verifier.deleteArtifact( "nexus502", "artifact-3", "1.0.0", "jar" );
        verifier.deleteArtifact( "nexus502", "artifact-3", "1.0.0", "pom" );
        verifier.deleteArtifact( "nexus502", "maven-execution", "1.0.0", "jar" );
        verifier.deleteArtifact( "nexus502", "maven-execution", "1.0.0", "pom" );

        verifier.resetStreams();

        List<String> options = new ArrayList<String>();
        options.add( "-s " + getTestFile( "repositories.xml" ).getAbsolutePath() );
        verifier.setCliOptions( options );
    }

    @Test
    public void dependencyDownload()
        throws Exception
    {
        verifier.executeGoal( "dependency:resolve" );
        verifier.verifyErrorFreeLog();
    }

    @Test( expected = VerificationException.class )
    public void dependencyDownloadPrivateServer()
        throws Exception
    {
        // Disable anonymous
        UserMessageUtil.removeUser( "anonymous" );

        verifier.executeGoal( "dependency:resolve" );
        verifier.verifyErrorFreeLog();
    }

    // Depends on nexus-508
    // @Test
    // public void dependencyDownloadProtectedServer()
    // throws Exception
    // {
    // // Disable anonymous
    // UserMessageUtil.removeUser( "anonymous" );
    //
    // List<String> options = new ArrayList<String>();
    // options.add( "-s " + getTestFile( "repositoriesWithAuthentication.xml" ).getAbsolutePath() );
    // verifier.setCliOptions( options );
    // verifier.executeGoal( "dependency:resolve" );
    // verifier.verifyErrorFreeLog();
    // }

}
