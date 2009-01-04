/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.maven;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.Plugin;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * A MetadataLocator powered by Nexus' MavenRepository.
 * 
 * @author cstamas
 */
public class MavenRepositoryMetadataLocator
    implements MetadataLocator
{
    private final MavenRepository mavenRepository;

    public MavenRepositoryMetadataLocator( MavenRepository repository )
    {
        super();

        this.mavenRepository = repository;
    }

    public MavenRepository getMavenRepository()
    {
        return mavenRepository;
    }

    public GavCalculator getGavCalculator()
    {
        return getMavenRepository().getGavCalculator();
    }

    public ArtifactPackagingMapper getArtifactPackagingMapper()
    {
        return getMavenRepository().getArtifactPackagingMapper();
    }

    public Gav getGavForRequest( ArtifactStoreRequest request )
    {
        Gav gav = null;

        if ( ArtifactStoreRequest.DUMMY_PATH.equals( request.getRequestPath() ) )
        {
            // we have no path info
            gav = new Gav(
                request.getGroupId(),
                request.getArtifactId(),
                request.getVersion(),
                request.getClassifier(),
                getArtifactPackagingMapper().getExtensionForPackaging( request.getPackaging() ),
                null,
                null,
                null,
                VersionUtils.isSnapshot( request.getVersion() ),
                false,
                null,
                false,
                null );
        }
        else
        {
            // we have path info, the best way is to calc GAV
            gav = getGavCalculator().pathToGav( request.getRequestPath() );
        }

        return gav;
    }

    public Plugin extractPluginElementFromPom( ArtifactStoreRequest request )
        throws IOException
    {
        Model pom = retrievePom( request );

        if ( !"maven-plugin".equals( pom.getPackaging() ) )
        {
            return null;
        }

        Plugin plugin = new Plugin();

        plugin.setArtifactId( pom.getArtifactId() );

        plugin.setName( pom.getName() );

        plugin.setPrefix( PluginDescriptor.getGoalPrefixFromArtifactId( pom.getArtifactId() ) );

        return plugin;
    }

    public Model retrievePom( ArtifactStoreRequest request )
        throws IOException
    {
        try
        {
            Gav gav = getMavenRepository().getMetadataManager().resolveArtifact( getMavenRepository(), request );

            String pomPath = getMavenRepository().getGavCalculator().gavToPath( gav );

            StorageFileItem pomFile = (StorageFileItem) getMavenRepository().retrieveItem(
                getMavenRepository().createUid( pomPath ),
                request.getRequestContext() );

            Model model = null;

            InputStream is = pomFile.getInputStream();

            try
            {
                MavenXpp3Reader rd = new MavenXpp3Reader();

                model = rd.read( is );

                return model;
            }
            catch ( XmlPullParserException e )
            {
                throw createIOExceptionWithCause( e.getMessage(), e );
            }
            finally
            {
                IOUtil.close( is );
            }
        }
        catch ( Exception e )
        {
            throw createIOExceptionWithCause( e.getMessage(), e );
        }
    }

    public Metadata retrieveGAVMetadata( ArtifactStoreRequest request )
        throws IOException
    {
        try
        {
            Gav gav = getGavForRequest( request );

            return readOrCreateGAVMetadata( getMavenRepository(), gav, request.getRequestContext() );
        }
        catch ( Exception e )
        {
            throw createIOExceptionWithCause( e.getMessage(), e );
        }
    }

    public Metadata retrieveGAMetadata( ArtifactStoreRequest request )
        throws IOException
    {
        try
        {
            Gav gav = getGavForRequest( request );

            return readOrCreateGAMetadata( getMavenRepository(), gav, request.getRequestContext() );
        }
        catch ( Exception e )
        {
            throw createIOExceptionWithCause( e.getMessage(), e );
        }
    }

    public Metadata retrieveGMetadata( ArtifactStoreRequest request )
        throws IOException
    {
        try
        {
            Gav gav = getGavForRequest( request );

            return readOrCreateGMetadata( getMavenRepository(), gav, request.getRequestContext() );
        }
        catch ( Exception e )
        {
            throw createIOExceptionWithCause( e.getMessage(), e );
        }
    }

    public void storeGAVMetadata( ArtifactStoreRequest request, Metadata metadata )
        throws IOException
    {
        try
        {
            Gav gav = getGavForRequest( request );

            writeGAVMetadata( getMavenRepository(), gav, metadata, request.getRequestContext() );
        }
        catch ( Exception e )
        {
            throw createIOExceptionWithCause( e.getMessage(), e );
        }
    }

    public void storeGAMetadata( ArtifactStoreRequest request, Metadata metadata )
        throws IOException
    {
        try
        {
            Gav gav = getGavForRequest( request );

            writeGAMetadata( getMavenRepository(), gav, metadata, request.getRequestContext() );
        }
        catch ( Exception e )
        {
            throw createIOExceptionWithCause( e.getMessage(), e );
        }
    }

    public void storeGMetadata( ArtifactStoreRequest request, Metadata metadata )
        throws IOException
    {
        try
        {
            Gav gav = getGavForRequest( request );

            writeGMetadata( getMavenRepository(), gav, metadata, request.getRequestContext() );
        }
        catch ( Exception e )
        {
            throw createIOExceptionWithCause( e.getMessage(), e );
        }
    }

    // ==================================================
    // -- internal stuff below
    // ==================================================

    private IOException createIOExceptionWithCause( String message, Throwable cause )
    {
        IOException result = new IOException( message );

        result.initCause( cause );

        return result;
    }

    protected Metadata readOrCreateMetadata( RepositoryItemUid uid, Map<String, Object> ctx )
        throws IllegalOperationException,
            IOException,
            MetadataException
    {
        Metadata result = null;

        try
        {
            StorageItem item = uid.getRepository().retrieveItem( uid, ctx );

            if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
            {
                StorageFileItem fileItem = (StorageFileItem) item;

                InputStream is = null;

                try
                {
                    is = fileItem.getInputStream();

                    result = MetadataBuilder.read( is );
                }
                finally
                {
                    IOUtil.close( is );
                }
            }
            else
            {
                throw new IllegalArgumentException( "The UID " + uid.toString() + " is not a file!" );
            }
        }
        catch ( ItemNotFoundException e )
        {
            result = new Metadata();
        }

        return result;
    }

    protected void writeMetadata( RepositoryItemUid uid, Map<String, Object> ctx, Metadata md )
        throws IllegalOperationException,
            UnsupportedStorageOperationException,
            MetadataException,
            IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        String mdString = outputStream.toString( "UTF-8" );

        outputStream.close();

        DefaultStorageFileItem file = new DefaultStorageFileItem(
            uid.getRepository(),
            uid.getPath(),
            true,
            true,
            new StringContentLocator( mdString ) );

        ( (MavenRepository) uid.getRepository() ).storeItemWithChecksums( file );
    }

    protected Metadata readOrCreateGAVMetadata( MavenRepository repository, Gav gav, Map<String, Object> ctx )
        throws IllegalOperationException,
            IOException,
            MetadataException
    {
        String mdPath = getGavCalculator().gavToPath( gav );

        // GAV
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) ) + "/maven-metadata.xml";

        RepositoryItemUid uid = repository.createUid( mdPath );

        Metadata result = readOrCreateMetadata( uid, ctx );

        result.setGroupId( gav.getGroupId() );

        result.setArtifactId( gav.getArtifactId() );

        result.setVersion( gav.getBaseVersion() );

        return result;
    }

    protected Metadata readOrCreateGAMetadata( MavenRepository repository, Gav gav, Map<String, Object> ctx )
        throws IllegalOperationException,
            IOException,
            MetadataException
    {
        String mdPath = getGavCalculator().gavToPath( gav );

        // GAV
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) );

        // GA
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) ) + "/maven-metadata.xml";

        RepositoryItemUid uid = repository.createUid( mdPath );

        Metadata result = readOrCreateMetadata( uid, ctx );

        result.setGroupId( gav.getGroupId() );

        result.setArtifactId( gav.getArtifactId() );

        result.setVersion( null );

        return result;
    }

    protected Metadata readOrCreateGMetadata( MavenRepository repository, Gav gav, Map<String, Object> ctx )
        throws IllegalOperationException,
            IOException,
            MetadataException
    {
        String mdPath = getGavCalculator().gavToPath( gav );

        // GAV
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) );

        // GA
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) );

        // G
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) ) + "/maven-metadata.xml";

        RepositoryItemUid uid = repository.createUid( mdPath );

        Metadata result = readOrCreateMetadata( uid, ctx );

        result.setGroupId( null );

        result.setArtifactId( null );

        result.setVersion( null );

        return result;
    }

    protected void writeGAVMetadata( MavenRepository repository, Gav gav, Metadata md, Map<String, Object> ctx )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            MetadataException,
            IOException
    {
        String mdPath = getGavCalculator().gavToPath( gav );

        // GAV
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) ) + "/maven-metadata.xml";

        RepositoryItemUid uid = repository.createUid( mdPath );

        writeMetadata( uid, ctx, md );
    }

    protected void writeGAMetadata( MavenRepository repository, Gav gav, Metadata md, Map<String, Object> ctx )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            MetadataException,
            IOException
    {
        String mdPath = getGavCalculator().gavToPath( gav );

        // GAV
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) );

        // GA
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) ) + "/maven-metadata.xml";

        RepositoryItemUid uid = repository.createUid( mdPath );

        writeMetadata( uid, ctx, md );
    }

    protected void writeGMetadata( MavenRepository repository, Gav gav, Metadata md, Map<String, Object> ctx )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            MetadataException,
            IOException
    {
        String mdPath = getGavCalculator().gavToPath( gav );

        // GAV
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) );

        // GA
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) );

        // G
        mdPath = mdPath.substring( 0, mdPath.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR ) ) + "/maven-metadata.xml";

        RepositoryItemUid uid = repository.createUid( mdPath );

        writeMetadata( uid, ctx, md );
    }

}
