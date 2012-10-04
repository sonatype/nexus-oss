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
package org.sonatype.nexus.plugins.yum.plugin.client.subsystem.impl;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.core.subsystem.repository.GenericRepository;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.client.core.subsystem.repository.Repository;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.plugins.yum.plugin.client.subsystem.CompressionAdapter;
import org.sonatype.nexus.plugins.yum.plugin.client.subsystem.CompressionType;
import org.sonatype.nexus.plugins.yum.plugin.client.subsystem.MetadataType;
import org.sonatype.nexus.plugins.yum.plugin.client.subsystem.YumClient;
import org.sonatype.nexus.rest.model.RepositoryResource;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

public class JerseyYumClient
    extends SubsystemSupport<JerseyNexusClient>
    implements YumClient
{

    private final Repositories repositories;

    public JerseyYumClient( JerseyNexusClient nexusClient, Repositories repositories )
    {
        super( nexusClient );
        this.repositories = repositories;
    }

    @Override
    public String getAliasVersion( String repositoryId, String alias )
    {
        return getNexusClient().serviceResource( getUrlPath( repositoryId, alias ) ).get( String.class );
    }

    @Override
    public void createOrUpdateAlias( String repositoryId, String alias, String version )
    {
        getNexusClient().serviceResource( getUrlPath( repositoryId, alias ) ).type( TEXT_PLAIN ).post( String.class,
            version );
    }

    @Override
    public <T> T getMetadata( String repositoryId, MetadataType metadataType, Class<T> returnType )
    {
        final Repository<GenericRepository, RepositoryResource> repo = repositories.get( repositoryId );
        final String url = repo.settings().getContentResourceURI() + metadataType.getPath();
        final ClientResponse clientResponse = getNexusClient().getClient().resource( url ).get( ClientResponse.class );
        return handleResponse( clientResponse, returnType, metadataType.getCompression() );
    }

    @Override
    public <T> T getMetadata( String repositoryId, String version, MetadataType metadataType, Class<T> returnType )
    {
        final String url = "yum/repos/" + repositoryId + "/" + version + metadataType.getPath();
        final ClientResponse clientResponse = getNexusClient().serviceResource( url ).get( ClientResponse.class );
        return handleResponse( clientResponse, returnType, metadataType.getCompression() );
    }

    private <T> T handleResponse( final ClientResponse clientResponse, Class<T> returnType, CompressionType compression )
    {
        try
        {
            if ( clientResponse.getStatus() < 300 )
            {
                try
                {
                    clientResponse.setEntityInputStream( new CompressionAdapter( compression ).adapt( clientResponse.getEntityInputStream() ) );
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( "Could not decompress response.", e );
                }
                return clientResponse.getEntity( returnType );
            }
            throw new UniformInterfaceException( "Could not get yum metatdata. Status: " + clientResponse.getStatus(),
                clientResponse );
        }
        finally
        {
            clientResponse.close();
        }
    }

    private String getUrlPath( String repositoryId, String alias )
    {
        return format( "yum/alias/%s/%s", encodeUtf8( repositoryId ), encodeUtf8( alias ) );
    }

    private static String encodeUtf8( String string )
    {
        try
        {
            return URLEncoder.encode( string, "UTF-8" );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new IllegalArgumentException( "Could not utf8-encode string : " + string, e );
        }
    }

}
