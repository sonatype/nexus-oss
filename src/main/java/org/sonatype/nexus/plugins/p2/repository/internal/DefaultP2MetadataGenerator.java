package org.sonatype.nexus.plugins.p2.repository.internal;

import static org.sonatype.nexus.plugins.p2.repository.internal.DefaultP2RepositoryGenerator.createTemporaryP2Repository;
import static org.sonatype.nexus.plugins.p2.repository.internal.NexusUtils.retrieveFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGenerator;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGeneratorConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.p2.bridge.ArtifactRepository;
import org.sonatype.p2.bridge.MetadataRepository;
import org.sonatype.p2.bridge.Publisher;
import org.sonatype.p2.bridge.model.InstallableArtifact;
import org.sonatype.p2.bridge.model.InstallableUnit;
import org.sonatype.p2.bridge.model.InstallableUnitArtifact;

@Named
@Singleton
public class DefaultP2MetadataGenerator
    implements P2MetadataGenerator
{
    private static final String GENERATED_AT_ATTRIBUTE = "p2Repository.generated.timestamp";

    @Inject
    private Logger logger;

    private final Map<String, P2MetadataGeneratorConfiguration> configurations;

    private final RepositoryRegistry repositories;

    private final ArtifactRepository artifactRepository;

    private final MetadataRepository metadataRepository;

    private final Publisher publisher;

    private final MimeUtil mimeUtil;

    @Inject
    public DefaultP2MetadataGenerator( final RepositoryRegistry repositories, final MimeUtil mimeUtil,
                                       final ArtifactRepository artifactRepository,
                                       final MetadataRepository metadataRepository, final Publisher publisher )
    {
        this.repositories = repositories;
        this.mimeUtil = mimeUtil;
        this.artifactRepository = artifactRepository;
        this.metadataRepository = metadataRepository;
        this.publisher = publisher;
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
            artifact.setPath( bundle.getAbsolutePath() );
            artifact.setRepositoryPath( item.getPath() );

            final Collection<InstallableArtifact> artifacts = new ArrayList<InstallableArtifact>();
            artifacts.add( artifact );

            final Collection<InstallableUnit> ius =
                publisher.generateIUs( true /* generateCapabilities */, true /* generateRequirements */,
                    true /* generateManifest */, bundle );

            for ( final InstallableUnit iu : ius )
            {
                final InstallableUnitArtifact iuArtifact = new InstallableUnitArtifact();
                iuArtifact.setId( artifact.getId() );
                iuArtifact.setClassifier( artifact.getClassifier() );
                iuArtifact.setVersion( artifact.getVersion() );

                iu.addArtifact( iuArtifact );
            }

            File tempP2Repository = null;
            try
            {
                final String extension = FileUtils.getExtension( bundle.getPath() );

                tempP2Repository = createTemporaryP2Repository();

                artifactRepository.write( tempP2Repository.toURI(), artifacts, bsn, null /** repository properties */
                , new String[][] { { "(classifier=osgi.bundle)", "${repoUrl}/" + item.getPath() + "}" } } );

                final String p2ArtifactsPath =
                    item.getPath().substring( 0, item.getPath().length() - extension.length() - 1 )
                        + "-p2Artifacts.xml";

                storeItemFromFile( p2ArtifactsPath, new File( tempP2Repository, "artifacts.xml" ), repository );

                metadataRepository.write( tempP2Repository.toURI(), ius, bsn, null /** repository properties */
                );

                final String p2ContentPath =
                    item.getPath().substring( 0, item.getPath().length() - extension.length() - 1 ) + "-p2Content.xml";

                storeItemFromFile( p2ContentPath, new File( tempP2Repository, "content.xml" ), repository );
            }
            finally
            {
                FileUtils.deleteDirectory( tempP2Repository );
            }
        }
        catch ( final Exception e )
        {
            logger.warn(
                String.format( "Could not generate p2 metadata of [%s:%s] due to %s. Bailing out.",
                    item.getRepositoryId(), item.getPath(), e.getMessage() ), e );
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

    private void storeItemFromFile( final String path, final File file, final Repository repository )
        throws Exception
    {
        InputStream in = null;
        try
        {
            in = new FileInputStream( file );
            final Map<String, String> attributes = new HashMap<String, String>();
            attributes.put( GENERATED_AT_ATTRIBUTE, new Date().toString() );

            final ResourceStoreRequest request = new ResourceStoreRequest( path );

            NexusUtils.storeItem( repository, request, in, mimeUtil.getMimeType( request.getRequestPath() ), attributes );
        }
        finally
        {
            IOUtil.close( in );
        }
    }

}
