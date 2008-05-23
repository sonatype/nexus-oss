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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class ArtifactStoreHelper
    implements ArtifactStore
{
    private final MavenRepository repository;

    public ArtifactStoreHelper( MavenRepository repo )
    {
        super();

        this.repository = repo;
    }

    public void storeItemWithChecksums( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            StorageException
    {
        try
        {
            repository.storeItem( item );

            StorageFileItem storedFile = (StorageFileItem) repository.retrieveItem( true, item.getRepositoryItemUid() );

            String sha1Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );

            String md5Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY );

            if ( !StringUtils.isEmpty( sha1Hash ) )
            {
                repository.storeItem( new DefaultStorageFileItem(
                    repository,
                    item.getPath() + ".sha1",
                    true,
                    true,
                    new StringContentLocator( sha1Hash ) ) );
            }

            if ( !StringUtils.isEmpty( md5Hash ) )
            {
                repository.storeItem( new DefaultStorageFileItem(
                    repository,
                    item.getPath() + ".md5",
                    true,
                    true,
                    new StringContentLocator( md5Hash ) ) );
            }
        }
        catch ( ItemNotFoundException e )
        {
            throw new StorageException( "Storage inconsistency!", e );
        }
    }

    public StorageFileItem retrieveArtifactPom( GAVRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        Gav gav = new Gav(
            gavRequest.getGroupId(),
            gavRequest.getArtifactId(),
            gavRequest.getVersion(),
            null,
            "pom",
            null,
            null,
            null,
            RepositoryPolicy.SNAPSHOT.equals( repository.getRepositoryPolicy() ),
            false,
            null );

        RepositoryItemUid uid = new RepositoryItemUid( repository, repository.getGavCalculator().gavToPath( gav ) );

        StorageItem item = repository.retrieveItem( false, uid );

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            return (StorageFileItem) item;
        }
        else
        {
            throw new StorageException( "The POM retrieval returned non-file, path:" + uid.getPath() );
        }
    }

    public StorageFileItem retrieveArtifact( GAVRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        // TODO: packaging2extension mapping, now we default to JAR
        // or use POM to "find" the packaging
        Gav gav = new Gav( gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest
            .getClassifier(), repository.getArtifactPackagingMapper().getExtensionForPackaging(
            gavRequest.getPackaging() ), null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository
            .getRepositoryPolicy() ), false, null );

        RepositoryItemUid uid = new RepositoryItemUid( repository, repository.getGavCalculator().gavToPath( gav ) );

        StorageItem item = repository.retrieveItem( false, uid );

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            return (StorageFileItem) item;
        }
        else
        {
            throw new StorageException( "The Artifact retrieval returned non-file, path:" + uid.getPath() );
        }
    }

    public void storeArtifactPom( GAVRequest gavRequest, InputStream is )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        Gav gav = new Gav( gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest
            .getClassifier(), "pom", null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository
            .getRepositoryPolicy() ), false, null );

        DefaultStorageFileItem file = new DefaultStorageFileItem( repository, repository.getGavCalculator().gavToPath(
            gav ), true, true, new PreparedContentLocator( is ) );

        storeItemWithChecksums( file );
    }

    public void storeArtifact( GAVRequest gavRequest, InputStream is )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        if ( gavRequest.getPackaging() == null )
        {
            throw new IllegalArgumentException( "Cannot generate POM without valid 'packaging'!" );
        }

        Gav gav = new Gav( gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest
            .getClassifier(), repository.getArtifactPackagingMapper().getExtensionForPackaging(
            gavRequest.getPackaging() ), null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository
            .getRepositoryPolicy() ), false, null );

        DefaultStorageFileItem file = new DefaultStorageFileItem( repository, repository.getGavCalculator().gavToPath(
            gav ), true, true, new PreparedContentLocator( is ) );

        storeItemWithChecksums( file );
    }

    public void storeArtifactWithGeneratedPom( GAVRequest gavRequest, InputStream is )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        Gav pomGav = new Gav( gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest
            .getClassifier(), "pom", null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository
            .getRepositoryPolicy() ), false, null );

        try
        {
            // check for POM existence
            repository.retrieveItem( true, new RepositoryItemUid( repository, repository.getGavCalculator().gavToPath(
                pomGav ) ) );
        }
        catch ( ItemNotFoundException e )
        {
            if ( gavRequest.getPackaging() == null )
            {
                throw new IllegalArgumentException( "Cannot generate POM without valid 'packaging'!" );
            }

            // POM does not exists
            // generate minimal POM
            // got from install:install-file plugin/mojo, thanks
            Model model = new Model();
            model.setModelVersion( "4.0.0" );
            model.setGroupId( gavRequest.getGroupId() );
            model.setArtifactId( gavRequest.getArtifactId() );
            model.setVersion( gavRequest.getVersion() );
            model.setPackaging( gavRequest.getPackaging() );
            model.setDescription( "POM was created by Sonatype Nexus" );

            StringWriter sw = new StringWriter();

            MavenXpp3Writer mw = new MavenXpp3Writer();

            try
            {
                mw.write( sw, model );
            }
            catch ( IOException ex )
            {
                // writing to string, not to happen
            }

            storeItemWithChecksums( new DefaultStorageFileItem( repository, repository.getGavCalculator().gavToPath(
                pomGav ), true, true, new StringContentLocator( sw.toString() ) ) );
        }

        Gav artifactGav = new Gav(
            gavRequest.getGroupId(),
            gavRequest.getArtifactId(),
            gavRequest.getVersion(),
            gavRequest.getClassifier(),
            repository.getArtifactPackagingMapper().getExtensionForPackaging( gavRequest.getPackaging() ),
            null,
            null,
            null,
            RepositoryPolicy.SNAPSHOT.equals( repository.getRepositoryPolicy() ),
            false,
            null );

        storeItemWithChecksums( new DefaultStorageFileItem( repository, repository.getGavCalculator().gavToPath(
            artifactGav ), true, true, new PreparedContentLocator( is ) ) );
    }

    public void deleteArtifact( GAVRequest gavRequest, boolean withAllSubordinates )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        Gav gav = new Gav( gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest
            .getClassifier(), repository.getArtifactPackagingMapper().getExtensionForPackaging(
            gavRequest.getPackaging() ), null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository
            .getRepositoryPolicy() ), false, null );

        RepositoryItemUid uid = new RepositoryItemUid( repository, repository.getGavCalculator().gavToPath( gav ) );

        // TODO: implement other stuff too
        repository.deleteItem( uid );
    }

    public Collection<Gav> listArtifacts( GAVRequest gavRequest )
    {
        return Collections.emptyList();
    }

    // =======================================================================================

    protected void checkRequest( GAVRequest gavRequest )
    {
        if ( gavRequest.getGroupId() == null || gavRequest.getArtifactId() == null || gavRequest.getVersion() == null )
        {
            throw new IllegalArgumentException( "GAV is not supplied or only partially supplied! (G: '"
                + gavRequest.getGroupId() + "', A: '" + gavRequest.getArtifactId() + "', V: '"
                + gavRequest.getVersion() + "')" );
        }
    }

}
