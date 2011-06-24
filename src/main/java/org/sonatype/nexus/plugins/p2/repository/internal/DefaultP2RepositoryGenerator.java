package org.sonatype.nexus.plugins.p2.repository.internal;

import static org.codehaus.plexus.util.FileUtils.deleteDirectory;
import static org.sonatype.nexus.plugins.p2.repository.P2Constants.P2_REPOSITORY_ROOT_PATH;
import static org.sonatype.nexus.plugins.p2.repository.internal.NexusUtils.safeRetrieveItem;
import static org.sonatype.nexus.plugins.p2.repository.internal.NexusUtils.storeItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryGenerator;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryGeneratorConfiguration;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.p2.bridge.ArtifactRepository;
import org.sonatype.p2.bridge.MetadataRepository;
import org.sonatype.p2.bridge.model.InstallableArtifact;
import org.sonatype.p2.bridge.model.InstallableUnit;

@Named
@Singleton
public class DefaultP2RepositoryGenerator
    implements P2RepositoryGenerator
{

    @Inject
    private Logger logger;

    private final Map<String, P2RepositoryGeneratorConfiguration> configurations;

    private final RepositoryRegistry repositories;

    private final MimeUtil mimeUtil;

    private final ArtifactRepository artifactRepository;

    private final MetadataRepository metadataRepository;

    @Inject
    public DefaultP2RepositoryGenerator( final RepositoryRegistry repositories, final MimeUtil mimeUtil,
                                         final ArtifactRepository artifactRepository,
                                         final MetadataRepository metadataRepository )
    {
        this.repositories = repositories;
        this.mimeUtil = mimeUtil;
        this.artifactRepository = artifactRepository;
        this.metadataRepository = metadataRepository;
        configurations = new HashMap<String, P2RepositoryGeneratorConfiguration>();
    }

    @Override
    public P2RepositoryGeneratorConfiguration getConfiguration( final String repositoryId )
    {
        return configurations.get( repositoryId );
    }

    @Override
    public void addConfiguration( final P2RepositoryGeneratorConfiguration configuration )
    {
        configurations.put( configuration.repositoryId(), configuration );
        try
        {
            final Repository repository = repositories.getRepository( configuration.repositoryId() );
            final StorageItem p2Dir = safeRetrieveItem( repository, P2_REPOSITORY_ROOT_PATH );
            // create if it does not exist
            if ( p2Dir == null )
            {
                final RepositoryItemUid p2RepoUid = repository.createUid( P2_REPOSITORY_ROOT_PATH );
                File tempP2Repository = null;
                try
                {
                    p2RepoUid.getLock().lock( Action.create );

                    tempP2Repository = createTemporaryP2Repository();

                    artifactRepository.write( tempP2Repository.toURI(), Collections.<InstallableArtifact> emptyList(),
                        repository.getId(), null /** repository properties */
                        , null /* mappings */);

                    final String p2ArtifactsPath = P2_REPOSITORY_ROOT_PATH + P2Constants.ARTIFACTS_XML;

                    storeItemFromFile( p2ArtifactsPath, new File( tempP2Repository, "artifacts.xml" ), repository );

                    metadataRepository.write( tempP2Repository.toURI(), Collections.<InstallableUnit> emptyList(),
                        repository.getId(), null /** repository properties */
                    );

                    final String p2ContentPath = P2_REPOSITORY_ROOT_PATH + "/" + P2Constants.CONTENT_XML;

                    storeItemFromFile( p2ContentPath, new File( tempP2Repository, "content.xml" ), repository );
                }
                finally
                {
                    p2RepoUid.getLock().unlock();
                    FileUtils.deleteDirectory( tempP2Repository );
                }
            }
        }
        catch ( final NoSuchRepositoryException e )
        {
            logger.warn( "Could not delete P2 repository [{}] as repository could not be found" );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void removeConfiguration( final P2RepositoryGeneratorConfiguration configuration )
    {
        configurations.remove( configuration.repositoryId() );
        try
        {
            final Repository repository = repositories.getRepository( configuration.repositoryId() );
            final RepositoryItemUid p2RepoUid = repository.createUid( P2_REPOSITORY_ROOT_PATH );
            try
            {
                p2RepoUid.getLock().lock( Action.create );
                final ResourceStoreRequest request = new ResourceStoreRequest( P2_REPOSITORY_ROOT_PATH );
                repository.deleteItem( request );
            }
            finally
            {
                p2RepoUid.getLock().unlock();
            }
        }
        catch ( final Exception e )
        {
            logger.warn( String.format( "Could not delete P2 repository [%s:%s] due to [%s]",
                configuration.repositoryId(), P2_REPOSITORY_ROOT_PATH, e.getMessage() ), e );
        }
    }

    @Override
    public void updateP2Artifacts( final StorageItem item )
    {
        final P2RepositoryGeneratorConfiguration configuration = getConfiguration( item.getRepositoryId() );
        if ( configuration == null )
        {
            return;
        }
        logger.debug( "Updating P2 repository artifacts (update) for [{}:{}]", item.getRepositoryId(), item.getPath() );
        try
        {
            final Repository repository = repositories.getRepository( configuration.repositoryId() );
            final RepositoryItemUid p2RepoUid = repository.createUid( P2_REPOSITORY_ROOT_PATH );
            File sourceP2Repository = null;
            File destinationP2Repository = null;
            try
            {
                p2RepoUid.getLock().lock( Action.update );

                // copy artifacts to a temp location
                sourceP2Repository = createTemporaryP2Repository();
                FileUtils.copyFile( NexusUtils.retrieveFile( repository, item.getPath() ), new File(
                    sourceP2Repository, "artifacts.xml" ) );

                destinationP2Repository = createTemporaryP2Repository();
                final File artifacts = getP2Artifacts( configuration, repository );
                final File tempArtifacts = new File( destinationP2Repository, artifacts.getName() );
                FileUtils.copyFile( artifacts, tempArtifacts );

                artifactRepository.merge( sourceP2Repository.toURI(), destinationP2Repository.toURI() );

                // create a link in /plugins directory back to original jar
                final Collection<InstallableArtifact> installableArtifacts =
                    artifactRepository.getInstallableArtifacts( sourceP2Repository.toURI() );
                for ( final InstallableArtifact installableArtifact : installableArtifacts )
                {
                    final String linkPath =
                        P2_REPOSITORY_ROOT_PATH + "/plugins/" + installableArtifact.getId() + "_"
                            + installableArtifact.getVersion() + ".jar";
                    NexusUtils.createLink( repository, item, linkPath );
                }

                // copy artifacts back to exposed location
                FileUtils.copyFile( tempArtifacts, artifacts );
            }
            finally
            {
                p2RepoUid.getLock().unlock();
                deleteDirectory( sourceP2Repository );
                deleteDirectory( destinationP2Repository );
            }
        }
        catch ( final Exception e )
        {
            logger.warn(
                String.format( "Could not update P2 repository [%s:%s] with [%s] due to [%s]",
                    configuration.repositoryId(), P2_REPOSITORY_ROOT_PATH, item.getPath(), e.getMessage() ), e );
        }
    }

    @Override
    public void removeP2Artifacts( final StorageItem item )
    {
        final P2RepositoryGeneratorConfiguration configuration = getConfiguration( item.getRepositoryId() );
        if ( configuration == null )
        {
            return;
        }
        logger.debug( "Updating P2 repository artifacts (remove) for [{}:{}]", item.getRepositoryId(), item.getPath() );
        try
        {
            final Repository repository = repositories.getRepository( configuration.repositoryId() );
            final RepositoryItemUid p2RepoUid = repository.createUid( P2_REPOSITORY_ROOT_PATH );
            File sourceP2Repository = null;
            File destinationP2Repository = null;
            try
            {
                p2RepoUid.getLock().lock( Action.update );

                // copy artifacts to a temp location
                sourceP2Repository = createTemporaryP2Repository();
                FileUtils.copyFile( NexusUtils.retrieveFile( repository, item.getPath() ), new File(
                    sourceP2Repository, "artifacts.xml" ) );

                destinationP2Repository = createTemporaryP2Repository();
                final File artifacts = getP2Artifacts( configuration, repository );
                final File tempArtifacts = new File( destinationP2Repository, artifacts.getName() );
                FileUtils.copyFile( artifacts, tempArtifacts );

                artifactRepository.remove( sourceP2Repository.toURI(), destinationP2Repository.toURI() );

                // copy artifacts back to exposed location
                FileUtils.copyFile( tempArtifacts, artifacts );
            }
            finally
            {
                p2RepoUid.getLock().unlock();
                deleteDirectory( sourceP2Repository );
                deleteDirectory( destinationP2Repository );
            }
        }
        catch ( final Exception e )
        {
            logger.warn(
                String.format( "Could not update P2 repository [%s:%s] with [%s] due to [%s]",
                    configuration.repositoryId(), P2_REPOSITORY_ROOT_PATH, item.getPath(), e.getMessage() ), e );
        }
    }

    @Override
    public void updateP2Metadata( final StorageItem item )
    {
        final P2RepositoryGeneratorConfiguration configuration = getConfiguration( item.getRepositoryId() );
        if ( configuration == null )
        {
            return;
        }
        logger.debug( "Updating P2 repository metadata (update) for [{}:{}]", item.getRepositoryId(), item.getPath() );
        try
        {
            final Repository repository = repositories.getRepository( configuration.repositoryId() );
            final RepositoryItemUid p2RepoUid = repository.createUid( P2_REPOSITORY_ROOT_PATH );
            File sourceP2Repository = null;
            File destinationP2Repository = null;
            try
            {
                p2RepoUid.getLock().lock( Action.update );

                // copy artifacts to a temp location
                sourceP2Repository = createTemporaryP2Repository();
                FileUtils.copyFile( NexusUtils.retrieveFile( repository, item.getPath() ), new File(
                    sourceP2Repository, "content.xml" ) );

                destinationP2Repository = createTemporaryP2Repository();
                final File content = getP2Content( configuration, repository );
                final File tempContent = new File( destinationP2Repository, content.getName() );
                FileUtils.copyFile( content, tempContent );

                metadataRepository.merge( sourceP2Repository.toURI(), destinationP2Repository.toURI() );

                // copy artifacts back to exposed location
                FileUtils.copyFile( tempContent, content );
            }
            finally
            {
                p2RepoUid.getLock().unlock();
                deleteDirectory( sourceP2Repository );
                deleteDirectory( destinationP2Repository );
            }
        }
        catch ( final Exception e )
        {
            logger.warn(
                String.format( "Could not update P2 repository [%s:%s] with [%s] due to [%s]",
                    configuration.repositoryId(), P2_REPOSITORY_ROOT_PATH, item.getPath(), e.getMessage() ), e );
        }
    }

    @Override
    public void removeP2Metadata( final StorageItem item )
    {
        final P2RepositoryGeneratorConfiguration configuration = getConfiguration( item.getRepositoryId() );
        if ( configuration == null )
        {
            return;
        }
        logger.debug( "Updating P2 repository metadata (remove) for [{}:{}]", item.getRepositoryId(), item.getPath() );
        try
        {
            final Repository repository = repositories.getRepository( configuration.repositoryId() );
            final RepositoryItemUid p2RepoUid = repository.createUid( P2_REPOSITORY_ROOT_PATH );
            File sourceP2Repository = null;
            File destinationP2Repository = null;
            try
            {
                p2RepoUid.getLock().lock( Action.update );

                // copy artifacts to a temp location
                sourceP2Repository = createTemporaryP2Repository();
                FileUtils.copyFile( NexusUtils.retrieveFile( repository, item.getPath() ), new File(
                    sourceP2Repository, "content.xml" ) );

                destinationP2Repository = createTemporaryP2Repository();
                final File content = getP2Content( configuration, repository );
                final File tempContent = new File( destinationP2Repository, content.getName() );
                FileUtils.copyFile( content, tempContent );

                metadataRepository.remove( sourceP2Repository.toURI(), destinationP2Repository.toURI() );

                // copy artifacts back to exposed location
                FileUtils.copyFile( tempContent, content );
            }
            finally
            {
                p2RepoUid.getLock().unlock();
                deleteDirectory( sourceP2Repository );
                deleteDirectory( destinationP2Repository );
            }
        }
        catch ( final Exception e )
        {
            logger.warn(
                String.format( "Could not update P2 repository [%s:%s] with [%s] due to [%s]",
                    configuration.repositoryId(), P2_REPOSITORY_ROOT_PATH, item.getPath(), e.getMessage() ), e );
        }
    }

    private void storeItemFromFile( final String path, final File file, final Repository repository )
        throws Exception
    {
        InputStream in = null;
        try
        {
            in = new FileInputStream( file );

            final ResourceStoreRequest request = new ResourceStoreRequest( path );

            storeItem( repository, request, in, mimeUtil.getMimeType( request.getRequestPath() ), null /* attributes */);
        }
        finally
        {
            IOUtil.close( in );
        }
    }

    private static File getP2Artifacts( final P2RepositoryGeneratorConfiguration configuration,
                                        final Repository repository )
        throws LocalStorageException
    {
        // TODO handle compressed repository
        final File file = NexusUtils.retrieveFile( repository, P2_REPOSITORY_ROOT_PATH + P2Constants.ARTIFACTS_XML );
        return file;
    }

    private static File getP2Content( final P2RepositoryGeneratorConfiguration configuration,
                                      final Repository repository )
        throws LocalStorageException
    {
        // TODO handle compressed repository
        final File file = NexusUtils.retrieveFile( repository, P2_REPOSITORY_ROOT_PATH + P2Constants.CONTENT_XML );
        return file;
    }

    static File createTemporaryP2Repository()
        throws IOException
    {
        File tempP2Repository;
        tempP2Repository = File.createTempFile( "nexus-p2-repository-plugin", "" );
        tempP2Repository.delete();
        tempP2Repository.mkdirs();
        return tempP2Repository;
    }

}
