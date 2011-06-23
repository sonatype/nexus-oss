package org.sonatype.nexus.plugins.p2.repository.its;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGeneratorConfiguration;
import org.sonatype.nexus.plugins.p2.repository.internal.capabilities.P2MetadataGeneratorCapability;
import org.sonatype.nexus.test.utils.CapabilitiesMessageUtil;

public abstract class AbstractP2MetadataGeneratorIT
    extends AbstractNexusIntegrationTest
{

    public AbstractP2MetadataGeneratorIT()
    {
    }

    public AbstractP2MetadataGeneratorIT( final String repoId )
    {
        super( repoId );
    }

    protected void createP2MetadataGeneratorCapability()
        throws Exception
    {
        final CapabilityResource capability = new CapabilityResource();
        capability.setName( AbstractP2MetadataGeneratorIT.class.getName() );
        capability.setTypeId( P2MetadataGeneratorCapability.ID );

        final CapabilityPropertyResource repoProp = new CapabilityPropertyResource();
        repoProp.setKey( P2MetadataGeneratorConfiguration.REPO_OR_GROUP_ID );
        repoProp.setValue( getTestRepositoryId() );

        capability.addProperty( repoProp );

        CapabilitiesMessageUtil.create( capability );
    }

    protected void deployArtifact( final String repoId, final File fileToDeploy, final String path )
        throws Exception
    {
        final String deployUrl = getNexusTestRepoUrl( repoId );
        final String deployUrlProtocol = deployUrl.substring( 0, deployUrl.indexOf( ":" ) );
        final String wagonHint = getWagonHintForDeployProtocol( deployUrlProtocol );
        getDeployUtils().deployWithWagon( wagonHint, deployUrl, fileToDeploy, path );
    }

    protected File downloadP2ArtifactsFor( final String groupId, final String artifactId, final String version )
        throws IOException
    {
        final File downloadDir = new File( "target/downloads/" + this.getClass().getSimpleName() );
        final File p2Artifacts =
            downloadArtifact( groupId, artifactId, version, "xml", "p2Artifacts", downloadDir.getPath() );
        return p2Artifacts;
    }

    protected File downloadP2ContentFor( final String groupId, final String artifactId, final String version )
        throws IOException
    {
        final File downloadDir = new File( "target/downloads/" + this.getClass().getSimpleName() );
        final File p2Content =
            downloadArtifact( groupId, artifactId, version, "xml", "p2Content", downloadDir.getPath() );
        return p2Content;
    }

    protected File storageP2ArtifactsFor( final String groupId, final String artifactId, final String version )
        throws IOException
    {
        final File p2Artifacts =
            new File( new File( nexusWorkDir ), "storage/" + getTestRepositoryId() + "/" + groupId + "/" + artifactId
                + "/" + version + "/" + artifactId + "-" + version + "-p2Artifacts.xml" );
        return p2Artifacts;
    }

    protected File storageP2ContentFor( final String groupId, final String artifactId, final String version )
        throws IOException
    {
        final File p2Artifacts =
            new File( new File( nexusWorkDir ), "storage/" + getTestRepositoryId() + "/" + groupId + "/" + artifactId
                + "/" + version + "/" + artifactId + "-" + version + "-p2Content.xml" );
        return p2Artifacts;
    }

}
