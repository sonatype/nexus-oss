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

import java.util.Map;

import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public interface MavenRepository
    extends ArtifactStore, Repository
{
    GavCalculator getGavCalculator();

    ChecksumPolicy getChecksumPolicy();

    ArtifactPackagingMapper getArtifactPackagingMapper();

    void setChecksumPolicy( ChecksumPolicy checksumPolicy );

    RepositoryPolicy getRepositoryPolicy();

    void setRepositoryPolicy( RepositoryPolicy repositoryPolicy );

    int getReleaseMaxAge();

    void setReleaseMaxAge( int releaseMaxAge );

    int getSnapshotMaxAge();

    void setSnapshotMaxAge( int snapshotMaxAge );

    int getMetadataMaxAge();

    void setMetadataMaxAge( int metadataMaxAge );

    boolean isCleanseRepositoryMetadata();

    void setCleanseRepositoryMetadata( boolean cleanseRepositoryMetadata );

    boolean isFixRepositoryChecksums();

    void setFixRepositoryChecksums( boolean fixRepositoryChecksums );

    MetadataManager getMetadataManager();

    void storeItemWithChecksums( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            StorageException;

    void deleteItemWithChecksums( RepositoryItemUid uid, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException;
}
