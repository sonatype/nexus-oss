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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.maven.mercury.artifact.version.VersionComparator;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.Versioning;
import org.apache.maven.mercury.util.TimeUtil;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.StoreWalker;

/**
 * @author Juven Xu
 */
public class RecreateMavenMetadataWalker
    extends StoreWalker
{

    private Repository repository;

    private boolean isHostedRepo;

    private static final String VERSION_REGEX = "^[0-9].*$";

    public RecreateMavenMetadataWalker( Repository repository, Logger logger )
    {
        super( repository, logger );

        this.repository = repository;

        isHostedRepo = RepositoryType.HOSTED.equals( getRepository().getRepositoryType() );
    }

    protected void processItem( StorageItem item )
    {
        // nothing here
    }

    protected void onCollectionEnter( StorageCollectionItem coll )
    {
        try
        {
            if ( shouldCreateMavenMetadata( coll ) )
            {
                createMavenMetadata( coll );
            }
        }
        catch ( Exception e )
        {

            getLogger().info( "Can't recreate maven metadata. ", e );
        }

    }

    public Repository getRepository()
    {
        return repository;
    }

    private boolean shouldCreateMavenMetadata( StorageCollectionItem coll )
        throws StorageException,
            AccessDeniedException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException
    {
        if ( !isHostedRepo )
        {
            return false;
        }
        for ( StorageItem item : coll.list() )
        {
            if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() )
                && item.getName().matches( VERSION_REGEX ) )
            {
                return true;
            }
        }

        return false;
    }

    private void createMavenMetadata( StorageCollectionItem coll )
        throws AccessDeniedException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException,
            IOException,
            MetadataException,
            UnsupportedStorageOperationException
    {
        getLogger().debug( "Creating maven-metadata.xml at: " + coll.getPath() );

        // UIDs are like URIs! The separator is _always_ "/"!!!
        RepositoryItemUid mdUid = repository.createUid( coll.getPath() + "/maven-metadata.xml" );

        Metadata md = new Metadata();

        md.setGroupId( coll.getParentPath().substring( 1 ).replace( '/', '.' ) );

        md.setArtifactId( coll.getName() );

        md.setVersioning( createVersioning( coll ) );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        ContentLocator contentLocator = new StringContentLocator( outputStream.toString() );

        DefaultStorageFileItem mdFile = new DefaultStorageFileItem(
            repository,
            mdUid.getPath(),
            true,
            true,
            contentLocator );

        repository.storeItem( mdFile );

        repository.removeFromNotFoundCache( mdUid.getPath() );

        outputStream.close();
    }

    private Versioning createVersioning( StorageCollectionItem coll )
        throws StorageException,
            AccessDeniedException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException
    {
        Versioning versioning = new Versioning();

        String release = null;

        String latest = null;

        VersionComparator versionComparator = new VersionComparator();

        for ( StorageItem item : coll.list() )
        {
            if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() )
                && item.getName().matches( VERSION_REGEX ) )
            {
                versioning.addVersion( item.getName() );

                if ( latest == null )
                {
                    latest = item.getName();

                    continue;
                }

                if ( release == null && !item.getName().endsWith( "SNAPSHOT" ) )
                {
                    release = item.getName();

                    continue;
                }

                if ( latest != null && versionComparator.compare( latest, item.getName() ) < 0 )
                {
                    latest = item.getName();
                }

                if ( release != null && !item.getName().endsWith( "SNAPSHOT" )
                    && versionComparator.compare( release, item.getName() ) < 0 )
                {
                    release = item.getName();
                }
            }
        }

        if ( release != null )
        {
            versioning.setRelease( release );
        }

        if ( latest != null )
        {
            versioning.setLatest( latest );
        }

        versioning.setLastUpdated( TimeUtil.getUTCTimestamp() );

        return versioning;
    }

}
