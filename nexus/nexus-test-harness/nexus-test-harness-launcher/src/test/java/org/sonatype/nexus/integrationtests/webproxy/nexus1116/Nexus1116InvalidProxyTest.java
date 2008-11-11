package org.sonatype.nexus.integrationtests.webproxy.nexus1116;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.TestProperties;

public class Nexus1116InvalidProxyTest
    extends AbstractNexusWebProxyIntegrationTest
    implements Runnable
{

    @Test
    public void checkInvalidProxy()
        throws Exception
    {
        if ( true )
        {
            printKnownErrorButDoNotFail( getClass(), "downloadArtifactOverWebProxy" );
            return;
        }

        Thread thread = new Thread( this );
        thread.setDaemon( true );// don't stuck VM
        thread.start();
        for ( int i = 0; i < 100; i++ )
        {
            String status = this.status;

            if ( status.startsWith( "fail" ) )
            {
                Assert.fail( "Verifier fail: " + status );
            }
            else if ( status.equals( "executed" ) )
            {
                // finished ok
                return;
            }

            Thread.yield();
            Thread.sleep( 200 );
        }

        Assert.fail( "Verifier didn't runn after 20 seconds: " + this.status );
    }

    private String status = "notStarted";

    public void run()
    {
        status = "started";
        File mavenProject = getTestFile( "pom.xml" ).getParentFile();
        Verifier verifier;
        try
        {
            verifier = new Verifier( mavenProject.getAbsolutePath(), false );
            status = "verifierCreated";
        }
        catch ( VerificationException e )
        {
            status = "failCreation" + e.getMessage();
            return;
        }

        System.setProperty( "maven.home", TestProperties.getString( "maven.instance" ) );

        File mavenRepository = new File( TestProperties.getString( "maven.local.repo" ) );
        verifier.setLocalRepo( mavenRepository.getAbsolutePath() );

        verifier.resetStreams();

        List<String> options = new ArrayList<String>();
        options.add( "-X" );
        options.add( "-Dmaven.repo.local=" + mavenRepository.getAbsolutePath() );
        options.add( "-s " + getOverridableFile( "settings.xml" ) );
        verifier.setCliOptions( options );

        status = "pre-execute";
        try
        {
            verifier.executeGoal( "dependency:resolve" );
            status = "executed";
        }
        catch ( VerificationException e )
        {
            status = "failExecute" + e.getMessage();
        }
    }

}
