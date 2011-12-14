/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.maven2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.maven.gav.M2ArtifactRecognizer;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.proxy.maven.metadata.operations.ModelVersionUtility;
import org.sonatype.nexus.proxy.maven.metadata.operations.ModelVersionUtility.Version;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.util.AlphanumComparator;
import org.sonatype.nexus.util.DigesterUtils;

/**
 * The default M2Repository.
 * 
 * @author cstamas
 */
@Component( role = Repository.class, hint = M2Repository.ID, instantiationStrategy = "per-lookup", description = "Maven2 Repository" )
public class M2Repository
    extends AbstractMavenRepository
{
    /** This "mimics" the @Named("maven2") */
    public static final String ID = Maven2ContentClass.ID;

    /**
     * The GAV Calculator.
     */
    @Requirement( hint = "maven2" )
    private GavCalculator gavCalculator;

    @Requirement( hint = Maven2ContentClass.ID )
    private ContentClass contentClass;

    @Requirement
    private M2RepositoryConfigurator m2RepositoryConfigurator;

    @Override
    protected M2RepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (M2RepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<M2RepositoryConfiguration>()
        {
            public M2RepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new M2RepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
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
        return m2RepositoryConfigurator;
    }

    @Override
    public boolean isMavenMetadataPath( String path )
    {
        return M2ArtifactRecognizer.isMetadata( path );
    }

    /**
     * Should serve by policies.
     * 
     * @param request the request
     * @return true, if successful
     */
    @Override
    public boolean shouldServeByPolicies( ResourceStoreRequest request )
    {
        if ( M2ArtifactRecognizer.isMetadata( request.getRequestPath() ) )
        {
            if ( M2ArtifactRecognizer.isSnapshot( request.getRequestPath() ) )
            {
                return RepositoryPolicy.SNAPSHOT.equals( getRepositoryPolicy() );
            }
            else
            {
                // metadatas goes always
                return true;
            }
        }

        // we are using Gav to test the path
        final Gav gav = getGavCalculator().pathToGav( request.getRequestPath() );

        if ( gav == null )
        {
            return true;
        }
        else
        {
            if ( gav.isSnapshot() )
            {
                // snapshots goes if enabled
                return RepositoryPolicy.SNAPSHOT.equals( getRepositoryPolicy() );
            }
            else
            {
                return RepositoryPolicy.RELEASE.equals( getRepositoryPolicy() );
            }
        }
    }

    @Override
    public AbstractStorageItem doCacheItem( AbstractStorageItem item )
        throws LocalStorageException
    {
        // if the item is file, is M2 repository metadata and this repo is release-only or snapshot-only
        if ( isCleanseRepositoryMetadata() && item instanceof StorageFileItem
            && M2ArtifactRecognizer.isMetadata( item.getPath() ) )
        {
            InputStream orig = null;
            StorageFileItem mdFile = (StorageFileItem) item;
            ByteArrayInputStream backup = null;
            ByteArrayOutputStream backup1 = new ByteArrayOutputStream();
            try
            {
                // remote item is not reusable, and we usually cache remote stuff locally
                try
                {
                    orig = mdFile.getInputStream();
                    IOUtil.copy( orig, backup1 );
                }
                finally
                {
                    IOUtil.close( orig );
                }
                backup = new ByteArrayInputStream( backup1.toByteArray() );

                // Metadata is small, let's do it in memory
                MetadataXpp3Reader metadataReader = new MetadataXpp3Reader();
                InputStreamReader isr = new InputStreamReader( backup );
                Metadata imd = metadataReader.read( isr );

                // and fix it
                imd = cleanseMetadataForRepository( RepositoryPolicy.SNAPSHOT.equals( getRepositoryPolicy() ), imd );

                // serialize and swap the new metadata
                MetadataXpp3Writer metadataWriter = new MetadataXpp3Writer();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter( bos );
                metadataWriter.write( osw, imd );
                mdFile.setContentLocator( new ByteArrayContentLocator( bos.toByteArray(), mdFile.getMimeType() ) );
            }
            catch ( Exception e )
            {
                getLogger().error( "Exception during repository metadata cleansing.", e );

                if ( backup != null )
                {
                    // get backup and continue operation
                    backup.reset();
                    mdFile.setContentLocator( new ByteArrayContentLocator( backup1.toByteArray(), mdFile.getMimeType() ) );
                }
            }
        }

        return super.doCacheItem( item );
    }

    @Override
    protected boolean isOld( StorageItem item )
    {
        if ( M2ArtifactRecognizer.isMetadata( item.getPath() ) )
        {
            return isOld( getMetadataMaxAge(), item );
        }
        if ( M2ArtifactRecognizer.isSnapshot( item.getPath() ) )
        {
            return isOld( getArtifactMaxAge(), item );
        }

        // we are using Gav to test the path
        final Gav gav = getGavCalculator().pathToGav( item.getPath() );

        if ( gav == null )
        {
            // this is not an artifact, it is just any "file"
            return super.isOld( item );
        }
        // it is a release
        return isOld( getArtifactMaxAge(), item );
    }

    protected Metadata cleanseMetadataForRepository( boolean snapshot, Metadata metadata )
    {
        // remove base versions not belonging here
        List<String> versions = metadata.getVersioning().getVersions();
        for ( Iterator<String> iversion = versions.iterator(); iversion.hasNext(); )
        {
            // if we need snapshots and the version is not snapshot, or
            // if we need releases and the version is snapshot
            if ( ( snapshot && !Gav.isSnapshot( iversion.next() ) )
                || ( !snapshot && Gav.isSnapshot( iversion.next() ) ) )
            {
                iversion.remove();
            }
        }

        metadata.getVersioning().setLatest( getLatestVersion( metadata.getVersioning().getVersions() ) );
        if ( snapshot )
        {
            metadata.getVersioning().setRelease( null );
        }
        else
        {
            metadata.getVersioning().setRelease( metadata.getVersioning().getLatest() );
        }
        return metadata;
    }

    public String getLatestVersion( List<String> versions )
    {
        Collections.sort( versions, new AlphanumComparator() );

        return versions.get( versions.size() - 1 );
    }

    @Override
    protected void enforceWritePolicy( ResourceStoreRequest request, Action action )
        throws IllegalRequestException
    {
        // allow updating of metadata
        // we also need to allow updating snapshots
        if ( !M2ArtifactRecognizer.isMetadata( request.getRequestPath() )
            && !M2ArtifactRecognizer.isSnapshot( request.getRequestPath() ) )
        {
            super.enforceWritePolicy( request, action );
        }
    }

    @Override
    protected StorageItem doRetrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        String userAgent = (String) request.getRequestContext().get( AccessManager.REQUEST_AGENT );

        if ( M2ArtifactRecognizer.isMetadata( request.getRequestPath() )
            && !ModelVersionUtility.LATEST_MODEL_VERSION.equals( getClientSupportedVersion( userAgent ) ) )
        {
            // metadata checksum files are calculated and cached as side-effect
            // of doRetrieveMetadata.
            final StorageFileItem mdItem;
            if ( M2ArtifactRecognizer.isChecksum( request.getRequestPath() ) )
            {
                String path = request.getRequestPath();
                if ( request.getRequestPath().endsWith( ".md5" ) )
                {
                    path = path.substring( 0, path.length() - 4 );
                }
                else if ( request.getRequestPath().endsWith( ".sha1" ) )
                {
                    path = path.substring( 0, path.length() - 5 );
                }
                ResourceStoreRequest mdRequest = new ResourceStoreRequest( path );
                mdRequest.getRequestContext().setParentContext( request.getRequestContext() );

                mdItem = (StorageFileItem) super.doRetrieveItem( mdRequest );
            }
            else
            {
                mdItem = (StorageFileItem) super.doRetrieveItem( request );
            }

            InputStream inputStream = null;
            try
            {
                inputStream = mdItem.getInputStream();

                Metadata metadata = MetadataBuilder.read( inputStream );
                Version requiredVersion = getClientSupportedVersion( userAgent );
                Version metadataVersion = ModelVersionUtility.getModelVersion( metadata );

                if ( requiredVersion == null || requiredVersion.equals( metadataVersion ) )
                {
                    return super.doRetrieveItem( request );
                }

                ModelVersionUtility.setModelVersion( metadata, requiredVersion );

                ByteArrayOutputStream mdOutput = new ByteArrayOutputStream();

                MetadataBuilder.write( metadata, mdOutput );

                final byte[] content;
                if ( M2ArtifactRecognizer.isChecksum( request.getRequestPath() ) )
                {
                    String digest;
                    if ( request.getRequestPath().endsWith( ".md5" ) )
                    {
                        digest = DigesterUtils.getMd5Digest( mdOutput.toByteArray() );
                    }
                    else
                    {
                        digest = DigesterUtils.getSha1Digest( mdOutput.toByteArray() );
                    }
                    content = ( digest + '\n' ).getBytes( "UTF-8" );
                }
                else
                {
                    content = mdOutput.toByteArray();
                }

                String mimeType = getMimeSupport().guessMimeTypeFromPath( getMimeRulesSource(), request.getRequestPath() );
                ContentLocator contentLocator = new ByteArrayContentLocator( content, mimeType );

                DefaultStorageFileItem result = new DefaultStorageFileItem( this, request, true, false, contentLocator );
                result.setLength( content.length );
                result.setCreated( mdItem.getCreated() );
                result.setModified( System.currentTimeMillis() );
                return result;
            }
            catch ( IOException e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().error( "Error parsing metadata, serving as retrieved", e );
                }
                else
                {
                    getLogger().error( "Error parsing metadata, serving as retrieved: " + e.getMessage() );
                }

                return super.doRetrieveItem( request );
            }
            finally
            {
                IOUtil.close( inputStream );
            }
        }

        return super.doRetrieveItem( request );
    }

    protected Version getClientSupportedVersion( String userAgent )
    {
        if ( userAgent == null )
        {
            return null;
        }

        if ( userAgent.startsWith( "Apache Ivy" ) )
        {
            return Version.V100;
        }

        if ( userAgent.startsWith( "Java" ) )
        {
            return Version.V100;
        }

        if ( userAgent.startsWith( "Apache-Maven/2" ) )
        {
            return Version.V100;
        }

        return Version.V110;
    }

}
