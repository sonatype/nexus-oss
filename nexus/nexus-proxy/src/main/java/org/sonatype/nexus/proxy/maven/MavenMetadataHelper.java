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
import java.io.IOException;

import org.apache.maven.mercury.artifact.version.VersionComparator;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.SetSnapshotOperation;
import org.apache.maven.mercury.repository.metadata.Snapshot;
import org.apache.maven.mercury.repository.metadata.SnapshotOperand;
import org.apache.maven.mercury.repository.metadata.Versioning;
import org.apache.maven.mercury.util.TimeUtil;
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
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.RecreateMavenMetadataWalker.PluginInfoForMetadata;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * 
 * @author Juven Xu
 *
 */
public class MavenMetadataHelper
{

    private RecreateMavenMetadataWalker walker;


    public MavenMetadataHelper( RecreateMavenMetadataWalker walker )
    {

        this.walker = walker;

    }

    public void createMetadataForArtifactDir( StorageCollectionItem coll )
        throws AccessDeniedException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException,
            MetadataException,
            IOException,
            UnsupportedStorageOperationException
    {

        // UIDs are like URIs! The separator is _always_ "/"!!!
        RepositoryItemUid mdUid = walker.getRepository().createUid( coll.getPath() + "/maven-metadata.xml" );

        Metadata md = new Metadata();

        md.setGroupId( walker.getCurrentGroupId() );

        md.setArtifactId( walker.getCurrentArtifactId() );

        createVersioningForRelease( md, coll );

        store( buildContentLocatorFromMd( md ), mdUid );
    }

    public void createMetadataForSnapshotVersionDir( StorageCollectionItem coll )
        throws NumberFormatException,
            MetadataException,
            AccessDeniedException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException,
            IOException,
            UnsupportedStorageOperationException
    {
        RepositoryItemUid mdUid = walker.getRepository().createUid( coll.getPath() + "/maven-metadata.xml" );

        Metadata md = new Metadata();

        md.setGroupId( walker.getCurrentGroupId() );

        md.setArtifactId( walker.getCurrentArtifactId() );

        md.setVersion( walker.getCurrentVersion() );

        createVersioningForSnapshot( md, coll );

        store( buildContentLocatorFromMd( md ), mdUid );
    }

    public void createMetadataForPluginGroupDir( StorageCollectionItem coll ) throws StorageException, UnsupportedStorageOperationException, RepositoryNotAvailableException
    {
        RepositoryItemUid mdUid = walker.getRepository().createUid( coll.getPath() + "/maven-metadata.xml" );

        StringBuffer md = new StringBuffer();

        md.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );

        md.append( "<metadata>\n" );

        md.append( "  <groupId>" + walker.getCurrentGroupId() + "</groupId>\n");
        
        md.append( "  <plugins>\n" );

        for ( PluginInfoForMetadata plugin : walker.getCurrentPlugins().values() )
        {
            md.append( plugin.toXml() );
        }

        md.append( "  </plugins>\n" );

        md.append( "</metadata>\n" );
        
        store( new StringContentLocator(md.toString()), mdUid );

    }

    private ContentLocator buildContentLocatorFromMd( Metadata md )
        throws MetadataException,
            IOException
    {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        ContentLocator contentLocator = new StringContentLocator( outputStream.toString() );

        outputStream.close();

        return contentLocator;
    }

    private void store( ContentLocator contentLocator, RepositoryItemUid mdUid )
        throws StorageException,
            UnsupportedStorageOperationException,
            RepositoryNotAvailableException

    {
        DefaultStorageFileItem mdFile = new DefaultStorageFileItem(
            walker.getRepository(),
            mdUid.getPath(),
            true,
            true,
            contentLocator );

        walker.getRepository().storeItem( mdFile );

        walker.getRepository().removeFromNotFoundCache( mdUid.getPath() );
    }

    private void createVersioningForRelease( Metadata md, StorageCollectionItem coll )
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
                && item.getName().matches( RecreateMavenMetadataWalker.VERSION_REGEX ) )
            {
                versioning.addVersion( item.getName() );

                if ( latest != null && versionComparator.compare( latest, item.getName() ) < 0 )
                {
                    latest = item.getName();
                }

                if ( release != null && !item.getName().endsWith( "SNAPSHOT" )
                    && versionComparator.compare( release, item.getName() ) < 0 )
                {
                    release = item.getName();
                }

                if ( latest == null )
                {
                    latest = item.getName();

                }

                if ( release == null && !item.getName().endsWith( "SNAPSHOT" ) )
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

        md.setVersioning( versioning );
    }

    private void createVersioningForSnapshot( Metadata md, StorageCollectionItem coll )
        throws MetadataException,
            NumberFormatException,
            StorageException,
            AccessDeniedException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException
    {
        Versioning versioning = new Versioning();

        versioning.setLastUpdated( TimeUtil.getUTCTimestamp() );

        md.setVersioning( versioning );

        Snapshot snapshot = new Snapshot();

        snapshot.setLocalCopy( false );

        snapshot.setBuildNumber( 1 );

        for ( StorageItem item : coll.list() )
        {
            if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) && item.getName().endsWith( "pom" ) )
            {
                int lastHyphenPos = item.getName().lastIndexOf( '-' );

                int buildNumber = Integer.parseInt( item.getName().substring(
                    lastHyphenPos + 1,
                    item.getName().length() - 4 ) );

                if ( buildNumber > snapshot.getBuildNumber() )
                {
                    snapshot.setBuildNumber( buildNumber );

                    String timeStamp = item.getName().substring(
                        ( md.getArtifactId() + '-' + md.getVersion() + '-' ).length() - "-SNAPSHOT".length(),
                        lastHyphenPos );

                    snapshot.setTimestamp( timeStamp );

                }
            }
        }

        MetadataBuilder.changeMetadata( md, new SetSnapshotOperation( new SnapshotOperand( snapshot ) ) );

    }
}
