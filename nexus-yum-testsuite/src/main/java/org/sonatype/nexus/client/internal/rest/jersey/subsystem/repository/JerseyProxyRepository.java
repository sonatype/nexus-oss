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
package org.sonatype.nexus.client.internal.rest.jersey.subsystem.repository;

import org.sonatype.nexus.client.core.subsystem.repository.ProxyRepository;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.rest.model.NexusResponse;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;

public class JerseyProxyRepository
    extends JerseyRepositorySupport<ProxyRepository, RepositoryProxyResource>
    implements ProxyRepository
{

    public JerseyProxyRepository( final JerseyNexusClient nexusClient )
    {
        super( nexusClient );
    }

    @Override
    protected RepositoryProxyResource createSettings()
    {
        final RepositoryProxyResource settings = new RepositoryProxyResource();

        settings.setRepoType( "proxy" );
        settings.setProviderRole( "org.sonatype.nexus.proxy.repository.Repository" );
        settings.setExposed( true );
        settings.setWritePolicy( "READ_ONLY" );
        settings.setBrowseable( true );
        settings.setIndexable( true );
        settings.setNotFoundCacheTTL( 1440 );
        settings.setRepoPolicy( "RELEASE" );
        settings.setChecksumPolicy( "WARN" );
        settings.setDownloadRemoteIndexes( true );
        settings.setFileTypeValidation( true );
        settings.setArtifactMaxAge( -1 );
        settings.setMetadataMaxAge( 1440 );
        settings.setAutoBlockActive( true );

        return settings;
    }

    @Override
    public ProxyRepository withRepoPolicy( final String policy )
    {
        settings().setRepoPolicy( policy );
        return this;
    }

    @Override
    public ProxyRepository withRemoteUrl( final String remoteUrl )
    {
        RepositoryResourceRemoteStorage remoteStorage = settings().getRemoteStorage();
        if ( remoteStorage == null )
        {
            remoteStorage = new RepositoryResourceRemoteStorage();
        }
        remoteStorage.setRemoteStorageUrl( remoteUrl );
        return this;
    }

  @Override
  protected Class<? extends NexusResponse> getResponseClass() {
    return RepositoryResourceResponse.class;
  }

  @Override
  protected RepositoryProxyResource getData(Object response) {
    return (RepositoryProxyResource) ((RepositoryResourceResponse) response).getData();
  }

}
