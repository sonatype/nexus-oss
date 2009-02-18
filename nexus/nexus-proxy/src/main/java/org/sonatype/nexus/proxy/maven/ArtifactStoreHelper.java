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

import java.io.ByteArrayInputStream;
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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
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
{
    private final MavenRepository repository;

    public ArtifactStoreHelper( MavenRepository repo )
    {
        super();

        this.repository = repo;
    }

    public MavenRepository getMavenRepository()
    {
        return repository;
    }

    public void storeItemWithChecksums( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        try
        {
            try
            {
                getMavenRepository().storeItem( request, is, userAttributes );
            }
            catch ( IOException e )
            {
                throw new StorageException( "Could not get the content from the ContentLocator!", e );
            }

            RepositoryItemUid itemUid = getMavenRepository().createUid( request.getRequestPath() );

            StorageFileItem storedFile = (StorageFileItem) getMavenRepository().retrieveItem( itemUid, null );

            String sha1Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );

            String md5Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY );

            if ( !StringUtils.isEmpty( sha1Hash ) )
            {
                getMavenRepository().storeItem(
                    new DefaultStorageFileItem(
                        getMavenRepository(),
                        storedFile.getPath() + ".sha1",
                        true,
                        true,
                        new StringContentLocator( sha1Hash ) ) );
            }

            if ( !StringUtils.isEmpty( md5Hash ) )
            {
                getMavenRepository().storeItem(
                    new DefaultStorageFileItem(
                        getMavenRepository(),
                        storedFile.getPath() + ".md5",
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

    public void deleteItemWithChecksums( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        try
        {
            getMavenRepository().deleteItem( request );
        }
        catch ( ItemNotFoundException e )
        {
            if ( request.getRequestPath().endsWith( ".asc" ) )
            {
                // Do nothing no guarantee that the .asc files will exist
            }
            else
            {
                throw e;
            }
        }

        String originalPath = request.getRequestPath();

        request.setRequestPath( originalPath + ".sha1" );

        try
        {
            getMavenRepository().deleteItem( request );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }

        request.setRequestPath( originalPath + ".md5" );

        try
        {
            getMavenRepository().deleteItem( request );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }

        // Now remove the .asc files, and the checksums stored with them as well
        // Note this is a recursive call, hence the check for .asc
        if ( !originalPath.endsWith( ".asc" ) )
        {
            request.setRequestPath( originalPath + ".asc" );

            deleteItemWithChecksums( request );
        }
    }

    public void storeItemWithChecksums( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException
    {
        try
        {
            try
            {
                getMavenRepository().storeItem( item );
            }
            catch ( IOException e )
            {
                throw new StorageException( "Could not get the content from the ContentLocator!", e );
            }

            StorageFileItem storedFile = (StorageFileItem) getMavenRepository().retrieveItem(
                item.getRepositoryItemUid(),
                item.getItemContext() );

            String sha1Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );

            String md5Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY );

            if ( !StringUtils.isEmpty( sha1Hash ) )
            {
                getMavenRepository().storeItem(
                    new DefaultStorageFileItem(
                        getMavenRepository(),
                        item.getPath() + ".sha1",
                        true,
                        true,
                        new StringContentLocator( sha1Hash ) ) );
            }

            if ( !StringUtils.isEmpty( md5Hash ) )
            {
                getMavenRepository().storeItem(
                    new DefaultStorageFileItem(
                        getMavenRepository(),
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

    public void deleteItemWithChecksums( RepositoryItemUid uid, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        try
        {
            getMavenRepository().deleteItem( uid, context );
        }
        catch ( ItemNotFoundException e )
        {
            if ( uid.getPath().endsWith( ".asc" ) )
            {
                // Do nothing no guarantee that the .asc files will exist
            }
            else
            {
                throw e;
            }
        }

        RepositoryItemUid sha1Uid = getMavenRepository().createUid( uid.getPath() + ".sha1" );

        try
        {
            getMavenRepository().deleteItem( sha1Uid, context );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }

        RepositoryItemUid md5Uid = getMavenRepository().createUid( uid.getPath() + ".md5" );

        try
        {
            getMavenRepository().deleteItem( md5Uid, context );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }

        // Now remove the .asc files, and the checksums stored with them as well
        // Note this is a recursive call, hence the check for .asc
        if ( !uid.getPath().endsWith( ".asc" ) )
        {
            deleteItemWithChecksums( getMavenRepository().createUid( uid.getPath() + ".asc" ), context );
        }
    }

    public StorageFileItem retrieveArtifactPom( ArtifactStoreRequest gavRequest )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        gavRequest.setClassifier( null );

        gavRequest.setPackaging( "pom" );

        return retrieveArtifact( gavRequest );
    }

    public StorageFileItem retrieveArtifact( ArtifactStoreRequest gavRequest )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        try
        {
            Gav gav = repository.getMetadataManager().resolveArtifact( repository, gavRequest );

            if ( gav == null )
            {
                throw new ItemNotFoundException( "GAV: " + gavRequest.getGroupId() + " : " + gavRequest.getArtifactId()
                    + " : " + gavRequest.getVersion() );
            }

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
            IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        Gav gav = new Gav( gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest
            .getClassifier(), "pom", null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository
            .getRepositoryPolicy() ), false, null, false, null );

        gavRequest.setRequestPath( repository.getGavCalculator().gavToPath( gav ) );

        repository.storeItemWithChecksums( gavRequest, is, attributes );

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
            IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        if ( gavRequest.getPackaging() == null )
        {
            throw new IllegalArgumentException( "Cannot generate POM without valid 'packaging'!" );
        }

        Gav gav = new Gav(
            gavRequest.getGroupId(),
            gavRequest.getArtifactId(),
            gavRequest.getVersion(),
            gavRequest.getClassifier(),
            gavRequest.getExtension() != null ? gavRequest.getExtension() : repository
                .getArtifactPackagingMapper().getExtensionForPackaging( gavRequest.getPackaging() ),
            null,
            null,
            null,
            RepositoryPolicy.SNAPSHOT.equals( repository.getRepositoryPolicy() ),
            false,
            null,
            false,
            null );

        gavRequest.setRequestPath( repository.getGavCalculator().gavToPath( gav ) );

        repository.storeItemWithChecksums( gavRequest, is, attributes );
    }

    public void storeArtifactWithGeneratedPom( ArtifactStoreRequest gavRequest, InputStream is,
        Map<String, String> attributes )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        checkRequest( gavRequest );

        // Force classifier to null, as the pom shouldn't have a classifier
        Gav pomGav = new Gav(
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
            null,
            false,
            null );

        try
        {
            // check for POM existence
            repository.retrieveItem(
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

            repository.storeItemWithChecksums(
                gavRequest,
                new ByteArrayInputStream( sw.toString().getBytes() ),
                attributes );

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
            gavRequest.getExtension() != null ? gavRequest.getExtension() : repository
                .getArtifactPackagingMapper().getExtensionForPackaging( gavRequest.getPackaging() ),
            null,
            null,
            null,
            RepositoryPolicy.SNAPSHOT.equals( repository.getRepositoryPolicy() ),
            false,
            null,
            false,
            null );

        gavRequest.setRequestPath( repository.getGavCalculator().gavToPath( artifactGav ) );

        repository.storeItemWithChecksums( gavRequest, is, attributes );
    }

    public void deleteArtifactPom( ArtifactStoreRequest gavRequest, boolean withChecksums, boolean withAllSubordinates,
        boolean deleteWholeGav )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
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
            IllegalOperationException,
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
            IllegalOperationException,
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
                repository.deleteItemWithChecksums( gavRequest );
            }
            else
            {
                repository.deleteItem( gavRequest );
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
            IllegalOperationException,
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
            IllegalOperationException,
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

    protected String getPackagingFromPom( String requestPath )
        throws IOException,
            XmlPullParserException,
            IllegalOperationException,
            ItemNotFoundException
    {
        String packaging = "jar";

        RepositoryItemUid uid = repository.createUid( requestPath );

        Reader reader = null;

        try
        {
            repository.retrieveItem( uid, null );

            // reader = ReaderFactory.newXmlReader( repository.retrieveItemContent( uid ) );

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
