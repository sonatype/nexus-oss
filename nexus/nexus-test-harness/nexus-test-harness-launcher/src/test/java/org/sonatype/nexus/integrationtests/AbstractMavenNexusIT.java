package org.sonatype.nexus.integrationtests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.sonatype.nexus.test.utils.TestProperties;

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

    public Verifier createVerifier( File mavenProject )
        throws VerificationException, IOException
    {
        return createVerifier( mavenProject, null );
    }

    /**
     * Create a nexus verifier instance
     *
     * @param mavenProject Maven Project folder
     * @param settings A settings.xml file
     * @return
     * @throws VerificationException
     * @throws IOException
     */
    public Verifier createVerifier( File mavenProject, File settings )
        throws VerificationException, IOException
    {
        Verifier verifier = new Verifier( mavenProject.getAbsolutePath(), false );

        File mavenRepository = new File(TestProperties.getString( "maven.local.repo" ));
        verifier.setLocalRepo( mavenRepository.getAbsolutePath() );
        cleanRepository( mavenRepository );

        verifier.resetStreams();

        List<String> options = new ArrayList<String>();
        options.add( "-Dmaven.repo.local=" + mavenRepository.getAbsolutePath() );
        if ( settings != null )
        {
            options.add( "-s " + settings.getAbsolutePath() );
        }
        else
        {
            options.add( "-s " + this.getOverridableFile( "settings.xml" ) );
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