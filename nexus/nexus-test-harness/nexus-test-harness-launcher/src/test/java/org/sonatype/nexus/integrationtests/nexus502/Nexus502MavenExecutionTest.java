package org.sonatype.nexus.integrationtests.nexus502;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.UserMessageUtil;

import com.thoughtworks.xstream.XStream;

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

        cleanRepository();

        verifier.resetStreams();

        List<String> options = new ArrayList<String>();
        options.add( "-s " + getTestFile( "repositories.xml" ).getAbsolutePath() );
        verifier.setCliOptions( options );
    }

    @After
    public void cleanRepository()
        throws IOException
    {
        if ( verifier == null )
        {
            return;
        }

        File localRepo = new File( verifier.localRepo );
        if ( !localRepo.exists() )
        {
            Assert.fail( "File not found " + localRepo.getAbsolutePath() );
        }
        File nexus502 = new File( localRepo, "nexus502" );
        FileUtils.deleteDirectory( nexus502 );

        verifier.deleteArtifact( "nexus502", "artifact-1", "1.0.0", "jar" );
        verifier.deleteArtifact( "nexus502", "artifact-1", "1.0.0", "pom" );
        verifier.deleteArtifact( "nexus502", "artifact-2", "1.0.0", "jar" );
        verifier.deleteArtifact( "nexus502", "artifact-2", "1.0.0", "pom" );
        verifier.deleteArtifact( "nexus502", "artifact-3", "1.0.0", "jar" );
        verifier.deleteArtifact( "nexus502", "artifact-3", "1.0.0", "pom" );
        verifier.deleteArtifact( "nexus502", "maven-execution", "1.0.0", "jar" );
        verifier.deleteArtifact( "nexus502", "maven-execution", "1.0.0", "pom" );
    }

    @Test
    public void dependencyDownload()
        throws Exception
    {
        try
        {
            verifier.executeGoal( "dependency:resolve" );
            verifier.verifyErrorFreeLog();
        }
        catch ( VerificationException e )
        {
            failTest();
        }
    }

    @Test
    public void dependencyDownloadPrivateServer()
        throws Exception
    {
        // Disable anonymous
        disableUser( "anonymous" );

        verifier = new Verifier( getTestFile( "maven-project2" ).getAbsolutePath(), false );
        verifier.resetStreams();
        try
        {
            verifier.executeGoal( "dependency:resolve" );
            verifier.verifyErrorFreeLog();
            failTest();
        }
        catch ( VerificationException e )
        {

        }
    }

    /**
     * Workaround to get more details when tests fails
     *
     * @throws IOException
     */
    private void failTest()
        throws IOException
    {
        File logFile = new File( verifier.getBasedir(), "log.txt" );
        String log = FileUtils.readFileToString( logFile );
        Assert.fail( log );
    }

    private UserResource disableUser( String userId )
        throws IOException
    {
        UserMessageUtil util =
            new UserMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML );
        return util.disableUser( userId );
    }

    // Depends on nexus-508
    // @Test
    // public void dependencyDownloadProtectedServer()
    // throws Exception
    // {
    // // Disable anonymous
    // disableUser( "anonymous" );
    //
    // List<String> options = new ArrayList<String>();
    // options.add( "-s " + getTestFile( "repositoriesWithAuthentication.xml" ).getAbsolutePath() );
    // verifier.setCliOptions( options );
    // verifier.executeGoal( "dependency:resolve" );
    // verifier.verifyErrorFreeLog();
    // }

}
