/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.client.rest.BaseUrl.baseUrlFrom;
import static org.sonatype.nexus.plugins.p2.repository.P2Constants.ARTIFACTS_XML;
import static org.sonatype.nexus.plugins.p2.repository.P2Constants.P2_REPOSITORY_ROOT_PATH;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.sonatype.nexus.capabilities.client.Capabilities;
import org.sonatype.nexus.capabilities.client.rest.JerseyCapabilitiesSubsystemFactory;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.rest.UsernamePasswordAuthenticationInfo;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClientFactory;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGenerator;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGeneratorConfiguration;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregator;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregatorConfiguration;
import org.sonatype.nexus.plugins.p2.repository.internal.capabilities.P2MetadataGeneratorCapabilityDescriptor;
import org.sonatype.nexus.plugins.p2.repository.internal.capabilities.P2RepositoryAggregatorCapabilityDescriptor;
import com.google.common.base.Throwables;

public abstract class AbstractNexusP2GeneratorIT
    extends AbstractNexusP2IT
{

    private String p2RepositoryAggregatorCapabilityId;

    private NexusClient nexusClient;

    public AbstractNexusP2GeneratorIT( final String repoId )
    {
        super( repoId );
    }

    protected NexusClient client()
    {
        if ( nexusClient == null )
        {
            try
            {
                nexusClient = new JerseyNexusClientFactory(
                    new JerseyCapabilitiesSubsystemFactory()
                ).createFor(
                    baseUrlFrom( nexusBaseUrl ),
                    new UsernamePasswordAuthenticationInfo( checkNotNull( "admin" ), checkNotNull( "admin123" ) )
                );
            }
            catch ( MalformedURLException e )
            {
                throw Throwables.propagate( e );
            }
        }
        return nexusClient;
    }

    public Capabilities capabilities()
    {
        return client().getSubsystem( Capabilities.class );
    }

    protected void createP2MetadataGeneratorCapability()
        throws Exception
    {
        final CapabilityResource capability = new CapabilityResource();
        capability.setNotes( P2MetadataGenerator.class.getName() );
        capability.setTypeId( P2MetadataGeneratorCapabilityDescriptor.TYPE_ID );

        final CapabilityPropertyResource repoProp = new CapabilityPropertyResource();
        repoProp.setKey( P2MetadataGeneratorConfiguration.REPOSITORY );
        repoProp.setValue( getTestRepositoryId() );

        capability.addProperty( repoProp );

        capabilities().add( capability );
    }

    protected void createP2RepositoryAggregatorCapability()
        throws Exception
    {
        final CapabilityResource capability = new CapabilityResource();
        capability.setNotes( P2RepositoryAggregator.class.getName() );
        capability.setTypeId( P2RepositoryAggregatorCapabilityDescriptor.TYPE_ID );

        final CapabilityPropertyResource repoProp = new CapabilityPropertyResource();
        repoProp.setKey( P2RepositoryAggregatorConfiguration.REPOSITORY );
        repoProp.setValue( getTestRepositoryId() );

        capability.addProperty( repoProp );

        p2RepositoryAggregatorCapabilityId = capabilities().add( capability ).getId();
    }

    protected void removeP2RepositoryAggregatorCapability()
        throws Exception
    {
        capabilities().delete( p2RepositoryAggregatorCapabilityId );
    }

    protected void passivateP2RepositoryAggregatorCapability()
        throws Exception
    {
        final CapabilityResource capabilityResource = capabilities().get( p2RepositoryAggregatorCapabilityId );
        capabilityResource.setEnabled( false );
        capabilities().update( capabilityResource );
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
