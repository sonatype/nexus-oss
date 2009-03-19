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
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.M2ArtifactRecognizer;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenGroupRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.RepositoryConfigurator;
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
    protected M2GroupRepositoryConfiguration getExternalConfiguration()
    {
        return (M2GroupRepositoryConfiguration) super.getExternalConfiguration();
    }

    public boolean isMergeMetadata()
    {
        return getExternalConfiguration().isMergeMetadata();
    }

    public void setMergeMetadata( boolean mergeMetadata )
    {
        getExternalConfiguration().setMergeMetadata( mergeMetadata );
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
    public RepositoryConfigurator getRepositoryConfigurator()
    {
        return m2GroupRepositoryConfigurator;
    }

    @Override
    protected StorageItem doRetrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
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
     * Aggregates metadata from all member repositories
     */
    private StorageItem doRetrieveMetadata( ResourceStoreRequest request )
        throws StorageException,
            IllegalOperationException,
            UnsupportedStorageOperationException,
            ItemNotFoundException
    {
        List<StorageItem> listOfStorageItems = doRetrieveItems( request );

        if ( listOfStorageItems.isEmpty() )
        {
            // empty: not found
            throw new ItemNotFoundException( request, this );
        }

        if ( !isMergeMetadata() )
        {
            // not merging: return the 1st and ciao
            return listOfStorageItems.get( 0 );
        }

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
            catch ( XmlPullParserException ex )
            {
                getLogger().info(
                    "Got Exception during parsing of M2 metadata: " + currentItem.getRepositoryItemUid()
                        + ", skipping it!",
                    ex );
            }
            catch ( IOException ex )
            {
                throw new StorageException( "Got IOException during merge of Maven2 metadata, UID='"
                    + currentItem.getRepositoryItemUid() + "'", ex );
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

        if ( mergedMetadata == null )
        {
            // may happen if only one or all metadatas are unparseable
            throw new ItemNotFoundException( request, this );
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

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Item for path " + request.toString() + " merged from "
                        + Integer.toString( listOfStorageItems.size() ) + " found items." );
            }

            AbstractStorageItem item = createStorageItem( request, bos.toByteArray() );

            item.getItemContext().put( CTX_TRANSITIVE_ITEM, Boolean.TRUE );

            return item;
        }
        catch ( NoSuchAlgorithmException ex )
        {
            throw new StorageException( "Got NoSuchAlgorithmException during M2 metadata merging.", ex );
        }
        catch ( IOException ex )
        {
            throw new StorageException( "Got IOException during M2 metadata merging.", ex );
        }
    }

    protected void storeDigest( ResourceStoreRequest request, MessageDigest digest )
        throws IOException,
            UnsupportedStorageOperationException,
            IllegalOperationException
    {
        byte[] bytes = ( new String( Hex.encodeHex( digest.digest() ) ) + "\n" ).getBytes();

        ResourceStoreRequest req = new ResourceStoreRequest( request.getRequestPath() + "."
            + digest.getAlgorithm().toLowerCase() );

        req.getRequestContext().setParentContext( request.getRequestContext() );

        AbstractStorageItem item = createStorageItem( req, bytes );

        storeItem( false, item );
    }
}
