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
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * Am ArtifactStore helper class, that simply drives a MavenRepository and gets various infos from it. It uses the
 * Repository interface of it's "owner" repository for storing/retrieval.
 * 
 * @author cstamas
 */
public class ArtifactStoreHelper
    implements ArtifactStore
{
    private final MavenRepository repository;

    public ArtifactStoreHelper( MavenRepository repo )
    {
        super();

        this.repository = repo;
    }

    protected void storeItemWithChecksums( ResourceStoreRequest request, ContentLocator locator,
        Map<String, String> attributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        repository.storeItemWithChecksums( new DefaultStorageFileItem(
            repository,
            request.getRequestPath(),
            true,
            true,
            locator ) );
    }

    protected RepositoryItemUid deleteWithChecksums( ArtifactStoreRequest request )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        RepositoryItemUid uid = repository.createUid( request.getRequestPath() );

        repository.deleteItemWithChecksums( uid, request.getRequestContext() );

        return uid;
    }

    protected RepositoryItemUid deleteWithoutChecksums( ArtifactStoreRequest request )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        RepositoryItemUid uid = repository.createUid( request.getRequestPath() );

        repository.deleteItem( uid, request.getRequestContext() );

        return uid;
    }

    public StorageFileItem retrieveArtifactPom( ArtifactStoreRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        gavRequest.setClassifier( null );

        gavRequest.setPackaging( "pom" );

        return retrieveArtifact( gavRequest );
    }

    public StorageFileItem retrieveArtifact( ArtifactStoreRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        try
        {
            Gav gav = repository.getMetadataManager().resolveArtifact( repository, gavRequest );

            gavRequest.setRequestPath( repository.getGavCalculator().gavToPath( gav ) );
        }
        catch ( IOException e )
        {
            throw new StorageException( "Could not maintain metadata!", e );
        }

        StorageItem item = repository.retrieveItem( gavRequest );

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            return (StorageFileItem) item;
        }
        else
        {
            throw new StorageException( "The Artifact retrieval returned non-file, path:" + gavRequest.getRequestPath() );
        }
    }

    public void storeArtifactPom( ArtifactStoreRequest gavRequest, InputStream is, Map<String, String> attributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        Gav gav = new Gav( gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest
            .getClassifier(), "pom", null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository
            .getRepositoryPolicy() ), false, null, false, null );

        gavRequest.setRequestPath( repository.getGavCalculator().gavToPath( gav ) );

        storeItemWithChecksums( gavRequest, new PreparedContentLocator( is ), attributes );

        try
        {
            repository.getMetadataManager().deployArtifact( gavRequest, repository );
        }
        catch ( IOException e )
        {
            throw new StorageException( "Could not maintain metadata!", e );
        }
    }

    public void storeArtifact( ArtifactStoreRequest gavRequest, InputStream is, Map<String, String> attributes )
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
            .getRepositoryPolicy() ), false, null, false, null );

        gavRequest.setRequestPath( repository.getGavCalculator().gavToPath( gav ) );

        storeItemWithChecksums( gavRequest, new PreparedContentLocator( is ), attributes );
    }

    public void storeArtifactWithGeneratedPom( ArtifactStoreRequest gavRequest, InputStream is,
        Map<String, String> attributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        Gav pomGav = new Gav( gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest
            .getClassifier(), "pom", null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository
            .getRepositoryPolicy() ), false, null, false, null );

        try
        {
            // check for POM existence
            repository.retrieveItem(
                true,
                repository.createUid( repository.getGavCalculator().gavToPath( pomGav ) ),
                gavRequest.getRequestContext() );
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

            gavRequest.setRequestPath( repository.getGavCalculator().gavToPath( pomGav ) );

            storeItemWithChecksums( gavRequest, new StringContentLocator( sw.toString() ), attributes );

            try
            {
                repository.getMetadataManager().deployArtifact( gavRequest, repository );
            }
            catch ( IOException ex )
            {
                throw new StorageException( "Could not maintain metadata!", ex );
            }

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
            null,
            false,
            null );

        gavRequest.setRequestPath( repository.getGavCalculator().gavToPath( artifactGav ) );

        storeItemWithChecksums( gavRequest, new PreparedContentLocator( is ), attributes );
    }

    public void deleteArtifactPom( ArtifactStoreRequest gavRequest, boolean withChecksums, boolean withAllSubordinates,
        boolean deleteWholeGav )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        // This is just so we can get teh gavToPath functionallity, to give us a path to work with
        Gav gav = new Gav( gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest
            .getClassifier(), "pom", null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository
            .getRepositoryPolicy() ), false, null, false, null );

        gavRequest.setRequestPath( repository.getGavCalculator().gavToPath( gav ) );
        /*
         * // First undeploy, we will read the pom contents to build the gav try { gav = new Gav(
         * gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest.getClassifier(),
         * getPackagingFromPom( gavRequest.getRequestPath() ), null, null, null, RepositoryPolicy.SNAPSHOT.equals(
         * repository.getRepositoryPolicy() ), false, null, false, null ); } catch ( IOException e ) { throw new
         * StorageException( "Could not read pom file!", e ); } catch ( XmlPullParserException e ) { throw new
         * StorageException( "Could not read pom file!", e ); } gavRequest.setRequestPath(
         * repository.getGavCalculator().gavToPath( gav ) ); // delete the pom's artifact handleDelete( gavRequest,
         * deleteWholeGav, withChecksums, withAllSubordinates ); // Now delete the pom gav = new Gav(
         * gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest .getClassifier(),
         * "pom", null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository .getRepositoryPolicy() ), false, null,
         * false, null ); gavRequest.setRequestPath( repository.getGavCalculator().gavToPath( gav ) );
         */

        handleDelete( gavRequest, deleteWholeGav, withChecksums, withAllSubordinates );
    }

    public void deleteArtifact( ArtifactStoreRequest gavRequest, boolean withChecksums, boolean withAllSubordinates,
        boolean deleteWholeGav )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        // delete the artifact
        Gav gav = new Gav( gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest
            .getClassifier(), repository.getArtifactPackagingMapper().getExtensionForPackaging(
            gavRequest.getPackaging() ), null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository
            .getRepositoryPolicy() ), false, null, false, null );

        gavRequest.setRequestPath( repository.getGavCalculator().gavToPath( gav ) );

        handleDelete( gavRequest, deleteWholeGav, withChecksums, withAllSubordinates );
    }

    private void handleDelete( ArtifactStoreRequest gavRequest, boolean deleteWholeGav, boolean withChecksums,
        boolean withAllSubordinates )
        throws StorageException,
            UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            AccessDeniedException,
            ItemNotFoundException
    {
        try
        {
            repository.getMetadataManager().undeployArtifact( gavRequest, repository );
        }
        catch ( IOException e )
        {
            throw new StorageException( "Could not maintain metadata!", e );
        }

        if ( deleteWholeGav )
        {
            deleteWholeGav( gavRequest );
        }
        else
        {
            if ( withChecksums )
            {
                deleteWithChecksums( gavRequest );
            }
            else
            {
                deleteWithoutChecksums( gavRequest );
            }

            if ( withAllSubordinates )
            {
                deleteAllSubordinates( gavRequest );
            }
        }
    }

    public Collection<Gav> listArtifacts( ArtifactStoreRequest gavRequest )
    {
        // TODO: implement this
        return Collections.emptyList();
    }

    // =======================================================================================

    protected void deleteAllSubordinates( ArtifactStoreRequest gavRequest )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        // delete all "below", meaning: classifiers of the GAV
        // watch for subdirs
        // delete dir if empty
        RepositoryItemUid parentCollUid = repository.createUid( gavRequest.getRequestPath().substring(
            0,
            gavRequest.getRequestPath().indexOf( RepositoryItemUid.PATH_SEPARATOR ) ) );

        try
        {
            // get the parent collection
            StorageCollectionItem parentColl = (StorageCollectionItem) repository.retrieveItem(
                true,
                parentCollUid,
                gavRequest.getRequestContext() );

            // list it
            Collection<StorageItem> items = repository.list( parentColl );

            boolean hadSubdirectoryOrOtherFiles = false;

            // and delete all except subdirs
            for ( StorageItem item : items )
            {
                if ( !StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
                {
                    Gav gav = repository.getGavCalculator().pathToGav( item.getPath() );

                    if ( gav != null && gavRequest.getGroupId().equals( gav.getGroupId() )
                        && gavRequest.getArtifactId().equals( gav.getArtifactId() )
                        && gavRequest.getVersion().equals( gav.getVersion() ) && gav.getClassifier() != null )
                    {
                        repository.deleteItem( item.getRepositoryItemUid(), gavRequest.getRequestContext() );
                    }
                    else if ( !item.getPath().endsWith( "maven-metadata.xml" ) )
                    {
                        hadSubdirectoryOrOtherFiles = true;
                    }
                }
                else
                {
                    hadSubdirectoryOrOtherFiles = true;
                }
            }

            if ( !hadSubdirectoryOrOtherFiles )
            {
                repository.deleteItem( parentCollUid, gavRequest.getRequestContext() );
            }
        }
        catch ( ItemNotFoundException e )
        {
            // silent
        }
    }

    protected void deleteWholeGav( ArtifactStoreRequest gavRequest )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        // delete all in this directory
        // watch for subdirs
        // delete dir if empty
        RepositoryItemUid parentCollUid = repository.createUid( gavRequest.getRequestPath().substring(
            0,
            gavRequest.getRequestPath().lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) ) );

        try
        {
            // get the parent collection
            StorageCollectionItem parentColl = (StorageCollectionItem) repository.retrieveItem(
                true,
                parentCollUid,
                gavRequest.getRequestContext() );

            // list it
            Collection<StorageItem> items = repository.list( parentColl );

            boolean hadSubdirectory = false;

            // and delete all except subdirs
            for ( StorageItem item : items )
            {
                if ( !StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
                {
                    repository.deleteItem( item.getRepositoryItemUid(), gavRequest.getRequestContext() );
                }
                else if ( !item.getPath().endsWith( "maven-metadata.xml" ) )
                {
                    hadSubdirectory = true;
                }
            }

            if ( !hadSubdirectory )
            {
                repository.deleteItem( parentCollUid, gavRequest.getRequestContext() );
            }
        }
        catch ( ItemNotFoundException e )
        {
            // silent
        }
    }

    protected void checkRequest( ArtifactStoreRequest gavRequest )
    {
        if ( gavRequest.getGroupId() == null || gavRequest.getArtifactId() == null || gavRequest.getVersion() == null )
        {
            throw new IllegalArgumentException( "GAV is not supplied or only partially supplied! (G: '"
                + gavRequest.getGroupId() + "', A: '" + gavRequest.getArtifactId() + "', V: '"
                + gavRequest.getVersion() + "')" );
        }
    }

    private String getPackagingFromPom( String requestPath )
        throws IOException,
            XmlPullParserException,
            RepositoryNotAvailableException,
            ItemNotFoundException
    {
        String packaging = "jar";

        RepositoryItemUid uid = repository.createUid( requestPath );

        Reader reader = null;

        try
        {
            reader = ReaderFactory.newXmlReader( repository.retrieveItemContent( uid ) );

            XmlPullParser parser = new MXParser();

            parser.setInput( reader );

            boolean foundRoot = false;

            int eventType = parser.getEventType();

            while ( eventType != XmlPullParser.END_DOCUMENT )
            {
                if ( eventType == XmlPullParser.START_TAG )
                {
                    if ( parser.getName().equals( "project" ) )
                    {
                        foundRoot = true;
                    }
                    else if ( parser.getName().equals( "packaging" ) )
                    {
                        // 1st: if found project/packaging -> overwrite
                        if ( parser.getDepth() == 2 )
                        {
                            packaging = StringUtils.trim( parser.nextText() );
                            break;
                        }
                    }
                    else if ( !foundRoot )
                    {
                        throw new XmlPullParserException( "Unrecognised tag: '" + parser.getName() + "'", parser, null );
                    }
                }

                eventType = parser.next();
            }
        }
        finally
        {
            IOUtil.close( reader );
        }

        return packaging;
    }
}
