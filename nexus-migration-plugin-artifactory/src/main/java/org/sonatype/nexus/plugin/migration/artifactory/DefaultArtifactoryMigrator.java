package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.log.SimpleLog4jConfig;
import org.sonatype.nexus.maven.tasks.RebuildMavenMetadataTask;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryConfig;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryProxy;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryRepository;
import org.sonatype.nexus.plugin.migration.artifactory.config.ArtifactoryVirtualRepository;
import org.sonatype.nexus.plugin.migration.artifactory.dto.EMixResolution;
import org.sonatype.nexus.plugin.migration.artifactory.dto.ERepositoryType;
import org.sonatype.nexus.plugin.migration.artifactory.dto.ERepositoryTypeResolution;
import org.sonatype.nexus.plugin.migration.artifactory.dto.GroupResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.UserResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.persist.MappingConfiguration;
import org.sonatype.nexus.plugin.migration.artifactory.persist.model.CMapping;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactorySecurityConfig;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryUser;
import org.sonatype.nexus.plugin.migration.artifactory.security.SecurityConfigConvertor;
import org.sonatype.nexus.plugin.migration.artifactory.security.SecurityConfigConvertorRequest;
import org.sonatype.nexus.plugin.migration.artifactory.security.SecurityConfigReceiver;
import org.sonatype.nexus.plugin.migration.artifactory.security.builder.ArtifactorySecurityConfigBuilder;
import org.sonatype.nexus.plugin.migration.artifactory.util.MigrationLog4jConfig;
import org.sonatype.nexus.plugin.migration.artifactory.util.VirtualRepositoryUtil;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.RebuildAttributesTask;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.tools.repository.RepositoryConvertor;
import org.sonatype.scheduling.ScheduledTask;

@Component( role = ArtifactoryMigrator.class )
public class DefaultArtifactoryMigrator
    extends AbstractLogEnabled
    implements ArtifactoryMigrator, Initializable
{
    private static final String MIGRATION_LOG = "migration.log";

    private static final NotFileFilter ARTIFACTORY_METADATA_FILE_FILTER =
        new NotFileFilter( new SuffixFileFilter( ".artifactory-metadata" ) );

    @Requirement( role = org.codehaus.plexus.archiver.UnArchiver.class, hint = "zip" )
    private ZipUnArchiver zipUnArchiver;

    @Requirement
    private NexusScheduler nexusScheduler;

    @Requirement
    private RepositoryConvertor repositoryConvertor;

    @Requirement
    private MappingConfiguration mappingConfiguration;

    @Requirement
    private SecurityConfigReceiver securityConfigAdaptorPersistor;

    @Requirement
    private SecurityConfigConvertor securityConfigConvertor;

    @Requirement
    private Nexus nexus;

    @Requirement
    private LogManager logManager;

    /**
     * Map of processed migrations.
     */
    private HashMap<String, MigrationResult> migrationResults = new HashMap<String, MigrationResult>();

    public MigrationResult getMigrationResultForId( String id )
    {
        if ( migrationResults.containsKey( id ) )
        {
            return migrationResults.get( id );
        }
        else
        {
            return null;
        }
    }

    public MigrationResult migrate( MigrationSummaryDTO migrationSummary )
    {
        MigrationResult result =
            new MigrationResult( getLogger()/* .getChildLogger( migrationSummary.getId() ) */, migrationSummary );

        if ( migrationResults.containsKey( result.getId() ) )
        {
            result.addWarningMessage( "Trying to import the same package twice. Skiping and returning previous result." );
            return getMigrationResultForId( result.getId() );
        }
        else
        {
            migrationResults.put( result.getId(), result );
        }

        result.addInfoMessage( "Importing Artifactory Backup from file: '" + migrationSummary.getBackupLocation()
            + "'." );

        // need to resolve that on posts
        File artifactoryBackupZip = new File( migrationSummary.getBackupLocation() );
        File artifactoryBackupDir;

        ArtifactoryConfig cfg = null;

        ArtifactorySecurityConfig securityCfg = null;

        // this code now looks bad... but it will make the result object easier to read.
        // maybe we should do something like the ValidationException, but i think that
        // would be ugly here too.

        // extract the zip
        try
        {
            artifactoryBackupDir = this.unzipArtifactoryBackup( result, artifactoryBackupZip );
        }
        catch ( Exception e )
        {
            result.addErrorMessage( "Failed to extract zipfile", e );

            return result;
        }

        // parse artifactory.config.xml
        try
        {
            result.addInfoMessage( "Parsing artifactory.config.xml" );

            cfg = ArtifactoryConfig.read( new File( artifactoryBackupDir, "artifactory.config.xml" ) );
        }
        catch ( Exception e )
        {
            result.addErrorMessage( "Failed to read artifactory.config.xml from backup.", e );
        }

        // parse security.xml
        try
        {
            result.addInfoMessage( "Parsing security.xml" );

            securityCfg = ArtifactorySecurityConfigBuilder.read( new File( artifactoryBackupDir, "security.xml" ) );
        }
        catch ( Exception e )
        {
            result.addErrorMessage( "Failed to read security.xml from backup.", e );
        }

        try
        {
            this.mappingConfiguration.setNexusContext( migrationSummary.getNexusContext() );
        }
        catch ( Exception e )
        {
            result.addErrorMessage( "Error updating nexus context.", e );
        }

        // if we have errors already, just return
        if ( !result.getErrorMessages().isEmpty() )
        {
            return result;
        }

        try
        {
            importRepositories( result, cfg, artifactoryBackupDir );
        }
        catch ( Exception e )
        {
            result.addErrorMessage( "Error importing repositories.", e );
        }

        try
        {
            importGroups( result, cfg );
        }
        catch ( Exception e )
        {
            result.addErrorMessage( "Error importing groups.", e );
        }

        try
        {
            importSecurity( result, securityCfg );
        }
        catch ( Exception e )
        {
            result.addErrorMessage( "Error importing security.", e );
        }

        // finally, recreate metadata for all migrated reposes
        spawnAllMetadatas( result );

        cleanBackupDir( artifactoryBackupDir );

        // if we have errors already, just return
        if ( !result.getErrorMessages().isEmpty() )
        {
            result.addWarningMessage( "Migration finished with some errors." );
        }
        else
        {
            result.addInfoMessage( "Migration finished successfully." );
        }

        result.setSuccessful( true );

        return result;
    }

    private void cleanBackupDir( File artifactoryBackupDir )
    {
        File[] files = artifactoryBackupDir.listFiles();
        for ( File file : files )
        {
            try
            {
                org.codehaus.plexus.util.FileUtils.forceDelete( file );
            }
            catch ( IOException e )
            {
                // ignore
            }
        }

        try
        {
            org.codehaus.plexus.util.FileUtils.forceDelete( artifactoryBackupDir );
        }
        catch ( IOException e )
        {
            // ignore
        }
    }

    private void importSecurity( MigrationResult result, ArtifactorySecurityConfig cfg )
    {
        result.addInfoMessage( "Importing security" );

        List<ArtifactoryUser> userList = new ArrayList<ArtifactoryUser>();

        for ( UserResolutionDTO userResolution : result.getMigrationSummary().getUsersResolution() )
        {
            result.addInfoMessage( "Importing user: " + userResolution.getUserId() );

            ArtifactoryUser user =
                new ArtifactoryUser( userResolution.getUserId(), userResolution.getPassword(),
                                     userResolution.getEmail() );

            user.setAdmin( userResolution.isAdmin() );

            userList.add( user );
        }

        // clear the users in the config, so we only import what is in the summary
        cfg.getUsers().clear();

        cfg.getUsers().addAll( userList );

        SecurityConfigConvertorRequest convertorRequest =
            new SecurityConfigConvertorRequest( cfg, securityConfigAdaptorPersistor, mappingConfiguration, result );

        convertorRequest.setResolvePermission( result.getMigrationSummary().isResolvePermission() );

        // returns a result of the migration, which may contain errors and warnings.
        securityConfigConvertor.convert( convertorRequest );
    }

    private void importRepositories( MigrationResult result, ArtifactoryConfig cfg, File artifactoryBackupDir )
    {
        result.addInfoMessage( "Importing repositories" );

        final File repositoriesBackup = new File( artifactoryBackupDir, "repositories" );

        final Map<String, ArtifactoryRepository> artifactoryRepositories = cfg.getRepositories();
        final Map<String, ArtifactoryProxy> artifactoryProxies = cfg.getProxies();
        final List<RepositoryResolutionDTO> repositories = result.getMigrationSummary().getRepositoriesResolution();

        for ( RepositoryResolutionDTO resolution : repositories )
        {
            result.addInfoMessage( "Importing repository: " + resolution.getRepositoryId() );

            final ArtifactoryRepository repo = artifactoryRepositories.get( resolution.getRepositoryId() );

            try
            {

                if ( resolution.isMixed() )
                {
                    if ( EMixResolution.RELEASES_ONLY.equals( resolution.getMixResolution() ) )
                    {
                        importRepository( result, repositoriesBackup, artifactoryProxies, resolution, repo, false, null );
                    }
                    else if ( EMixResolution.SNAPSHOTS_ONLY.equals( resolution.getMixResolution() ) )
                    {
                        importRepository( result, repositoriesBackup, artifactoryProxies, resolution, repo, true, null );
                    }
                    else
                    // BOTH
                    {
                        Repository nexusRepoReleases = createRepository( repo, artifactoryProxies, false, "releases" );
                        Repository nexusRepoSnapshots = createRepository( repo, artifactoryProxies, true, "snapshots" );

                        CRepositoryGroup nexusGroup =
                            createGroup( repo.getKey(), repo.getType(), nexusRepoReleases.getId(),
                                         nexusRepoSnapshots.getId() );

                        if ( resolution.isCopyCachedArtifacts() )
                        {
                            File repositoryBackup = new File( repositoriesBackup, repo.getKey() );
                            copyArtifacts( result, nexusRepoSnapshots, nexusRepoReleases, repositoryBackup );
                        }

                        if ( resolution.isMapUrls() )
                        {
                            CMapping map =
                                new CMapping( resolution.getRepositoryId(), nexusGroup.getGroupId(),
                                              nexusRepoReleases.getId(), nexusRepoSnapshots.getId() );
                            addMapping( map );
                        }

                    }
                }
                else
                {
                    if ( ERepositoryType.PROXY.equals( resolution.getType() ) && resolution.isMergeSimilarRepository() )
                    {
                        if ( resolution.isMapUrls() )
                        {
                            CMapping map =
                                new CMapping( resolution.getRepositoryId(), resolution.getSimilarRepositoryId() );
                            addMapping( map );
                        }
                    }
                    else
                    {
                        importRepository( result, repositoriesBackup, artifactoryProxies, resolution, repo,
                                          repo.getHandleSnapshots(), null );
                    }
                }

            }
            catch ( MigrationException e )
            {
                result.addErrorMessage( "Failed to import repository '" + resolution.getRepositoryId() + "': "
                    + e.getMessage(), e );
            }
        }
    }

    private CRepositoryGroup createGroup( String groupId, String repoType, String... repositoriesIds )
        throws MigrationException
    {
        CRepositoryGroup group = new CRepositoryGroup();
        group.setGroupId( groupId );
        group.setName( groupId );
        group.setType( repoType );

        for ( String repo : repositoriesIds )
        {
            group.addRepository( repo );
        }

        try
        {
            this.nexus.createRepositoryGroup( group );
        }
        catch ( Exception e )
        {
            throw new MigrationException( "Unable to create repository group: " + groupId, e );
        }

        return group;
    }

    private void importRepository( MigrationResult result, File repositoriesBackup,
                                   Map<String, ArtifactoryProxy> artifactoryProxies,
                                   RepositoryResolutionDTO resolution, ArtifactoryRepository repo, boolean isSnapshot,
                                   String suffix )
        throws MigrationException
    {
        File repositoryBackup = new File( repositoriesBackup, repo.getKey() );

        Repository nexusRepo;
        if ( resolution.isAlreadyExists() )
        {
            try
            {
                nexusRepo = nexus.getRepository( resolution.getRepositoryId() );
            }
            catch ( NoSuchRepositoryException e )
            {
                throw new MigrationException( "Repository should already exists: " + resolution.getRepositoryId(), e );
            }
        }
        else
        {
            nexusRepo = createRepository( repo, artifactoryProxies, isSnapshot, suffix );

        }

        if ( resolution.isCopyCachedArtifacts() )
        {
            File storage = getStorage( nexusRepo );
            copyArtifacts( result, nexusRepo.getId(), repositoryBackup, storage );
        }

        if ( resolution.isMapUrls() )
        {
            CMapping map = new CMapping( resolution.getRepositoryId(), nexusRepo.getId() );
            addMapping( map );
        }
    }

    private Repository createRepository( ArtifactoryRepository repo, Map<String, ArtifactoryProxy> artifactoryProxies,
                                         boolean isSnapshot, String suffix )
        throws MigrationException

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
        if ( !StringUtils.isBlank( url ) )
        {
            CRemoteStorage remote = new CRemoteStorage();
            remote.setUrl( url );

            String proxyId = repo.getProxy();

            if ( !StringUtils.isBlank( proxyId ) )
            {
                ArtifactoryProxy proxy = artifactoryProxies.get( proxyId );

                CRemoteHttpProxySettings nexusProxy = new CRemoteHttpProxySettings();
                nexusProxy.setProxyHostname( proxy.getHost() );
                nexusProxy.setProxyPort( proxy.getPort() );

                if ( !StringUtils.isBlank( proxy.getUsername() ) )
                {
                    CRemoteAuthentication authentication = new CRemoteAuthentication();
                    authentication.setUsername( proxy.getUsername() );
                    authentication.setPassword( proxy.getPassword() );

                    if ( !StringUtils.isBlank( proxy.getDomain() ) )
                    {
                        authentication.setNtlmDomain( proxy.getDomain() );
                    }

                    nexusProxy.setAuthentication( authentication );

                    remote.setHttpProxySettings( nexusProxy );
                }
            }

            nexusRepo.setRemoteStorage( remote );
        }

        try
        {
            this.nexus.createRepository( nexusRepo );
        }
        catch ( Exception e )
        {
            throw new MigrationException( "Unable to create new repository: " + repoId, e );
        }

        try
        {
            return this.nexus.getRepository( repoId );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new MigrationException( "Should never happen, repo was created 3 lines before", e );
        }
    }

    private void importGroups( MigrationResult result, ArtifactoryConfig cfg )
    {
        result.addInfoMessage( "Importing groups" );

        final Map<String, ArtifactoryVirtualRepository> virtualRepositories = cfg.getVirtualRepositories();
        VirtualRepositoryUtil.resolveRepositories( virtualRepositories );
        final Map<String, ArtifactoryRepository> repositories = cfg.getRepositories();
        final List<GroupResolutionDTO> groups = result.getMigrationSummary().getGroupsResolution();

        for ( GroupResolutionDTO resolution : groups )
        {
            result.addInfoMessage( "Importing group: " + resolution.getGroupId() );

            ArtifactoryVirtualRepository virtualRepo = virtualRepositories.get( resolution.getGroupId() );
            CRepositoryGroup group = new CRepositoryGroup();
            group.setGroupId( virtualRepo.getKey() );
            group.setName( virtualRepo.getKey() );

            if ( ERepositoryTypeResolution.MAVEN_1_ONLY.equals( resolution.getRepositoryTypeResolution() ) )
            {
                group.setType( "maven1" );
            }

            for ( final String repoId : virtualRepo.getResolvedRepositories() )
            {
                RepositoryResolutionDTO repoResolution = result.getMigrationSummary().getRepositoryResolution( repoId );

                if ( ERepositoryType.PROXY.equals( repoResolution.getType() )
                    && repoResolution.isMergeSimilarRepository() )
                {
                    group.addRepository( repoResolution.getSimilarRepositoryId() );
                }
                else if ( !resolution.isMixed() )
                {
                    addRepository( group, repoId );
                }
                else
                {
                    ArtifactoryRepository repo = repositories.get( repoId );
                    String type = repo.getType();
                    if ( ERepositoryTypeResolution.MAVEN_1_ONLY.equals( resolution.getRepositoryTypeResolution() ) )
                    {
                        if ( "maven1".equals( type ) )
                        {
                            addRepository( group, repoId );
                        }
                    }
                    else if ( ERepositoryTypeResolution.VIRTUAL_BOTH.equals( resolution.getRepositoryTypeResolution() ) )
                    {
                        if ( "maven1".equals( type ) )
                        {
                            try
                            {
                                addVirtualRepository( group, repoId );
                            }
                            catch ( Exception e )
                            {
                                result.addErrorMessage( "Failed to add a 'maven1' virtual repository for repository '"
                                    + repoId + "': " + e.getMessage(), e );
                            }
                        }
                        else
                        {
                            addRepository( group, repoId );
                        }
                    }
                    else
                    // MAVEN2 only
                    {
                        if ( type == null || "maven2".equals( type ) )
                        {
                            addRepository( group, repoId );
                        }

                    }
                }
            }

            try
            {
                this.nexus.createRepositoryGroup( group );
            }
            catch ( Exception e )
            {
                result.addErrorMessage( "Failed to create group '" + group.getGroupId() + "': " + e.getMessage(), e );
            }

            CMapping map = new CMapping( virtualRepo.getKey(), group.getGroupId(), null, null );
            try
            {
                addMapping( map );
            }
            catch ( MigrationException e )
            {
                result.addErrorMessage( "Unable to update mapping information", e );
            }
        }
    }

    private void addMapping( CMapping map )
        throws MigrationException
    {
        try
        {
            mappingConfiguration.addMapping( map );
        }
        catch ( IOException e )
        {
            throw new MigrationException( "Unable to update mapping information", e );
        }
    }

    private void addVirtualRepository( CRepositoryGroup group, String repoId )
        throws MigrationException
    {

        CMapping map = mappingConfiguration.getMapping( repoId );

        if ( map.getNexusGroupId() == null )
        {
            CRepositoryShadow shadowRepo = createShadowRepo( map.getNexusRepositoryId() );
            group.addRepository( shadowRepo.getId() );
        }
        else
        {
            CRepositoryShadow releasesRepo = createShadowRepo( map.getReleasesRepositoryId() );
            group.addRepository( releasesRepo.getId() );
            CRepositoryShadow snapshotsRepo = createShadowRepo( map.getSnapshotsRepositoryId() );
            group.addRepository( snapshotsRepo.getId() );
        }
    }

    private void addRepository( CRepositoryGroup group, String repoId )
    {
        CMapping map = mappingConfiguration.getMapping( repoId );

        if ( map.getNexusGroupId() == null )
        {
            group.addRepository( map.getNexusRepositoryId() );
        }
        else
        {
            group.addRepository( map.getReleasesRepositoryId() );
            group.addRepository( map.getSnapshotsRepositoryId() );
        }
    }

    private CRepositoryShadow createShadowRepo( String shadowOfRepoId )
        throws MigrationException
    {
        CRepositoryShadow shadowRepo = new CRepositoryShadow();
        String shadowId = shadowOfRepoId + "-virtual";
        shadowRepo.setId( shadowId );
        shadowRepo.setName( shadowId );
        shadowRepo.setShadowOf( shadowOfRepoId );
        shadowRepo.setType( "m1-m2-shadow" );

        try
        {
            this.nexus.createRepositoryShadow( shadowRepo );
        }
        catch ( Exception e )
        {
            throw new MigrationException( "Error creating shadow repo: " + shadowOfRepoId, e );
        }

        return shadowRepo;
    }

    private void copyArtifacts( MigrationResult result, Repository nexusRepoSnapshots, Repository nexusRepoReleases,
                                File repositoryBackup )
        throws MigrationException
    {
        result.addInfoMessage( "Copying cached artifacts to: " + nexusRepoReleases.getId() + ", "
            + nexusRepoSnapshots.getId() );

        try
        {
            repositoryConvertor.convertRepositoryWithCopy( repositoryBackup, getStorage( nexusRepoReleases ),
                                                           getStorage( nexusRepoSnapshots ),
                                                           ARTIFACTORY_METADATA_FILE_FILTER );
        }
        catch ( IOException e )
        {
            throw new MigrationException( "Unable to copy cached artifacts", e );
        }

        createMetadatas( result, nexusRepoSnapshots.getId() );
        createMetadatas( result, nexusRepoReleases.getId() );
    }

    private void copyArtifacts( MigrationResult result, String repoId, File sourceRepositoryBackup,
                                File destinationStorage )
        throws MigrationException
    {
        result.addInfoMessage( "Copying cached artifacts to: " + repoId );

        if ( sourceRepositoryBackup.isDirectory() && sourceRepositoryBackup.list().length > 0 )
        {
            // filter artifactory metadata
            try
            {
                FileUtils.copyDirectory( sourceRepositoryBackup, destinationStorage, ARTIFACTORY_METADATA_FILE_FILTER );
            }
            catch ( IOException e )
            {
                throw new MigrationException( "Unable to copy cached artifacts", e );
            }

            createMetadatas( result, repoId );
        }

    }

    private void createMetadatas( MigrationResult result, String repoId )
    {
        if ( result.getMigratedRepositoryIds().add( repoId ) )
        {
            result.addInfoMessage( "Spooling the creation of repository metadatas for repository " + repoId );
        }
    }

    private void spawnAllMetadatas( MigrationResult result )
    {
        for ( String repoId : result.getMigratedRepositoryIds() )
        {
            result.addInfoMessage( "Recreating repository metadatas " + repoId );

            RebuildAttributesTask at = nexusScheduler.createTaskInstance( RebuildAttributesTask.class );
            at.setRepositoryId( repoId );
            ScheduledTask<Object> schedule = nexusScheduler.submit( "rebuild-attributes-" + repoId, at );
            try
            {
                schedule.get();
            }
            catch ( Exception e )
            {
                result.addWarningMessage( "Error building attributes " + repoId, e );
            }

            RebuildMavenMetadataTask mt = nexusScheduler.createTaskInstance( RebuildMavenMetadataTask.class );
            mt.setRepositoryId( repoId );
            schedule = nexusScheduler.submit( "rebuild-maven-metadata-" + repoId, mt );
            try
            {
                schedule.get();
            }
            catch ( Exception e )
            {
                result.addWarningMessage( "Error creating maven metadata " + repoId, e );
            }

            ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );
            rt.setRepositoryId( repoId );
            schedule = nexusScheduler.submit( "reindex-" + repoId, rt );
            try
            {
                schedule.get();
            }
            catch ( Exception e )
            {
                result.addWarningMessage( "Error creating nexus index " + repoId, e );
            }
        }

    }

    private File getStorage( Repository nexusRepo )
        throws MigrationException
    {
        String localStorage = nexusRepo.getLocalUrl();
        if ( localStorage == null )
        {
            throw new MigrationException( "Local storage information not available!" );
        }

        URI storageURI;
        try
        {
            storageURI = new URI( localStorage );
        }
        catch ( URISyntaxException e )
        {
            throw new MigrationException( "Invalid repository URI! " + localStorage );
        }

        File storage = new File( storageURI );
        if ( !storage.exists() )
        {
            throw new MigrationException( "Repository storage not found! " + storage.getAbsolutePath() );
        }
        return storage;
    }

    private File unzipArtifactoryBackup( MigrationResult result, File fileItem )
        throws IOException, ArchiverException
    {
        result.addInfoMessage( "Unpacking backup file" );

        File tempDir = this.nexus.getNexusConfiguration().getTemporaryDirectory();

        File artifactoryBackup = new File( tempDir, FilenameUtils.getBaseName( fileItem.getName() ) + "content" );
        artifactoryBackup.mkdirs();

        zipUnArchiver.setSourceFile( fileItem );
        zipUnArchiver.setDestDirectory( artifactoryBackup );
        zipUnArchiver.extract();

        return artifactoryBackup;
    }

    public void initialize()
        throws InitializationException
    {
        if ( this.logManager.getLogFile( MIGRATION_LOG ) != null )
        {
            return;
        }

        File nexusLog = this.logManager.getLogFile( "nexus.log" );
        File migrationLog = new File( nexusLog.getParentFile(), MIGRATION_LOG );

        try
        {
            SimpleLog4jConfig logConfig = new MigrationLog4jConfig( nexus.getLogConfig(), migrationLog );
            logManager.setLogConfig( logConfig );
        }
        catch ( IOException e )
        {
            throw new InitializationException( "Unable to configure migration log", e );
        }
    }

}
