package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.maven.tasks.RebuildMavenMetadataTask;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryConfig;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryProxy;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryRepository;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryVirtualRepository;
import org.sonatype.nexus.plugin.migration.artifactory.dto.EMixResolution;
import org.sonatype.nexus.plugin.migration.artifactory.dto.ERepositoryType;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.util.VirtualRepositoryUtil;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.RebuildAttributesTask;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.tasks.descriptors.RebuildAttributesTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ReindexTaskDescriptor;
import org.sonatype.nexus.tools.repository.RepositoryConvertor;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "artifactoryMigration" )
public class ArtifactoryMigrationPlexusResource
    extends AbstractArtifactoryMigrationPlexusResource

{

    private static final NotFileFilter ARTIFACTORY_METADATA_FILE_FILTER =
        new NotFileFilter( new SuffixFileFilter( ".artifactory-metadata" ) );

    @Requirement
    private NexusScheduler nexusScheduler;

    @Requirement
    private RepositoryConvertor repositoryConvertor;

    public ArtifactoryMigrationPlexusResource()
    {
        this.setReadable( false );
        this.setModifiable( true );
    }

    @Override
    public String getResourceUri()
    {
        return "/migration/artifactory/content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:migration]" );
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        MigrationSummaryDTO migrationSummary = ( (MigrationSummaryRequestDTO) payload ).getData();

        // need to resolve that on posts
        File artifactoryBackup = new File( migrationSummary.getBackupLocation() );
        ArtifactoryConfig cfg;
        try
        {
            cfg = ArtifactoryConfig.read( new File( artifactoryBackup, "artifactory.config.xml" ) );
        }
        catch ( Exception e )
        {
            throw new ResourceException( e );
        }

        importRepositories( migrationSummary, cfg );
        // importRepositories( cfg.getRemoteRepositories(), cfg.getProxies(), repositoriesBackup );

        importGroups( cfg.getVirtualRepositories() );

        return null;
    }

    private void importRepositories( MigrationSummaryDTO migrationSummary, ArtifactoryConfig cfg )
        throws ResourceException
    {
        final File repositoriesBackup = new File( migrationSummary.getBackupLocation(), "repositories" );

        final Map<String, ArtifactoryRepository> artifactoryRepositories = cfg.getRepositories();
        final Map<String, ArtifactoryProxy> artifactoryProxies = cfg.getProxies();
        final List<RepositoryResolutionDTO> repositories = migrationSummary.getRepositoriesResolution();

        for ( RepositoryResolutionDTO resolution : repositories )
        {
            final ArtifactoryRepository repo = artifactoryRepositories.get( resolution.getRepositoryId() );

            if ( resolution.isMixed() )
            {
                if ( EMixResolution.RELEASES_ONLY.equals( resolution.getMixResolution() ) )
                {
                    importRepository( repositoriesBackup, artifactoryProxies, resolution, repo, false, null );
                }
                else if ( EMixResolution.SNAPSHOTS_ONLY.equals( resolution.getMixResolution() ) )
                {
                    importRepository( repositoriesBackup, artifactoryProxies, resolution, repo, true, null );
                }
                else
                // BOTH
                {
                    CRepository nexusRepoReleases = createRepository( repo, artifactoryProxies, false, "releases" );
                    CRepository nexusRepoSnapshots = createRepository( repo, artifactoryProxies, true, "snapshots" );

                    CRepositoryGroup nexusGroup =
                        createGroup( repo.getKey(), nexusRepoReleases.getId(), nexusRepoSnapshots.getId() );

                    if ( resolution.isCopyCachedArtifacts() )
                    {
                        File repositoryBackup = new File( repositoriesBackup, repo.getKey() );
                        copyArtifacts( nexusRepoSnapshots, nexusRepoReleases, repositoryBackup );
                        // copyArtifacts( nexusRepoSnapshots, repositoryBackup );
                        // copyArtifacts( nexusRepoReleases, repositoryBackup );
                    }

                    if ( resolution.isMapUrls() )
                    {
                        addMapping( resolution.getRepositoryId(), nexusGroup.getGroupId(), true );
                    }

                }
            }
            else
            {
                if ( ERepositoryType.PROXIED.equals( resolution.getType() ) && resolution.isMergeSimilarRepository() )
                {
                    if ( resolution.isMapUrls() )
                    {
                        addMapping( resolution.getRepositoryId(), resolution.getSimilarRepository(), false );
                    }
                }
                else
                {
                    importRepository( repositoriesBackup, artifactoryProxies, resolution, repo,
                                      repo.getHandleSnapshots(), null );
                }
            }
        }
    }

    private void copyArtifacts( CRepository nexusRepoSnapshots, CRepository nexusRepoReleases, File repositoryBackup )
        throws ResourceException
    {
        try
        {
            repositoryConvertor.convertRepositoryWithCopy( repositoryBackup, getStorage( nexusRepoReleases ),
                                                           getStorage( nexusRepoSnapshots ),
                                                           ARTIFACTORY_METADATA_FILE_FILTER );
        }
        catch ( Exception e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to converto mixed repository", e );
        }
    }

    private CRepositoryGroup createGroup( String groupId, String... repositoriesIds )
        throws ResourceException
    {
        CRepositoryGroup group = new CRepositoryGroup();
        group.setGroupId( groupId );
        group.setName( groupId );

        for ( String repo : repositoriesIds )
        {
            group.addRepository( repo );
        }

        try
        {
            getNexus().createRepositoryGroup( group );
        }
        catch ( Exception e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to create group " + groupId, e );
        }

        return group;
    }

    private void importRepository( File repositoriesBackup, Map<String, ArtifactoryProxy> artifactoryProxies,
                                   RepositoryResolutionDTO resolution, ArtifactoryRepository repo, boolean isSnapshot,
                                   String suffix )
        throws ResourceException
    {
        CRepository nexusRepo = createRepository( repo, artifactoryProxies, isSnapshot, suffix );

        if ( resolution.isCopyCachedArtifacts() )
        {
            File repositoryBackup = new File( repositoriesBackup, repo.getKey() );
            copyArtifacts( nexusRepo, repositoryBackup );
        }
        if ( resolution.isMapUrls() )
        {
            addMapping( resolution.getRepositoryId(), nexusRepo.getId(), false );
        }
    }

    private CRepository createRepository( ArtifactoryRepository repo, Map<String, ArtifactoryProxy> artifactoryProxies,
                                          boolean isSnapshot, String suffix )
        throws ResourceException
    {
        String repoId = repo.getKey();
        String repoName = repo.getDescription();
        if ( suffix != null )
        {
            repoId += "-" + suffix;
            repoName += "-" + suffix;
        }

        CRepository nexusRepo = new CRepository();
        nexusRepo.setId( repoId );
        nexusRepo.setName( repoName );
        if ( isSnapshot )
        {
            nexusRepo.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_SNAPSHOT );
        }
        // supported on artifactory 1.3
        if ( "maven1".equals( repo.getType() ) )
        {
            nexusRepo.setType( "maven1" );
        }

        String url = repo.getUrl();
        if ( url != null )
        {
            CRemoteStorage remote = new CRemoteStorage();
            remote.setUrl( url );

            String proxyId = repo.getProxy();
            if ( proxyId != null )
            {
                ArtifactoryProxy proxy = artifactoryProxies.get( proxyId );

                CRemoteHttpProxySettings nexusProxy = new CRemoteHttpProxySettings();
                nexusProxy.setProxyHostname( proxy.getHost() );
                nexusProxy.setProxyPort( proxy.getPort() );

                CRemoteAuthentication authentication = new CRemoteAuthentication();
                authentication.setUsername( proxy.getUsername() );
                authentication.setPassword( proxy.getPassword() );
                nexusProxy.setAuthentication( authentication );

                remote.setHttpProxySettings( nexusProxy );
            }
            nexusRepo.setRemoteStorage( remote );
        }

        try
        {
            getNexus().createRepository( nexusRepo );
        }
        catch ( Exception e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Error creating repository " + repoId );
        }

        return nexusRepo;

    }

    private void addMapping( String artifactoryRepositoryId, String nexusRepositoryId, boolean isGroup )
    {
        // TODO Auto-generated method stub

    }

    private void importGroups( Map<String, ArtifactoryVirtualRepository> map )
        throws ResourceException
    {
        VirtualRepositoryUtil.resolveRepositories( map );
        for ( ArtifactoryVirtualRepository virtualRepo : map.values() )
        {
            CRepositoryGroup group = new CRepositoryGroup();
            group.setGroupId( virtualRepo.getKey() );
            group.setName( virtualRepo.getKey() );

            for ( String repo : virtualRepo.getResolvedRepositories() )
            {
                group.addRepository( repo );
            }

            try
            {
                getNexus().createRepositoryGroup( group );
            }
            catch ( Exception e )
            {
                throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to create group "
                    + virtualRepo.getKey(), e );
            }
        }
    }

    private void copyArtifacts( CRepository nexusRepo, File repositoryBackup )
        throws ResourceException
    {
        if ( repositoryBackup.isDirectory() && repositoryBackup.list().length > 0 )
        {
            try
            {
                File storage = getStorage( nexusRepo );

                // filter artifactory metadata
                FileUtils.copyDirectory( repositoryBackup, storage, ARTIFACTORY_METADATA_FILE_FILTER );
            }
            catch ( Exception e )
            {
                throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to copy artifacts to "
                    + nexusRepo.getId(), e );
            }

            createMetadatas( nexusRepo );
        }

    }

    private void createMetadatas( CRepository nexusRepo )
    {
        String repoId = nexusRepo.getId();

        ReindexTask rt = (ReindexTask) nexusScheduler.createTaskInstance( ReindexTaskDescriptor.ID );
        rt.setRepositoryId( repoId );
        nexusScheduler.submit( "reindex-" + repoId, rt );

        RebuildMavenMetadataTask mt =
            (RebuildMavenMetadataTask) nexusScheduler.createTaskInstance( RebuildMavenMetadataTaskDescriptor.ID );
        mt.setRepositoryId( repoId );
        nexusScheduler.submit( "rebuild-maven-metadata-" + repoId, mt );

        RebuildAttributesTask at =
            (RebuildAttributesTask) nexusScheduler.createTaskInstance( RebuildAttributesTaskDescriptor.ID );
        at.setRepositoryId( repoId );
        nexusScheduler.submit( "rebuild-attributes-" + repoId, at );
    }

    private File getStorage( CRepository nexusRepo )
        throws MalformedURLException, URISyntaxException
    {
        URL storage;
        if ( nexusRepo.getLocalStorage() == null )
        {
            storage = new URL( nexusRepo.defaultLocalStorageUrl );
        }
        else
        {
            storage = new URL( nexusRepo.getLocalStorage().getUrl() );
        }

        return new File( storage.toURI() );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new MigrationSummaryRequestDTO();
    }

}
