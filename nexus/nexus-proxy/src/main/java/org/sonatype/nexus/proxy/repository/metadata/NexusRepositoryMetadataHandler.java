package org.sonatype.nexus.proxy.repository.metadata;

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
