/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.repositories.metadata;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.metadata.MetadataHandlerException;
import org.sonatype.nexus.repository.metadata.RepositoryMetadataHandler;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;
import org.sonatype.nexus.repository.metadata.restlet.RestletRawTransport;

@Component( role = NexusRepositoryMetadataHandler.class )
public class DefaultNexusRepositoryMetadataHandler
    extends AbstractLoggingComponent
    implements NexusRepositoryMetadataHandler
{
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private RepositoryMetadataHandler repositoryMetadataHandler;

    public RepositoryMetadata readRemoteRepositoryMetadata( String url )
        throws MetadataHandlerException,
            IOException
    {
        // TODO: honor global proxy? Current solution will neglect it
        RestletRawTransport restletRawTransport = new RestletRawTransport( url );

        return repositoryMetadataHandler.readRepositoryMetadata( restletRawTransport );
    }

    public RepositoryMetadata readRepositoryMetadata( String repositoryId )
        throws NoSuchRepositoryException,
            MetadataHandlerException,
            IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        NexusRawTransport nrt = new NexusRawTransport( repository, false, true );

        return repositoryMetadataHandler.readRepositoryMetadata( nrt );
    }

    public void writeRepositoryMetadata( String repositoryId, RepositoryMetadata repositoryMetadata )
        throws NoSuchRepositoryException,
            MetadataHandlerException,
            IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        NexusRawTransport nrt = new NexusRawTransport( repository, true, false );

        repositoryMetadataHandler.writeRepositoryMetadata( repositoryMetadata, nrt );
    }

}
