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
package org.sonatype.nexus.proxy.maven.maven2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.artifact.M2ArtifactRecognizer;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.router.AbstractRegistryDrivenRepositoryRouter;
import org.sonatype.nexus.proxy.router.DefaultGroupIdBasedRepositoryRouter;
import org.sonatype.nexus.proxy.router.GroupIdBasedRepositoryRouter;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

/**
 * Mavenized version of RepoGrouId based router. The only difference with the base class is the maven specific
 * aggregation. Since requests may hit multiple resources in groups with multiple repositories, this is the place where
 * we aggregate them. Aggregation happens for repository metadata only.
 * 
 * @author cstamas
 */
@Component( role = RepositoryRouter.class, hint = "groups-m2" )
public class M2GroupIdBasedRepositoryRouter
    extends GroupIdBasedRepositoryRouter
{
    /**
     * The ContentClass.
     */
    @Requirement( hint = "maven2" )
    private ContentClass contentClass;

    public ContentClass getHandledContentClass()
    {
        return contentClass;
    }

    /** Should the metadata be merged? */
    private boolean mergeMetadata = true;

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( ConfigurationChangeEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            super.onProximityEvent( evt );

            mergeMetadata = getApplicationConfiguration().getConfiguration().getRouting().getGroups().isMergeMetadata();
        }
    }

    /**
     * Checks if is merge metadata.
     * 
     * @return true, if is merge metadata
     */
    public boolean isMergeMetadata()
    {
        return mergeMetadata;
    }

    public void setMergeMetadata( boolean mergeMetadata )
    {
        this.mergeMetadata = mergeMetadata;
    }

    protected boolean shouldStopItemSearchOnFirstFoundFile( StorageItem item )
    {
        if ( isMergeMetadata() && M2ArtifactRecognizer.isMetadata( item.getPath() ) )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger()
                    .debug( "The item " + item.getPath() + " is metadata and mergeMetadata is true. Continuing." );
            }
            return false;
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "The item " + item.getPath() + " is not metadata or mergeMetadata is false. Stopping." );
            }
            return super.shouldStopItemSearchOnFirstFoundFile( item );
        }
    }

    protected StorageItem retrieveItemPostprocessor( ResourceStoreRequest request, List<StorageItem> listOfStorageItems )
        throws StorageException
    {
        if ( !M2ArtifactRecognizer.isMetadata( request.getRequestPath() ) || listOfStorageItems.size() == 1 )
        {
            // there is no need for Metadata aggregation, the result list contains only one item
            // or it is not metadata
            return super.retrieveItemPostprocessor( request, listOfStorageItems );
        }
        else
        {
            getLogger().debug( "Applying 'maven' postprocessing for metadatas and checksums." );
            InputStream spoofedContent = null;
            long spoofedLength = 0;

            // metadata aggregation
            if ( !M2ArtifactRecognizer.isChecksum( request.getRequestPath() ) )
            {
                MetadataXpp3Reader metadataReader = new MetadataXpp3Reader();
                MetadataXpp3Writer metadataWriter = new MetadataXpp3Writer();
                InputStreamReader isr = null;

                Metadata mergedMetadata = null;

                // Reversing the result list, so that the most authoritative result
                // will provide fields like lastVersiion
                Collections.reverse( listOfStorageItems );

                for ( StorageItem currentItem : listOfStorageItems )
                {
                    try
                    {
                        DefaultStorageFileItem currentFileItem = (DefaultStorageFileItem) currentItem;
                        isr = new InputStreamReader( currentFileItem.getInputStream() );
                        Metadata imd = metadataReader.read( isr );
                        if ( mergedMetadata == null )
                        {
                            mergedMetadata = imd;
                        }
                        else
                        {
                            mergedMetadata.merge( imd );
                        }
                    }
                    catch ( Exception ex )
                    {
                        getLogger().warn(
                            "Got Exception during merge of M2 metadata: " + currentItem.getRepositoryItemUid(),
                            ex );
                    }
                    finally
                    {
                        if ( isr != null )
                        {
                            try
                            {
                                isr.close();
                            }
                            catch ( IOException e )
                            {
                                getLogger().warn( "Got IO exception during close of InputStream.", e );
                            }
                        }
                    }
                }

                try
                {
                    // we are not saving the merged metadata, just calculating
                    // correct
                    // checksum for later retrieval
                    // and sending it back with in-memory stream
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    MessageDigest md5alg = MessageDigest.getInstance( "md5" );
                    MessageDigest sha1alg = MessageDigest.getInstance( "sha1" );
                    OutputStreamWriter osw = new OutputStreamWriter( new DigestOutputStream( new DigestOutputStream(
                        bos,
                        md5alg ), sha1alg ) );
                    metadataWriter.write( osw, mergedMetadata );
                    osw.flush();
                    osw.close();
                    storeDigest( request, md5alg );
                    storeDigest( request, sha1alg );
                    ByteArrayInputStream is = new ByteArrayInputStream( bos.toByteArray() );

                    spoofedContent = is;
                    spoofedLength = bos.size();

                    getLogger().debug(
                        "Item for path " + request.getRequestPath() + " merged from "
                            + Integer.toString( listOfStorageItems.size() ) + " found items." );
                }
                catch ( NoSuchAlgorithmException ex )
                {
                    // ignore
                }
                catch ( IOException ex )
                {
                    throw new StorageException( "Got IOException during M2 metadata merging.", ex );
                }
            }
            else
            {
                // it is checksum. We must suppose, that M2 requested first the
                // metadata itself and we have already calculated and stored it
                File tmpFile = new File( getApplicationConfiguration().getTemporaryDirectory(), request
                    .getRequestPath().replace( RepositoryItemUid.PATH_SEPARATOR.charAt( 0 ), '_' ) );
                try
                {
                    spoofedContent = new FileInputStream( tmpFile );
                    spoofedLength = tmpFile.length();

                    getLogger().info(
                        "Item for path " + request.getRequestPath() + " SPOOFED with merged metadata checksum." );
                }
                catch ( FileNotFoundException ex )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug(
                            "Item for path " + request.getRequestPath() + " SPOOFED with first got from repo group." );
                    }

                }
            }
            StorageFileItem item = (StorageFileItem) listOfStorageItems.get( 0 );

            DefaultStorageFileItem result = new DefaultStorageFileItem(
                item.getRepositoryItemUid().getRepository(),
                item.getPath(),
                item.isReadable(),
                item.isWritable(),
                new PreparedContentLocator( spoofedContent ) );
            result.getItemContext().putAll( request.getRequestContext() );
            result.getAttributes().putAll( item.getAttributes() );
            result.setLength( spoofedLength );
            result.setCreated( System.currentTimeMillis() );
            result.setModified( result.getCreated() );
            result.setMimeType( item.getMimeType() );
            result.setRemoteUrl( null );
            result.setRepositoryId( null );

            return result;
        }

    }

    protected void storeDigest( ResourceStoreRequest request, MessageDigest digest )
        throws IOException
    {
        File tmpFile = new File( getApplicationConfiguration().getTemporaryDirectory(), request
            .getRequestPath().replace( RepositoryItemUid.PATH_SEPARATOR.charAt( 0 ), '_' )
            + "." + digest.getAlgorithm().toLowerCase() );

        tmpFile.deleteOnExit();

        FileWriter fw = null;

        try
        {
            fw = new FileWriter( tmpFile );

            fw.write( new String( Hex.encodeHex( digest.digest() ) ) + "\n" );

            fw.flush();
        }
        finally
        {
            fw.close();
        }
    }

    public String getId()
    {
        return DefaultGroupIdBasedRepositoryRouter.ID;
    }



}
