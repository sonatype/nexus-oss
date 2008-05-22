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

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * A specialized store for Maven Artifacts.
 * 
 * @author cstamas
 */
public interface ArtifactStore
{
    /**
     * Stores the item and also creates and stores it's Maven repository checksums.
     * 
     * @param item
     * @throws UnsupportedStorageOperationException
     * @throws RepositoryNotAvailableException
     * @throws StorageException
     */
    void storeItemWithChecksums( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            StorageException;

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
    StorageFileItem retrieveArtifactPom( GAVRequest gavRequest )
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
    StorageFileItem retrieveArtifact( GAVRequest gavRequest )
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
    void storeArtifactPom( GAVRequest gavRequest, InputStream is )
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
    void storeArtifact( GAVRequest gavRequest, InputStream is )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException;

    /**
     * Stores the artifact contents, that is supplied as InputStream, and along with it generated a little POM based on
     * information in GAVRequest.
     * 
     * @param gavRequest
     * @param is
     * @throws UnsupportedStorageOperationException
     * @throws NoSuchResourceStoreException
     * @throws RepositoryNotAvailableException
     * @throws StorageException
     * @throws AccessDeniedException
     */
    void storeArtifactWithGeneratedPom( GAVRequest gavRequest, InputStream is )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException;

    /**
     * Deletes the addressed Artifact (with POM if exists). If it is addressed only up to GAV, and withAllSubordinates
     * is true, it will erase the whole GAV.
     * 
     * @param gavRequest
     * @param withAllSubordinates
     * @throws UnsupportedStorageOperationException
     * @throws NoSuchResourceStoreException
     * @throws RepositoryNotAvailableException
     * @throws ItemNotFoundException
     * @throws StorageException
     * @throws AccessDeniedException
     */
    void deleteArtifact( GAVRequest gavRequest, boolean withAllSubordinates )
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
    Collection<Gav> listArtifacts( GAVRequest gavRequest );
}
