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
package org.sonatype.nexus.repositories.metadata;

import java.io.IOException;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.repository.metadata.MetadataHandlerException;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;

public interface NexusRepositoryMetadataHandler
{
    /**
     * Will get the repository metadata from the passed remote repository root. If none found, null is returned.
     * 
     * @param url the repository root of the remote repository.
     * @return the metadata, or null if not found.
     * @throws MetadataHandlerException if some validation or other non-io problem occurs
     * @throws IOException if some IO problem occurs (except file not found).
     */
    RepositoryMetadata readRemoteRepositoryMetadata( String url )
        throws MetadataHandlerException,
            IOException;

    /**
     * Returns the Nexus repository metadata.
     * 
     * @param repositoryId
     * @return
     * @throws NoSuchRepositoryException
     * @throws MetadataHandlerException
     * @throws IOException
     */
    RepositoryMetadata readRepositoryMetadata( String repositoryId )
        throws NoSuchRepositoryException,
            MetadataHandlerException,
            IOException;

    /**
     * Writes/updates the Nexus repository metadata.
     * 
     * @param repositoryId
     * @param repositoryMetadata
     * @throws NoSuchRepositoryException
     * @throws MetadataHandlerException
     * @throws IOException
     */
    void writeRepositoryMetadata( String repositoryId, RepositoryMetadata repositoryMetadata )
        throws NoSuchRepositoryException,
            MetadataHandlerException,
            IOException;
}
