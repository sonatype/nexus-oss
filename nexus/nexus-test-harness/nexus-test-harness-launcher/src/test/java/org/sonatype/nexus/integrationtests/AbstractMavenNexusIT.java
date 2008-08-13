package org.sonatype.nexus.integrationtests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;

import com.thoughtworks.xstream.XStream;

public class AbstractMavenNexusIT
    extends AbstractNexusIntegrationTest
{

    public AbstractMavenNexusIT()
    {
        super();
    }

    public AbstractMavenNexusIT( String testRepositoryId )
    {
        super( testRepositoryId );
    }

    public Verifier createVerifier( File mavenProject, File mavenRepository )
        throws VerificationException, IOException
    {
        return createVerifier( mavenProject, mavenRepository, null );
    }

    /**
     * Create a nexus verifier instance
     *
     * @param mavenProject Maven Project folder
     * @param mavenRepository Maven Repository folder
     * @param settings A settings.xml file
     * @return
     * @throws VerificationException
     * @throws IOException
     */
    public Verifier createVerifier( File mavenProject, File mavenRepository, File settings )
        throws VerificationException, IOException
    {
        Verifier verifier = new Verifier( mavenProject.getAbsolutePath(), false );
        String nexusLocalRepo = mavenRepository.getAbsolutePath();
        verifier.setLocalRepo( nexusLocalRepo );

        cleanRepository( mavenRepository );

        verifier.resetStreams();

        List<String> options = new ArrayList<String>();
        options.add( "-Dmaven.repo.local=" + nexusLocalRepo );
        if ( settings != null )
        {
            options.add( "-s " + settings.getAbsolutePath() );
        }
        verifier.setCliOptions( options );
        return verifier;
    }

    /**
     * Remove all artifacts on <code>testId</code> groupId
     *
     * @param verifier
     * @throws IOException
     */
    public void cleanRepository( File mavenRepo )
        throws IOException
    {

        File testGroupIdFolder = new File( mavenRepo, getTestId() );
        FileUtils.deleteDirectory( testGroupIdFolder );

    }

    /**
     * Workaround to get some decent logging when tests fail
     *
     * @throws IOException
     */
    protected void failTest( Verifier verifier )
        throws IOException
    {
        File logFile = new File( verifier.getBasedir(), "log.txt" );
        String log = FileUtils.readFileToString( logFile );
        log += "\n";
        log += new XStream().toXML( verifier );
        Assert.fail( log );
    }
}