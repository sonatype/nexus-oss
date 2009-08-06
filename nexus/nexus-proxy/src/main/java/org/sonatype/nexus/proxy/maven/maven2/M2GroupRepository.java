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
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.maven.mercury.repository.metadata.MergeOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperand;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.M2ArtifactRecognizer;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageCompositeFileItem;
import org.sonatype.nexus.proxy.item.StorageCompositeFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenGroupRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

@Component( role = GroupRepository.class, hint = "maven2", instantiationStrategy = "per-lookup", description = "Maven2 Repository Group" )
public class M2GroupRepository
    extends AbstractMavenGroupRepository
{
    /**
     * The GAV Calculator.
     */
    @Requirement( hint = "maven2" )
    private GavCalculator gavCalculator;

    /**
     * Content class.
     */
    @Requirement( hint = "maven2" )
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
            try
            {
                if ( inputStream != null )
                {
                    inputStream.close();
                }
            }
            catch ( Exception e )
            {
            }
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

                existingMetadatas.add( parseMetadata( fileItem ) );
            }

            if ( existingMetadatas.isEmpty() )
            {
                throw new ItemNotFoundException( request, this );
            }

            Metadata result = new Metadata();

            List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

            for ( Metadata metadata : existingMetadatas )
            {
                ops.add( new MergeOperation( new MetadataOperand( metadata ) ) );
            }

            MetadataBuilder.changeMetadata( result, ops );

            // build the result item
            ByteArrayOutputStream resultOutputStream = new ByteArrayOutputStream();

            MetadataBuilder.write( result, resultOutputStream );

            StorageItem item = createMergedMetadataItem( request, resultOutputStream.toByteArray(), items );

            // build checksum files
            MessageDigest md5Digest = MessageDigest.getInstance( "md5" );

            MessageDigest sha1Digest = MessageDigest.getInstance( "sha1" );

            md5Digest.update( resultOutputStream.toByteArray() );

            sha1Digest.update( resultOutputStream.toByteArray() );

            storeDigest( request, md5Digest );

            storeDigest( request, sha1Digest );

            resultOutputStream.close();

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                                   "Item for path " + request.toString() + " merged from "
                                       + Integer.toString( items.size() ) + " found items." );
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

    protected void storeDigest( ResourceStoreRequest request, MessageDigest digest )
        throws IOException, UnsupportedStorageOperationException, IllegalOperationException
    {
        byte[] bytes = ( new String( Hex.encodeHex( digest.digest() ) ) + "\n" ).getBytes();

        ResourceStoreRequest req =
            new ResourceStoreRequest( request.getRequestPath() + "." + digest.getAlgorithm().toLowerCase() );

        req.getRequestContext().setParentContext( request.getRequestContext() );

        AbstractStorageItem item = createStorageItem( req, bytes );

        storeItem( false, item );
    }

    protected StorageCompositeFileItem createMergedMetadataItem( ResourceStoreRequest request, byte[] content,
                                                                 List<StorageItem> sources )
    {
        ContentLocator contentLocator = new ByteArrayContentLocator( content );

        DefaultStorageCompositeFileItem result =
            new DefaultStorageCompositeFileItem( this, request, true, false, contentLocator, sources );

        result.setMimeType( "text/plain" );

        result.setLength( content.length );

        result.getItemContext().put( CTX_TRANSITIVE_ITEM, Boolean.TRUE );

        return result;
    }
}
