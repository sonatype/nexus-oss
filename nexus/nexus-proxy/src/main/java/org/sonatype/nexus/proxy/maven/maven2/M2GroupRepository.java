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
package org.sonatype.nexus.proxy.maven.maven2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperand;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.NexusMergeOperation;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.M2ArtifactRecognizer;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageCompositeFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageCompositeFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenGroupRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

@Component( role = GroupRepository.class, hint = M2GroupRepository.ID, instantiationStrategy = "per-lookup", description = "Maven2 Repository Group" )
public class M2GroupRepository
    extends AbstractMavenGroupRepository
{
    /** This "mimics" the @Named("maven2") */
    public static final String ID = Maven2ContentClass.ID;

    /**
     * The GAV Calculator.
     */
    @Requirement( hint = "maven2" )
    private GavCalculator gavCalculator;

    /**
     * Content class.
     */
    @Requirement( hint = Maven2ContentClass.ID )
    private ContentClass contentClass;

    @Requirement
    private M2GroupRepositoryConfigurator m2GroupRepositoryConfigurator;

    @Override
    protected M2GroupRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (M2GroupRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<M2GroupRepositoryConfiguration> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<M2GroupRepositoryConfiguration>()
        {
            public M2GroupRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new M2GroupRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return m2GroupRepositoryConfigurator;
    }

    public boolean isMavenMetadataPath( String path )
    {
        return M2ArtifactRecognizer.isMetadata( path );
    }

    @Override
    protected StorageItem doRetrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        if ( M2ArtifactRecognizer.isMetadata( request.getRequestPath() )
            && !M2ArtifactRecognizer.isChecksum( request.getRequestPath() ) )
        {
            // metadata checksum files are calculated and cached as side-effect
            // of doRetrieveMetadata.

            try
            {
                return doRetrieveMetadata( request );
            }
            catch ( UnsupportedStorageOperationException e )
            {
                throw new StorageException( e );
            }
        }

        return super.doRetrieveItem( request );
    }

    /**
     * Parse a maven Metadata object from a storage file item
     */
    private Metadata parseMetadata( StorageFileItem fileItem )
        throws IOException, MetadataException
    {
        InputStream inputStream = null;

        try
        {
            inputStream = fileItem.getInputStream();

            return MetadataBuilder.read( inputStream );
        }
        finally
        {
            IOUtil.close( inputStream );
        }
    }

    /**
     * Aggregates metadata from all member repositories
     */
    private StorageItem doRetrieveMetadata( ResourceStoreRequest request )
        throws StorageException, IllegalOperationException, UnsupportedStorageOperationException, ItemNotFoundException
    {
        List<StorageItem> items = doRetrieveItems( request );

        if ( items.isEmpty() )
        {
            throw new ItemNotFoundException( request, this );
        }

        if ( !isMergeMetadata() )
        {
            // not merging: return the 1st and ciao
            return items.get( 0 );
        }

        List<Metadata> existingMetadatas = new ArrayList<Metadata>();

        try
        {
            for ( StorageItem item : items )
            {
                if ( !( item instanceof StorageFileItem ) )
                {
                    break;
                }

                StorageFileItem fileItem = (StorageFileItem) item;

                try
                {
                    existingMetadatas.add( parseMetadata( fileItem ) );
                }
                catch ( IOException e )
                {
                    getLogger().warn(
                        "IOException during parse of metadata UID=\"" + fileItem.getRepositoryItemUid().toString()
                            + "\", will be skipped from aggregation!", e );

                    getFeedRecorder().addNexusArtifactEvent(
                        newMetadataFailureEvent( fileItem,
                            "Invalid metadata served by repository. If repository is proxy, please check out what is it serving!" ) );
                }
                catch ( MetadataException e )
                {
                    getLogger().warn(
                        "Metadata exception during parse of metadata from UID=\""
                            + fileItem.getRepositoryItemUid().toString() + "\", will be skipped from aggregation!", e );

                    getFeedRecorder().addNexusArtifactEvent(
                        newMetadataFailureEvent( fileItem,
                            "Invalid metadata served by repository. If repository is proxy, please check out what is it serving!" ) );
                }
            }

            if ( existingMetadatas.isEmpty() )
            {
                throw new ItemNotFoundException( request, this );
            }

            Metadata result = existingMetadatas.get( 0 );

            // do a merge if necessary
            if ( existingMetadatas.size() > 1 )
            {
                List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

                for ( int i = 1; i < existingMetadatas.size(); i++ )
                {
                    ops.add( new NexusMergeOperation( new MetadataOperand( existingMetadatas.get( i ) ) ) );
                }

                MetadataBuilder.changeMetadata( result, ops );
            }

            // build the result item
            ByteArrayOutputStream resultOutputStream = new ByteArrayOutputStream();

            MetadataBuilder.write( result, resultOutputStream );

            StorageItem item = createMergedMetadataItem( request, resultOutputStream.toByteArray(), items );

            // build checksum files
            MessageDigest md5Digest = MessageDigest.getInstance( "md5" );

            MessageDigest sha1Digest = MessageDigest.getInstance( "sha1" );

            md5Digest.update( resultOutputStream.toByteArray() );

            sha1Digest.update( resultOutputStream.toByteArray() );

            storeMergedMetadataItemDigest( request, md5Digest, items );

            storeMergedMetadataItemDigest( request, sha1Digest, items );

            resultOutputStream.close();

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Item for path " + request.toString() + " merged from " + Integer.toString( items.size() )
                        + " found items." );
            }

            return item;

        }
        catch ( IOException e )
        {
            throw new StorageException( "Got IOException during M2 metadata merging.", e );
        }
        catch ( MetadataException e )
        {
            throw new StorageException( "Got MetadataException during M2 metadata merging.", e );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new StorageException( "Got NoSuchAlgorithmException during M2 metadata merging.", e );
        }
    }

    protected void storeMergedMetadataItemDigest( ResourceStoreRequest request, MessageDigest digest,
                                                  List<StorageItem> sources )
        throws IOException, UnsupportedStorageOperationException, IllegalOperationException
    {
        String digestFileName = request.getRequestPath() + "." + digest.getAlgorithm().toLowerCase();

        // see nexus-configuration mime-types.properties (defaulted to text/plain, as central reports them)
        String mimeType = getMimeUtil().getMimeType( digestFileName );

        byte[] bytes = ( new String( Hex.encodeHex( digest.digest() ) ) + "\n" ).getBytes( "UTF-8" );

        ContentLocator contentLocator = new ByteArrayContentLocator( bytes, mimeType );

        ResourceStoreRequest req = new ResourceStoreRequest( digestFileName );

        req.getRequestContext().setParentContext( request.getRequestContext() );

        // Metadata checksum files are not composite ones, they are derivatives of the Metadata (and metadata file _is_
        // composite one)
        DefaultStorageFileItem digestFileItem = new DefaultStorageFileItem( this, req, true, false, contentLocator );

        storeItem( false, digestFileItem );
    }

    protected StorageCompositeFileItem createMergedMetadataItem( ResourceStoreRequest request, byte[] content,
                                                                 List<StorageItem> sources )
    {
        // we are creating file maven-metadata.xml, and ask the MimeUtil for it's exact MIME type to honor potential
        // user configuration
        String mimeType = getMimeUtil().getMimeType( "maven-metadata.xml" );

        ContentLocator contentLocator = new ByteArrayContentLocator( content, mimeType );

        DefaultStorageCompositeFileItem result =
            new DefaultStorageCompositeFileItem( this, request, true, false, contentLocator, sources );

        result.setLength( content.length );

        result.setCreated( getNewestCreatedDate( sources ) );

        result.setModified( result.getCreated() );

        return result;
    }

    private long getNewestCreatedDate( List<StorageItem> sources )
    {
        long result = 0;

        for ( StorageItem source : sources )
        {
            result = Math.max( result, source.getCreated() );
        }

        return result;
    }

    // TODO: clean up this! This is a copy+paste from org.sonatype.nexus.proxy.maven.ChecksumContentValidator
    // centralize this!
    private NexusArtifactEvent newMetadataFailureEvent( StorageFileItem item, String msg )
    {
        NexusArtifactEvent nae = new NexusArtifactEvent();

        nae.setAction( NexusArtifactEvent.ACTION_BROKEN );

        nae.setEventDate( new Date() );

        nae.setEventContext( item.getItemContext() );

        nae.setMessage( msg );

        NexusItemInfo ai = new NexusItemInfo();

        ai.setPath( item.getPath() );

        ai.setRepositoryId( item.getRepositoryId() );

        ai.setRemoteUrl( item.getRemoteUrl() );

        nae.setNexusItemInfo( ai );

        return nae;
    }
}
