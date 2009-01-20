/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.maven;

import java.io.InputStream;
import java.util.Map;

import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public interface MavenRepository
    extends Repository
{
    GavCalculator getGavCalculator();

    ArtifactPackagingMapper getArtifactPackagingMapper();

    RepositoryPolicy getRepositoryPolicy();

    void setRepositoryPolicy( RepositoryPolicy repositoryPolicy );

    boolean isCleanseRepositoryMetadata();

    void setCleanseRepositoryMetadata( boolean cleanseRepositoryMetadata );

    boolean isFixRepositoryChecksums();

    void setFixRepositoryChecksums( boolean fixRepositoryChecksums );

    MetadataManager getMetadataManager();

    boolean recreateMavenMetadata( String path );

    ChecksumPolicy getChecksumPolicy();

    void setChecksumPolicy( ChecksumPolicy checksumPolicy );

    int getReleaseMaxAge();

    void setReleaseMaxAge( int releaseMaxAge );

    int getSnapshotMaxAge();

    void setSnapshotMaxAge( int snapshotMaxAge );

    int getMetadataMaxAge();

    void setMetadataMaxAge( int metadataMaxAge );

    // == "Public API" (JSec protected)

    void storeItemWithChecksums( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException;

    void deleteItemWithChecksums( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException;

    // == "Insider API" (unprotected)

    void storeItemWithChecksums( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException;

    void deleteItemWithChecksums( RepositoryItemUid uid, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException;
}
