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
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
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
            StorageFileItem pomFile = getMavenRepository().retrieveArtifactPom( request );

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
            Gav gav = getGavFromArtifactStoreRequest( request );

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
            Gav gav = getGavFromArtifactStoreRequest( request );

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
            Gav gav = getGavFromArtifactStoreRequest( request );

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
            Gav gav = getGavFromArtifactStoreRequest( request );

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
            Gav gav = getGavFromArtifactStoreRequest( request );

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
            Gav gav = getGavFromArtifactStoreRequest( request );

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
        throws RepositoryNotAvailableException,
            IOException,
            MetadataException
    {
        Metadata result = null;

        try
        {
            StorageItem item = uid.getRepository().retrieveItem( false, uid, ctx );

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
        throws RepositoryNotAvailableException,
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
        throws RepositoryNotAvailableException,
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
        throws RepositoryNotAvailableException,
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
        throws RepositoryNotAvailableException,
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
            RepositoryNotAvailableException,
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
            RepositoryNotAvailableException,
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
            RepositoryNotAvailableException,
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

    private Gav getGavFromArtifactStoreRequest( ArtifactStoreRequest request )
    {
        Gav gav = new Gav(
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

        return gav;
    }

}
