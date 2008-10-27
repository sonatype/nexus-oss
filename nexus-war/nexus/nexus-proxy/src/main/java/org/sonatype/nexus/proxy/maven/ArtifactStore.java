/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.maven;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * A specialized store for Maven Artifacts. This "face" if completely "maven aware", that means that doing operations
 * over this interface will maintain maven repo metadata too. This offers the "logical" view in contrary to the
 * Reposiory which offers the "low level" (file based) view on the same content.
 * 
 * @author cstamas
 */
public interface ArtifactStore
{
    /**
     * Retrieves the contents of the addressed POM.
     * 
     * @param gavRequest
     * @return
     * @throws NoSuchResourceStoreException
     * @throws RepositoryNotAvailableException
     * @throws ItemNotFoundException
     * @throws StorageException
     * @throws AccessDeniedException
     */
    StorageFileItem retrieveArtifactPom( ArtifactStoreRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Retrieves the contents of the addressed artifact.
     * 
     * @param gavRequest
     * @return
     * @throws NoSuchResourceStoreException
     * @throws RepositoryNotAvailableException
     * @throws ItemNotFoundException
     * @throws StorageException
     * @throws AccessDeniedException
     */
    StorageFileItem retrieveArtifact( ArtifactStoreRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Stores the artifacts POM contents, that is supplied as InputStream.
     * 
     * @param gavRequest
     * @param is
     * @throws UnsupportedStorageOperationException
     * @throws NoSuchResourceStoreException
     * @throws RepositoryNotAvailableException
     * @throws StorageException
     * @throws AccessDeniedException
     */
    void storeArtifactPom( ArtifactStoreRequest gavRequest, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException;

    /**
     * Stores the artifact contents, that is supplied as InputStream.
     * 
     * @param gavRequest
     * @param is
     * @throws UnsupportedStorageOperationException
     * @throws NoSuchResourceStoreException
     * @throws RepositoryNotAvailableException
     * @throws StorageException
     * @throws AccessDeniedException
     */
    void storeArtifact( ArtifactStoreRequest gavRequest, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException;

    /**
     * Stores the artifact contents, that is supplied as InputStream, and along with it generated a little POM based on
     * information in GAVRequest. If existing POM foind in repository, it will not be overwritten.
     * 
     * @param gavRequest
     * @param is
     * @throws UnsupportedStorageOperationException
     * @throws NoSuchResourceStoreException
     * @throws RepositoryNotAvailableException
     * @throws StorageException
     * @throws AccessDeniedException
     */
    void storeArtifactWithGeneratedPom( ArtifactStoreRequest gavRequest, InputStream is,
        Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException;

    /**
     * Deletes the addressed Artifact (with POM if exists). If it is addressed only up to GAV, and withAllSubordinates
     * is true, it will erase the artifact, and all it's "subordinates" (artifacts with any classfier). If
     * deleteWholeGav is true, the whole version will be deleted.
     * 
     * @param gavRequest request.
     * @param withChecksums if true, will delete all checksum (.sha1 & .md5) and signature (.asc) files associated
     *        with the artifact.
     * @param withAllSubordinates if true, will delete the all artifact and all of it's "subordinated" GAV with
     *        classifiers.
     * @param deleteWholeGav if true, will delete the whole version of this artifact.
     * @throws UnsupportedStorageOperationException
     * @throws NoSuchResourceStoreException
     * @throws RepositoryNotAvailableException
     * @throws ItemNotFoundException
     * @throws StorageException
     * @throws AccessDeniedException
     */
    void deleteArtifactPom( ArtifactStoreRequest gavRequest, boolean withChecksums, boolean withAllSubordinates, boolean deleteWholeGav )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Deletes the addressed Artifact (with POM if exists). If it is addressed only up to GAV, and withAllSubordinates
     * is true, it will erase the artifact, and all it's "subordinates" (artifacts with any classfier). If
     * deleteWholeGav is true, the whole version will be deleted.
     * 
     * @param gavRequest request.
     * @param withChecksums if true, will delete all checksum (.sha1 & .md5) and signature (.asc) files associated
     *        with the artifact.
     * @param withAllSubordinates if true, will delete the all artifact and all of it's "subordinated" GAV with
     *        classifiers.
     * @param deleteWholeGav if true, will delete the whole version of this artifact.
     * @throws UnsupportedStorageOperationException
     * @throws NoSuchResourceStoreException
     * @throws RepositoryNotAvailableException
     * @throws ItemNotFoundException
     * @throws StorageException
     * @throws AccessDeniedException
     */
    void deleteArtifact( ArtifactStoreRequest gavRequest, boolean withChecksums, boolean withAllSubordinates, boolean deleteWholeGav )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException;

    /**
     * Lists the Artifacts.
     * 
     * @param gavRequest
     * @return
     */
    Collection<Gav> listArtifacts( ArtifactStoreRequest gavRequest );
}
