package org.sonatype.nexus.plugins.yum.plugin;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.sonatype.nexus.plugins.yum.TimeUtil.sleep;
import static org.sonatype.nexus.plugins.yum.plugin.client.subsystem.MetadataType.PRIMARY_XML;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.subsystem.artifact.MavenArtifact;
import org.sonatype.nexus.client.core.subsystem.artifact.UploadRequest;
import org.sonatype.sisu.filetasks.FileTaskBuilder;

public class ProxyRepositoryIT
    extends AbstractIntegrationTestCase
{

    private static final int PORT = 57654;

    private static final String PRIMARY_XML_URL = "http://localhost:" + PORT + "/nexus/content/repositories/%s"
        + PRIMARY_XML.getPath();

    private static final String ARTIFACT_ID = "test-artifact";

    @Inject
    private FileTaskBuilder overlays;

    @Test
    public void shouldUpdateProxyRepositories()
        throws Exception
    {
        expectDownloadPrimaryXmlFailed( "releases" );
        expectDownloadPrimaryXmlFailed( "rpm-proxy" );
        final MavenArtifact artifact = client().getSubsystem( MavenArtifact.class );
        artifact.upload( new UploadRequest( "releases", "group", ARTIFACT_ID, "version", "pom", "", "rpm",
            testData( "rpm/test-artifact-1.2.3-1.noarch.rpm" ) ) );
        sleep( 5, SECONDS );
        String content = downloadPrimaryXml( "releases" );
        assertThat( content, containsString( ARTIFACT_ID ) );
        content = downloadPrimaryXml( "rpm-proxy" );
        assertThat( content, containsString( ARTIFACT_ID ) );
        artifact.upload( new UploadRequest( "releases", "group2", ARTIFACT_ID, "version2", "pom", "", "rpm",
            testData( "rpm/foo-bar-5.1.2-1.noarch.rpm" ) ) );
        sleep( 5, SECONDS );
        content = downloadPrimaryXml( "rpm-proxy" );
        assertThat( content, containsString( "foo-bar" ) );
    }

    private void expectDownloadPrimaryXmlFailed( String repoId )
    {
        try
        {
            downloadPrimaryXml( repoId );
            fail( "Could unexpected download primary xml" );
        }
        catch ( Exception e )
        {
        }
    }

    @Override
    protected NexusBundleConfiguration configureNexus( NexusBundleConfiguration configuration )
    {
        return super.configureNexus( configuration ).setPort( PORT ).addOverlays(
            overlays.copy().file( file( testData().resolveFile( "nexus-config/nexus-with-rpm-and-proxy-repo.xml" ) ) ).to().file(
                path( "sonatype-work/nexus/conf/nexus.xml" ) ) );
    }

    private String downloadPrimaryXml( String repoId )
        throws IOException, MalformedURLException
    {
        final HttpURLConnection conn =
            (HttpURLConnection) new URL( format( PRIMARY_XML_URL, repoId ) ).openConnection();
        try
        {
            if ( conn.getResponseCode() != SC_OK )
            {
                throw new IOException( "Wrong status code : " + conn.getResponseCode() );
            }

            return IOUtils.toString( new GZIPInputStream( conn.getInputStream() ) );
        }
        finally
        {
            conn.disconnect();
        }
    }
}
