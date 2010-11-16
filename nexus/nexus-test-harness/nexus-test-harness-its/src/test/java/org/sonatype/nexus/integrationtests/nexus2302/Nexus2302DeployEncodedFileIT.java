package org.sonatype.nexus.integrationtests.nexus2302;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.it.Verifier;
import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.test.utils.ContentListMessageUtil;
import org.sonatype.nexus.test.utils.MavenDeployer;
import org.testng.annotations.Test;

public class Nexus2302DeployEncodedFileIT
    extends AbstractNexusIntegrationTest
{

    public Nexus2302DeployEncodedFileIT()
    {
        super.setTestRepositoryId( REPO_TEST_HARNESS_REPO );
    }

    @Test
    public void deployUsingMaven()
        throws Exception
    {
        Gav gav =
            new Gav( "nexus2302", "artifact", "1.0", "c++", "jar", null, null, null, false, false, null, false, null );
        Verifier v =
            MavenDeployer.deployAndGetVerifier( gav, getRepositoryUrl( REPO_TEST_HARNESS_REPO ),
                getTestFile( "artifact.jar" ), getOverridableFile( "settings.xml" ) );
        v.verifyErrorFreeLog();

        // direct download
        downloadArtifact( gav, "target/nexus2302/direct.jar" );

        // redirect download
        downloadSnapshotArtifact( REPO_TEST_HARNESS_REPO, gav, new File( "target/nexus2302" ) );

        checkFileSystem( gav );
        checkIndex( gav );
        checkRepoBrowse( gav );
        checkBrowse( gav );
        delete( gav );
    }

    private void checkBrowse( Gav gav )
        throws Exception
    {
        URL url = new URL( nexusBaseUrl + "content/repositories/" + REPO_TEST_HARNESS_REPO + "/" );
        String content = IOUtil.toString( url.openStream() );
        assertThat( content, containsString( gav.getGroupId() ) );

        url = new URL( url.toString() + gav.getGroupId() + "/" );
        content = IOUtil.toString( url.openStream() );
        assertThat( content, containsString( gav.getArtifactId() ) );

        url = new URL( url.toString() + gav.getArtifactId() + "/" );
        content = IOUtil.toString( url.openStream() );
        assertThat( content, containsString( gav.getVersion() ) );

        url = new URL( url.toString() + gav.getVersion() + "/" );
        content = IOUtil.toString( url.openStream() );
        assertThat( content, containsString( gav.getClassifier() ) );
    }

    private void checkRepoBrowse( Gav gav )
        throws Exception
    {
        ContentListMessageUtil contentUtil =
            new ContentListMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );

        List<ContentListResource> result = contentUtil.getContentListResource( REPO_TEST_HARNESS_REPO, "/", false );

        ContentListResource g = select( result, gav.getGroupId() );

        result = contentUtil.getContentListResource( REPO_TEST_HARNESS_REPO, g.getRelativePath(), false );

        ContentListResource a = select( result, gav.getArtifactId() );

        result = contentUtil.getContentListResource( REPO_TEST_HARNESS_REPO, a.getRelativePath(), false );

        ContentListResource v = select( result, gav.getVersion() );

        result = contentUtil.getContentListResource( REPO_TEST_HARNESS_REPO, v.getRelativePath(), false );

        ContentListResource c =
            select( result,
                gav.getArtifactId() + "-" + gav.getVersion() + "-" + gav.getClassifier() + "." + gav.getExtension() );

        assertNotNull( c );
    }

    private ContentListResource select( List<ContentListResource> result, String text )
    {
        assertFalse( result.isEmpty() );
        ContentListResource g = null;
        for ( ContentListResource content : result )
        {
            if ( content.getText().equals( text ) )
            {
                g = content;
            }
        }
        assertNotNull( g );

        return g;
    }

    private void checkIndex( Gav gav )
        throws Exception
    {
        List<NexusArtifact> result =
            getSearchMessageUtil().searchForGav( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
                REPO_TEST_HARNESS_REPO );
        assertResult( gav, result );

        result =
            getSearchMessageUtil().searchForGav( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
                gav.getExtension(), gav.getClassifier(), REPO_TEST_HARNESS_REPO );
        assertResult( gav, result );
    }

    private void assertResult( Gav gav, List<NexusArtifact> result )
    {
        assertFalse( result.isEmpty() );

        assertThat( result.get( 0 ).getGroupId(), equalTo( gav.getGroupId() ) );
        assertThat( result.get( 0 ).getArtifactId(), equalTo( gav.getArtifactId() ) );
        assertThat( result.get( 0 ).getVersion(), equalTo( gav.getVersion() ) );
        assertThat( result.get( 0 ).getClassifier(), equalTo( gav.getClassifier() ) );
    }

    private void checkFileSystem( Gav gav )
        throws IOException
    {
        File artifact =
            new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_REPO + "/" + getRelitiveArtifactPath( gav ) );

        assertTrue( artifact.exists(), "File not found: " + artifact.getAbsolutePath() );
    }

    private void delete( Gav gav )
        throws IOException
    {
        assertTrue( deleteFromRepository( REPO_TEST_HARNESS_REPO, getRelitiveArtifactPath( gav ) ) );
    }

}
