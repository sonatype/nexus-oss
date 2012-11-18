package org.sonatype.nexus.repository.yum.testsuite;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.sonatype.nexus.repository.yum.client.MetadataType.PRIMARY_XML;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.inject.Inject;

import org.junit.Test;
import org.sonatype.nexus.client.core.subsystem.artifact.MavenArtifact;
import org.sonatype.nexus.client.core.subsystem.artifact.UploadRequest;
import org.sonatype.nexus.client.core.subsystem.repository.Repository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenProxyRepository;
import org.sonatype.sisu.filetasks.FileTaskBuilder;

public class ProxyRepositoryIT
    extends YumRepositoryITSupport
{

    private static final int PORT = 57654;

    private static final String ARTIFACT_ID = "test-artifact";

    @Inject
    private FileTaskBuilder overlays;

    private MavenHostedRepository hostedRepo;

    private MavenProxyRepository proxyRepo;

    public ProxyRepositoryIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void shouldUpdateProxyRepositories()
        throws Exception
    {
        setupRepositories();
        expectDownloadPrimaryXmlFailed( hostedRepo );
        expectDownloadPrimaryXmlFailed( proxyRepo );
        final MavenArtifact artifact = client().getSubsystem( MavenArtifact.class );
        artifact.upload( new UploadRequest( hostedRepo.id(), "group", ARTIFACT_ID, "version", "pom", "", "rpm",
                                            testData().resolveFile( "/rpms/test-artifact-1.2.3-1.noarch.rpm" ) ) );
        sleep( 5, SECONDS );
        String content = downloadPrimaryXml( hostedRepo );
        assertThat( content, containsString( ARTIFACT_ID ) );
        content = downloadPrimaryXml( proxyRepo );
        assertThat( content, containsString( ARTIFACT_ID ) );
        artifact.upload( new UploadRequest( hostedRepo.id(), "group2", ARTIFACT_ID, "version2", "pom", "", "rpm",
                                            testData().resolveFile( "/rpms/foo-bar-5.1.2-1.noarch.rpm" ) ) );
        sleep( 5, SECONDS );
        content = downloadPrimaryXml( proxyRepo );
        assertThat( content, containsString( "foo-bar" ) );
    }

    private void setupRepositories()
    {
        hostedRepo = repositories().create( MavenHostedRepository.class, repositoryIdForTest() + "_hosted" ).save();
        proxyRepo =
            repositories().create( MavenProxyRepository.class, repositoryIdForTest() + "_proxy" ).withArtifactMaxAge(
                0 ).withMetadataMaxAge( 0 ).asProxyOf( hostedRepo.contentUri() ).withNotFoundCacheTTL( 0 ).save();
    }

    private void expectDownloadPrimaryXmlFailed( Repository<?, ?> repo )
    {
        try
        {
            downloadPrimaryXml( repo );
            fail( "Could unexpected download primary xml" );
        }
        catch ( Exception e )
        {
        }
    }

    private String downloadPrimaryXml( Repository<?, ?> repo )
        throws IOException, MalformedURLException
    {
        return yum().getMetadata( repo.id(), PRIMARY_XML, String.class );
    }
}
