/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.client.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Callable;

import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.client.rest.jersey.JerseyUtils;
import org.sonatype.nexus.testsuite.client.LinkedItems;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Jersey based {@link LinkedItems} Nexus Client Subsystem implementation.
 *
 * @since 2.6
 */
public class JerseyLinkedItems
    extends SubsystemSupport<JerseyNexusClient>
    implements LinkedItems
{

    public JerseyLinkedItems( final JerseyNexusClient nexusClient )
    {
        super( nexusClient );
    }

    @Override
    public LinkedItems create( final String repositoryId,
                               final String path,
                               final String linkedPath )
    {
        return create( repositoryId, path, repositoryId, linkedPath );
    }

    @Override
    public LinkedItems create( final String repositoryId,
                               final String path,
                               final String linkedRepositoryId,
                               final String linkedPath )
    {
        checkNotNull( repositoryId, "repositoryId cannot be null" );
        checkNotNull( path, "path cannot be null" );
        checkNotNull( linkedRepositoryId, "linkedRepositoryId cannot be null" );
        checkNotNull( linkedPath, "linkedPath cannot be null" );

        JerseyUtils.handle(
            getNexusClient(),
            new Callable<ClientResponse>()
            {

                @Override
                public ClientResponse call()
                    throws Exception
                {
                    return getNexusClient()
                        .serviceResource( buildUrl( repositoryId, path ) )
                        .type( JerseyUtils.CONTENT_TYPE )
                        .accept( JerseyUtils.ACCEPTS )
                        .put( ClientResponse.class, new Link( linkedRepositoryId, linkedPath ) );
                }

            }
        );
        return this;
    }

    @Override
    public LinkedItems update( final String repositoryId,
                               final String path,
                               final String linkedPath )
    {
        return update( repositoryId, path, repositoryId, linkedPath );
    }

    @Override
    public LinkedItems update( final String repositoryId,
                               final String path,
                               final String linkedRepositoryId,
                               final String linkedPath )
    {
        checkNotNull( repositoryId, "repositoryId cannot be null" );
        checkNotNull( path, "path cannot be null" );
        checkNotNull( linkedRepositoryId, "linkedRepositoryId cannot be null" );
        checkNotNull( linkedPath, "linkedPath cannot be null" );

        JerseyUtils.handle(
            getNexusClient(),
            new Callable<ClientResponse>()
            {

                @Override
                public ClientResponse call()
                    throws Exception
                {
                    return getNexusClient()
                        .serviceResource( buildUrl( repositoryId, path ) )
                        .type( JerseyUtils.CONTENT_TYPE )
                        .accept( JerseyUtils.ACCEPTS )
                        .post( ClientResponse.class );
                }

            }
        );
        return this;
    }

    @Override
    public LinkedItems delete( final String repositoryId, final String path )
    {
        checkNotNull( repositoryId, "repositoryId cannot be null" );
        checkNotNull( path, "path cannot be null" );

        JerseyUtils.handle(
            getNexusClient(),
            new Callable<ClientResponse>()
            {

                @Override
                public ClientResponse call()
                    throws Exception
                {
                    return getNexusClient()
                        .serviceResource(
                            buildUrl( repositoryId, path )
                        )
                        .type( JerseyUtils.CONTENT_TYPE )
                        .accept( JerseyUtils.ACCEPTS )
                        .delete( ClientResponse.class );
                }

            }
        );
        return this;
    }

    private String buildUrl( final String repositoryId, final String path )
    {
        return "it/storage/link/" + repositoryId + "/" + path;
    }

    public static class Link
    {

        private String linkedRepositoryId;

        private String linkedPath;

        public Link()
        {
        }

        public Link( final String linkedRepositoryId,
                     final String linkedPath )
        {
            this.linkedRepositoryId = linkedRepositoryId;
            this.linkedPath = linkedPath;
        }

        public String getLinkedRepositoryId()
        {
            return linkedRepositoryId;
        }

        public void setLinkedRepositoryId( final String linkedRepositoryId )
        {
            this.linkedRepositoryId = linkedRepositoryId;
        }

        public String getLinkedPath()
        {
            return linkedPath;
        }

        public void setLinkedPath( final String linkedPath )
        {
            this.linkedPath = linkedPath;
        }
    }

}
