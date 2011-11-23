/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its;

import static org.sonatype.nexus.plugins.p2.repository.P2Constants.ARTIFACTS_XML;
import static org.sonatype.nexus.plugins.p2.repository.P2Constants.P2_REPOSITORY_ROOT_PATH;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGenerator;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGeneratorConfiguration;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregator;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregatorConfiguration;
import org.sonatype.nexus.plugins.p2.repository.internal.capabilities.P2MetadataGeneratorCapability;
import org.sonatype.nexus.plugins.p2.repository.internal.capabilities.P2RepositoryAggregatorCapability;
import org.sonatype.nexus.test.utils.CapabilitiesMessageUtil;

public abstract class AbstractNexusP2GeneratorIT
    extends AbstractNexusP2IT
{

    private String p2RepositoryAggregatorCapabilityId;

    public AbstractNexusP2GeneratorIT( final String repoId )
    {
        super( repoId );
    }

    protected void createP2MetadataGeneratorCapability()
        throws Exception
    {
        final CapabilityResource capability = new CapabilityResource();
        capability.setDescription( P2MetadataGenerator.class.getName() );
        capability.setTypeId( P2MetadataGeneratorCapability.ID );

        final CapabilityPropertyResource repoProp = new CapabilityPropertyResource();
        repoProp.setKey( P2MetadataGeneratorConfiguration.REPO_OR_GROUP_ID );
        repoProp.setValue( getTestRepositoryId() );

        capability.addProperty( repoProp );

        CapabilitiesMessageUtil.create( capability );
    }

    protected void createP2RepositoryAggregatorCapability()
        throws Exception
    {
        final CapabilityResource capability = new CapabilityResource();
        capability.setDescription( P2RepositoryAggregator.class.getName() );
        capability.setTypeId( P2RepositoryAggregatorCapability.ID );

        final CapabilityPropertyResource repoProp = new CapabilityPropertyResource();
        repoProp.setKey( P2RepositoryAggregatorConfiguration.REPO_OR_GROUP_ID );
        repoProp.setValue( getTestRepositoryId() );

        capability.addProperty( repoProp );

        p2RepositoryAggregatorCapabilityId = CapabilitiesMessageUtil.create( capability ).getId();
    }

    protected void removeP2RepositoryAggregatorCapability()
        throws Exception
    {
        CapabilitiesMessageUtil.delete( p2RepositoryAggregatorCapabilityId );
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
            downloadArtifact( groupId, artifactId, version, "xml", "p2Artifacts", downloadDir.getCanonicalPath() );
        return p2Artifacts;
    }

    protected File downloadP2ContentFor( final String groupId, final String artifactId, final String version )
        throws IOException
    {
        final File downloadDir = new File( "target/downloads/" + this.getClass().getSimpleName() );
        final File p2Content =
            downloadArtifact( groupId, artifactId, version, "xml", "p2Content", downloadDir.getCanonicalPath() );
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

    protected File storageP2Repository()
        throws IOException
    {
        final File p2Repository =
            new File( new File( nexusWorkDir ), "storage/" + getTestRepositoryId() + P2_REPOSITORY_ROOT_PATH );
        return p2Repository;
    }

    protected File storageP2RepositoryArtifactsXML()
        throws IOException
    {
        final File p2Artifacts = new File( storageP2Repository(), ARTIFACTS_XML );
        return p2Artifacts;
    }

    protected File storageP2RepositoryContentXML()
        throws IOException
    {
        final File p2Content = new File( storageP2Repository(), P2Constants.CONTENT_XML );
        return p2Content;
    }
}
