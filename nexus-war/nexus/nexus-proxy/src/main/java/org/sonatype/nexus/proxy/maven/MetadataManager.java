package org.sonatype.nexus.proxy.maven;

import java.io.IOException;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public interface MetadataManager
{
    String ROLE = MetadataManager.class.getName();

    /**
     * Calling this method updates the GAV and GA metadatas accordingly. It senses whether it is a snapshot or not.
     * 
     * @param req
     */
    void deployArtifact( ArtifactStoreRequest req, MavenRepository repository )
        throws RepositoryNotAvailableException,
            IOException,
            UnsupportedStorageOperationException;

    /**
     * Calling this method updates the GAV and GA metadatas accordingly. It senses whether it is a snapshot or not.
     * 
     * @param req
     */
    void undeployArtifact( ArtifactStoreRequest req, MavenRepository repository )
        throws RepositoryNotAvailableException,
            IOException,
            UnsupportedStorageOperationException;

    /**
     * Calling this method updates the plugin metadata.
     * 
     * @param req
     */
    void deployPlugin( ArtifactStoreRequest req, MavenRepository repository )
        throws RepositoryNotAvailableException,
            IOException,
            UnsupportedStorageOperationException;

    /**
     * Calling this method updates the plugin metadata.
     * 
     * @param req
     */
    void undeployPlugin( ArtifactStoreRequest req, MavenRepository repository )
        throws RepositoryNotAvailableException,
            IOException,
            UnsupportedStorageOperationException;

    /**
     * Resolves the artifact, honoring LATEST and RELEASE as version. In case of snapshots, it will try to resolve the
     * timestamped version too, if needed.
     * 
     * @return
     * @throws RepositoryNotAvailableException
     * @throws IOException
     */
    Gav resolveArtifact( MavenRepository repository, ArtifactStoreRequest gavRequest )
        throws RepositoryNotAvailableException,
            IOException;

    /**
     * Resolves the snapshot base version to a timestamped version if possible. So, when a repo is snapshot, and we have
     * GAV.
     * 
     * @return
     * @throws RepositoryNotAvailableException
     * @throws IOException
     */
    Gav resolveSnapshot( MavenRepository repository, ArtifactStoreRequest gavRequest, Gav gav )
        throws RepositoryNotAvailableException,
            IOException;
}
