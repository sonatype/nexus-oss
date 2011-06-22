package org.sonatype.nexus.plugins.p2.repository.internal;

import static org.sonatype.nexus.plugins.p2.repository.internal.NexusUtils.retrieveFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGenerator;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGeneratorConfiguration;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;
import org.sonatype.p2.bridge.ArtifactRepository;
import org.sonatype.p2.bridge.model.InstallableArtifact;

@Named
@Singleton
public class DefaultP2MetadataGenerator
    implements P2MetadataGenerator
{

    @Inject
    private Logger logger;

    private final Map<String, P2MetadataGeneratorConfiguration> configurations;

    private final RepositoryRegistry repositories;

    private final ArtifactRepository artifactRepository;

    @Inject
    public DefaultP2MetadataGenerator( final RepositoryRegistry repositories,
                                       final ArtifactRepository artifactRepository )
    {
        this.repositories = repositories;
        this.artifactRepository = artifactRepository;
        configurations = new HashMap<String, P2MetadataGeneratorConfiguration>();
    }

    @Override
    public P2MetadataGeneratorConfiguration getConfiguration( final String repositoryId )
    {
        return configurations.get( repositoryId );
    }

    @Override
    public void addConfiguration( final P2MetadataGeneratorConfiguration configuration )
    {
        configurations.put( configuration.repositoryId(), configuration );
    }

    @Override
    public void removeConfiguration( final P2MetadataGeneratorConfiguration configuration )
    {
        configurations.remove( configuration.repositoryId() );
    }

    @Override
    public void generateP2Metadata( final StorageItem item )
    {
        final P2MetadataGeneratorConfiguration configuration = getConfiguration( item.getRepositoryId() );
        if ( configuration == null )
        {
            return;
        }
        logger.debug( "Generate P2 metadata for [{}:{}]", item.getRepositoryId(), item.getPath() );

        // TODO only regenerate if jar is newer

        try
        {
            final Repository repository = repositories.getRepository( item.getRepositoryId() );
            final File bundle = retrieveFile( repository, item.getPath() );
            final JarFile jarFile = new JarFile( bundle );
            final Manifest manifest = jarFile.getManifest();
            final Attributes mainAttributes = manifest.getMainAttributes();

            final String bsn = mainAttributes.getValue( "Bundle-SymbolicName" );
            if ( bsn == null )
            {
                logger.debug( "[{}:{}] is not an OSGi bundle. Bailing out.", item.getRepositoryId(), item.getPath() );
                return;
            }
            final String version = mainAttributes.getValue( "Bundle-Version" );

            final InstallableArtifact artifact = new InstallableArtifact();
            artifact.setId( bsn );
            artifact.setClassifier( "osgi.bundle" );
            artifact.setVersion( version );

            // TODO set properties as:
            // <property name="artifact.size" value="3260428"/>
            // <property name="download.size" value="3260428"/>
            // <property name="download.md5" value="1023cb71600ee96e5d0ca225d23faa7b"/>

            final Collection<InstallableArtifact> artifacts = new ArrayList<InstallableArtifact>();
            artifacts.add( artifact );

            File tempP2Repository = null;
            try
            {
                final String extension = FileUtils.getExtension( bundle.getPath() );

                tempP2Repository = File.createTempFile( "nexus-p2-repository-plugin", "" );
                tempP2Repository.delete();
                tempP2Repository.mkdirs();

                artifactRepository.write( tempP2Repository.toURI(), artifacts, bsn, null );

                final String p2ArtifactsPath =
                    item.getPath().substring( 0, item.getPath().length() - extension.length() - 1 )
                        + "-p2Artifacts.xml";

                storeItemFromFile( p2ArtifactsPath, new File( tempP2Repository, "artifacts.xml" ), repository );
            }
            finally
            {
                FileUtils.deleteDirectory( tempP2Repository );
            }
        }
        catch ( final Exception e )
        {
            logger.warn( String.format( "Could not read manifest attributes of [%s:%s] due to %s. Bailing out.",
                item.getRepositoryId(), item.getPath(), e.getMessage() ) );
            return;
        }
    }

    @Override
    public void removeP2Metadata( final StorageItem item )
    {
        final P2MetadataGeneratorConfiguration configuration = getConfiguration( item.getRepositoryId() );
        if ( configuration == null )
        {
            return;
        }
        logger.debug( "Removing P2 metadata for [{}:{}]", item.getRepositoryId(), item.getPath() );
    }

    private static void storeItemFromFile( final String path, final File file, final Repository repository )
        throws LocalStorageException, UnsupportedStorageOperationException
    {
        final ContentLocator content = new FileContentLocator( file, "text/xml" );
        final DefaultStorageFileItem storageItem =
            new DefaultStorageFileItem( repository, new ResourceStoreRequest( path ), true /* isReadable */,
                false /* isWritable */, content );
        repository.getLocalStorage().storeItem( repository, storageItem );
    }

}
