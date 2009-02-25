package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
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
import org.sonatype.nexus.plugin.migration.artifactory.security.SecurityConfigReceiver;
import org.sonatype.nexus.plugin.migration.artifactory.security.builder.ArtifactorySecurityConfigBuilder;
import org.sonatype.nexus.plugin.migration.artifactory.util.VirtualRepositoryUtil;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.RebuildAttributesTask;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.tools.repository.RepositoryConvertor;

@Component( role = ArtifactoryMigrator.class )
public class ArtifactoryMigrator
    extends AbstractLogEnabled
    implements Initializable
{

    private static final NotFileFilter ARTIFACTORY_METADATA_FILE_FILTER =
        new NotFileFilter( new SuffixFileFilter( ".artifactory-metadata" ) );

    /**
     * If the log directory cannot be figured out from the LogFileManager, this path is used.
     */
    @Configuration( value = "${nexus-work}/logs/migration.log" )
    private String logFile;

    @Requirement( role = org.codehaus.plexus.archiver.UnArchiver.class, hint = "zip" )
    private ZipUnArchiver zipUnArchiver;

    @Requirement
    private NexusScheduler nexusScheduler;

    @Requirement
    private RepositoryConvertor repositoryConvertor;

    @Requirement( role = MappingConfiguration.class, hint = "default" )
    private MappingConfiguration mappingConfiguration;

    @Requirement
    private SecurityConfigReceiver securityConfigAdaptorPersistor;

    @Requirement
    private Nexus nexus;

    protected Logger logger = Logger.getLogger( this.getClass() );

    @Requirement
    protected MigrationResult migrationResult;

    private static final List<String> migrated = new ArrayList<String>();

    public MigrationResult migrate( MigrationSummaryDTO migrationSummary )
    {
        // reset the resultLogger
        this.migrationResult.clear();

        if ( migrated.contains( migrationSummary.getId() ) )
        {
            addWarnMessage( "Trying to import the same package twice: '" + migrationSummary.getId()
                + "'." );
            return migrationResult;
        }
        else
        {
            migrated.add( migrationSummary.getId() );
        }

        addInfoMessage( "Importing Artifactory Backup from file: '" + migrationSummary.getBackupLocation() + "'." );

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
            artifactoryBackupDir = this.unzipArtifactoryBackup( artifactoryBackupZip );
        }
        catch ( Exception e )
        {
            addErrorMessage( "Failed to extract zipfile", e );
            return migrationResult;
        }

        // parse artifactory.config.xml
        try
        {
            addInfoMessage( "Parsing artifactory.config.xml" );
            cfg = ArtifactoryConfig.read( new File( artifactoryBackupDir, "artifactory.config.xml" ) );
        }
        catch ( Exception e )
        {
            addErrorMessage( "Failed to read artifactory.config.xml from backup.", e );
        }

        // parse security.xml
        try
        {
            addInfoMessage( "Parsing security.xml" );
            securityCfg = ArtifactorySecurityConfigBuilder.read( new File( artifactoryBackupDir, "security.xml" ) );
        }
        catch ( Exception e )
        {
            addErrorMessage( "Failed to read security.xml from backup.", e );
        }

        try
        {
            this.mappingConfiguration.setNexusContext( migrationSummary.getNexusContext() );
        }
        catch ( Exception e )
        {
            addErrorMessage( "Error updating nexus context.", e );
        }

        // if we have errors already, just return
        if ( !migrationResult.getErrorMessages().isEmpty() )
        {
            return migrationResult;
        }

        try
        {
            importRepositories( migrationSummary, cfg, artifactoryBackupDir );
        }
        catch ( Exception e )
        {
            addErrorMessage( "Error importing repositories.", e );
        }

        try
        {
            importGroups( migrationSummary, cfg );
        }
        catch ( Exception e )
        {
            addErrorMessage( "Error importing groups.", e );
        }

        try
        {
            importSecurity( migrationSummary, securityCfg );
        }
        catch ( Exception e )
        {
            addErrorMessage( "Error importing security.", e );
        }

        cleanBackupDir( artifactoryBackupDir );

        // if we have errors already, just return
        if ( !migrationResult.getErrorMessages().isEmpty() )
        {
            addWarnMessage( "Migration finished with some errors." );
        }
        else
        {
            addInfoMessage( "Migration finished without any error." );
        }

        return migrationResult;

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

    private void importSecurity( MigrationSummaryDTO migrationSummary, ArtifactorySecurityConfig cfg )
    {
        addInfoMessage( "Importing security" );
        List<ArtifactoryUser> userList = new ArrayList<ArtifactoryUser>();

        for ( UserResolutionDTO userResolution : migrationSummary.getUsersResolution() )
        {
            addInfoMessage( "Importing user: " + userResolution.getUserId() );

            ArtifactoryUser user =
                new ArtifactoryUser( userResolution.getUserId(), userResolution.getPassword(),
                                     userResolution.getEmail() );

            user.setAdmin( userResolution.isAdmin() );

            userList.add( user );
        }

        // clear the users in the config, so we only import what is in the summary
        cfg.getUsers().clear();

        cfg.getUsers().addAll( userList );

        SecurityConfigConvertor convertor =
            new SecurityConfigConvertor( cfg, securityConfigAdaptorPersistor, mappingConfiguration,
                                         this.migrationResult );

        convertor.setResolvePermission( migrationSummary.isResolvePermission() );

        // returns a result of the migration, which may contain errors and warnings.
        MigrationResult result = convertor.convert();

        this.migrationResult.mergeResult( result );
    }

    private void importRepositories( MigrationSummaryDTO migrationSummary, ArtifactoryConfig cfg,
                                     File artifactoryBackupDir )
    {
        addInfoMessage( "Importing repositories" );

        final File repositoriesBackup = new File( artifactoryBackupDir, "repositories" );

        final Map<String, ArtifactoryRepository> artifactoryRepositories = cfg.getRepositories();
        final Map<String, ArtifactoryProxy> artifactoryProxies = cfg.getProxies();
        final List<RepositoryResolutionDTO> repositories = migrationSummary.getRepositoriesResolution();

        for ( RepositoryResolutionDTO resolution : repositories )
        {
            addInfoMessage( "Importing repository: " + resolution.getRepositoryId() );

            final ArtifactoryRepository repo = artifactoryRepositories.get( resolution.getRepositoryId() );

            try
            {

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
                        Repository nexusRepoReleases = createRepository( repo, artifactoryProxies, false, "releases" );
                        Repository nexusRepoSnapshots = createRepository( repo, artifactoryProxies, true, "snapshots" );

                        CRepositoryGroup nexusGroup =
                            createGroup( repo.getKey(), repo.getType(), nexusRepoReleases.getId(),
                                         nexusRepoSnapshots.getId() );

                        if ( resolution.isCopyCachedArtifacts() )
                        {
                            File repositoryBackup = new File( repositoriesBackup, repo.getKey() );
                            copyArtifacts( nexusRepoSnapshots, nexusRepoReleases, repositoryBackup );
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
                        importRepository( repositoriesBackup, artifactoryProxies, resolution, repo,
                                          repo.getHandleSnapshots(), null );
                    }
                }

            }
            catch ( MigrationException e )
            {
                addErrorMessage( "Failed to import repository '" + resolution.getRepositoryId() + "': "
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

    private void importRepository( File repositoriesBackup, Map<String, ArtifactoryProxy> artifactoryProxies,
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
            copyArtifacts( nexusRepo.getId(), repositoryBackup, storage );
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

    private void importGroups( MigrationSummaryDTO migrationSummary, ArtifactoryConfig cfg )
    {
        addInfoMessage( "Importing groups" );

        final Map<String, ArtifactoryVirtualRepository> virtualRepositories = cfg.getVirtualRepositories();
        VirtualRepositoryUtil.resolveRepositories( virtualRepositories );
        final Map<String, ArtifactoryRepository> repositories = cfg.getRepositories();
        final List<GroupResolutionDTO> groups = migrationSummary.getGroupsResolution();

        for ( GroupResolutionDTO resolution : groups )
        {
            addInfoMessage( "Importing group: " + resolution.getGroupId() );

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
                RepositoryResolutionDTO repoResolution = migrationSummary.getRepositoryResolution( repoId );

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
                                addErrorMessage( "Failed to add a 'maven1' virtual repository for repository '"
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
                addErrorMessage( "Failed to create group '" + group.getGroupId() + "': " + e.getMessage(), e );
            }

            CMapping map = new CMapping( virtualRepo.getKey(), group.getGroupId(), null, null );
            try
            {
                addMapping( map );
            }
            catch ( MigrationException e )
            {
                addErrorMessage( "Unable to update mapping information", e );
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

    private void copyArtifacts( Repository nexusRepoSnapshots, Repository nexusRepoReleases, File repositoryBackup )
        throws MigrationException
    {
        addInfoMessage( "Copying cached artifacts to: " + nexusRepoReleases.getId() + ", " + nexusRepoSnapshots.getId() );

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

        createMetadatas( nexusRepoSnapshots.getId() );
        createMetadatas( nexusRepoReleases.getId() );
    }

    private void copyArtifacts( String repoId, File sourceRepositoryBackup, File destinationStorage )
        throws MigrationException
    {
        addInfoMessage( "Copying cached artifacts to: " + repoId );

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

            createMetadatas( repoId );
        }

    }

    private void createMetadatas( String repoId )
    {
        addInfoMessage( "Recreating repository metadatas " + repoId );

        ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );
        rt.setRepositoryId( repoId );
        nexusScheduler.submit( "reindex-" + repoId, rt );

        RebuildMavenMetadataTask mt = nexusScheduler.createTaskInstance( RebuildMavenMetadataTask.class );
        mt.setRepositoryId( repoId );
        nexusScheduler.submit( "rebuild-maven-metadata-" + repoId, mt );

        RebuildAttributesTask at = nexusScheduler.createTaskInstance( RebuildAttributesTask.class );
        at.setRepositoryId( repoId );
        nexusScheduler.submit( "rebuild-attributes-" + repoId, at );
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

    private File unzipArtifactoryBackup( File fileItem )
        throws IOException, ArchiverException
    {
        addInfoMessage( "Unpacking backup file" );

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
        try
        {
            Set<Logger> loggers = new HashSet<Logger>();
            loggers.add( Logger.getLogger( SecurityConfigConvertor.class ) );
            loggers.add( this.logger );

            Layout layout = new PatternLayout( "%d{MM/dd/yyyy HH:mm:ss zzz}: [%-5p] %m%n" );
            Appender appender = new DailyRollingFileAppender( layout, this.logFile, "yyyy-mm-dd" );

            for ( Logger tmpLogger : loggers )
            {
                tmpLogger.addAppender( appender );

                // set the logger to at least INFO if not already
                if ( tmpLogger.getLevel() == null || tmpLogger.getLevel().isGreaterOrEqual( Level.INFO ) )
                {
                    tmpLogger.setLevel( Level.INFO );
                }
            }
        }
        catch ( IOException e )
        {
            this.logger.error( "Failed to add Migration log appender for file: '" + logFile + "'." );
        }
    }

    protected void addInfoMessage( String msg )
    {
        logger.info( msg );
        getLogger().info( msg );
    }
    protected void addWarnMessage( String msg )
    {
        this.migrationResult.addWarningMessage( msg );
        logger.warn( msg );
        getLogger().warn( msg );
    }

    protected void addErrorMessage( String msg )
    {
        this.migrationResult.addErrorMessage( msg );
        logger.error( msg );
        getLogger().error( msg );
    }

    protected void addErrorMessage( String msg, Exception e )
    {
        this.migrationResult.addErrorMessage( msg, e );
        logger.error( msg, e );
        getLogger().error( msg, e );
    }

}
