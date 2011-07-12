package org.sonatype.nexus.plugin.migration.artifactory;

import static org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryConfigFiles.ARTIFACTORY_CONF_FILE;
import static org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryConfigFiles.SECURITY_FILE;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
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
import org.sonatype.nexus.plugin.migration.artifactory.util.VirtualRepositoryUtil;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.RebuildAttributesTask;
import org.sonatype.nexus.tasks.RepairIndexTask;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;
import org.sonatype.nexus.templates.repository.ManuallyConfiguredRepositoryTemplate;
import org.sonatype.nexus.tools.repository.RepositoryConvertor;
import org.sonatype.scheduling.ScheduledTask;

@Component( role = ArtifactoryMigrator.class )
public class DefaultArtifactoryMigrator
    extends AbstractLogEnabled
    implements ArtifactoryMigrator, Initializable
{
    private static final String MAVEN2 = "maven2";

    private static final String MAVEN1 = "maven1";

    private static final NotFileFilter ARTIFACTORY_METADATA_FILE_FILTER = new NotFileFilter( new SuffixFileFilter(
        ".artifactory-metadata" ) );

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
    private MigrationLogInitializer logInitializer;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private NexusConfiguration nexusConfiguration;

    @Requirement( role = TemplateProvider.class, hint = DefaultRepositoryTemplateProvider.PROVIDER_ID )
    private DefaultRepositoryTemplateProvider repositoryTemplateProvider;

    /**
     * Map of processed migrations.
     */
    private static HashMap<String, MigrationResult> migrationResults = new HashMap<String, MigrationResult>();

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
        File backup = new File( migrationSummary.getBackupLocation() );

        // this code now looks bad... but it will make the result object easier to read.
        // maybe we should do something like the ValidationException, but i think that
        // would be ugly here too.

        boolean deleteBackup = false;

        File artifactoryBackupDir = null;
        try
        {
            // extract the zip
            if ( backup.isFile() )
            {
                deleteBackup = true;
                try
                {
                    artifactoryBackupDir = this.unzipArtifactoryBackup( result, backup );
                }
                catch ( Exception e )
                {
                    result.addErrorMessage( "Failed to extract zipfile", e );

                    return result;
                }
            }
            else
            {
                artifactoryBackupDir = backup;
            }

            // parse artifactory.config.xml
            ArtifactoryConfig cfg = null;
            try
            {
                result.addInfoMessage( "Parsing " + ARTIFACTORY_CONF_FILE );

                cfg = ArtifactoryConfig.read( new File( artifactoryBackupDir, ARTIFACTORY_CONF_FILE ) );
            }
            catch ( Exception e )
            {
                result.addErrorMessage( "Failed to read " + ARTIFACTORY_CONF_FILE + " from backup.", e );
            }

            // parse security.xml
            ArtifactorySecurityConfig securityCfg = null;
            try
            {
                result.addInfoMessage( "Parsing " + SECURITY_FILE );

                securityCfg = ArtifactorySecurityConfigBuilder.read( new File( artifactoryBackupDir, SECURITY_FILE ) );
            }
            catch ( Exception e )
            {
                result.addErrorMessage( "Failed to read " + SECURITY_FILE + " from backup.", e );
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

            try
            {
                this.nexusConfiguration.saveConfiguration();
            }
            catch ( IOException e )
            {
                result.addErrorMessage( "Error saving configuration changes.", e );
            }

            // finally, recreate metadata for all migrated reposes
            if ( result.getErrorMessages().isEmpty() )
            {
                spawnAllMetadatas( result );
            }

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
        finally
        {
            if ( artifactoryBackupDir != null && deleteBackup )
            {
                cleanBackupDir( artifactoryBackupDir );
            }
        }
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

            ArtifactoryUser user = cfg.getUserByUsername( userResolution.getUserId() );

            user.setUsername( userResolution.getUserId() );
            user.setPassword( userResolution.getPassword() );
            user.setEmail( userResolution.getEmail() );
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
                        Repository nexusRepoReleases =
                            createRepository( repo, artifactoryProxies, false, "releases", result );
                        Repository nexusRepoSnapshots =
                            createRepository( repo, artifactoryProxies, true, "snapshots", result );

                        String repoType = repo.getType();
                        if ( repoType == null )
                        {
                            repoType = MAVEN2;
                        }
                        GroupRepository nexusGroup =
                            createGroup( repo.getKey(), repoType, result, nexusRepoReleases.getId(),
                                nexusRepoSnapshots.getId() );

                        if ( resolution.isCopyCachedArtifacts() )
                        {
                            File repositoryBackup = new File( repositoriesBackup, repo.getKey() );
                            copyArtifacts( result, nexusRepoSnapshots, nexusRepoReleases, repositoryBackup );
                        }

                        if ( resolution.isMapUrls() )
                        {
                            CMapping map =
                                new CMapping( resolution.getRepositoryId(), nexusGroup.getId(),
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
                result.addErrorMessage(
                    "Failed to import repository '" + resolution.getRepositoryId() + "': " + e.getMessage(), e );
            }
        }
    }

    private GroupRepository createGroup( String groupId, String repoType, MigrationResult result,
                                         String... repositoriesIds )
        throws MigrationException
    {
        result.addInfoMessage( "Creating group " + groupId );

        CRepository group = new DefaultCRepository();

        group.setId( groupId );
        group.setName( groupId );

        group.setProviderRole( GroupRepository.class.getName() );
        group.setProviderHint( repoType );

        group.setUserManaged( true );
        group.setExposed( true );
        group.setLocalStatus( LocalStatus.IN_SERVICE.name() );

        boolean indexable;
        if ( MAVEN2.equals( repoType ) )
        {
            indexable = true;
        }
        else
        {
            indexable = false;
        }
        group.setIndexable( indexable );

        Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );
        group.setExternalConfiguration( ex );
        M2GroupRepositoryConfiguration exConf = new M2GroupRepositoryConfiguration( ex );

        List<String> members = new ArrayList<String>();
        for ( String repoId : repositoriesIds )
        {
            members.add( repoId );
        }
        exConf.setMemberRepositoryIds( members );
        try
        {
            
            ManuallyConfiguredRepositoryTemplate template = repositoryTemplateProvider
                .createManuallyTemplate( new CRepositoryCoreConfiguration( repositoryTemplateProvider
                    .getApplicationConfiguration(), group, null ) );

            Repository r = template.create();
            this.nexusConfiguration.saveConfiguration();
            return r.adaptToFacet( GroupRepository.class );
        }
        catch ( Exception e )
        {
            throw new MigrationException( "Unable to create repository group: " + groupId, e );
        }

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
                nexusRepo = repositoryRegistry.getRepository( resolution.getRepositoryId() );
            }
            catch ( NoSuchRepositoryException e )
            {
                throw new MigrationException( "Repository should already exists: " + resolution.getRepositoryId(), e );
            }
        }
        else
        {
            nexusRepo = createRepository( repo, artifactoryProxies, isSnapshot, suffix, result );

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
                                         boolean isSnapshot, String suffix, MigrationResult result )
        throws MigrationException

    {

        String repoId = repo.getKey();
        String repoName = repo.getDescription();

        if ( repoName == null )
        {
            repoName = repoId;
        }

        if ( suffix != null )
        {
            repoId += "-" + suffix;
            repoName += "-" + suffix;
        }

        result.addInfoMessage( "Creating repository " + repoId );

        CRepository nexusRepo = new DefaultCRepository();

        Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );

        nexusRepo.setLocalStatus( LocalStatus.IN_SERVICE.name() );

        nexusRepo.setId( repoId );
        nexusRepo.setName( repoName );

        nexusRepo.setProviderRole( Repository.class.getName() );
        String hint = repo.getType();
        if ( hint == null )
        {
            hint = MAVEN2;
        }
        nexusRepo.setProviderHint( hint );

        boolean indexable;
        if ( MAVEN2.equals( hint ) )
        {
            indexable = true;
        }
        else
        {
            indexable = false;
        }

        nexusRepo.setIndexable( indexable );
        nexusRepo.setUserManaged( true );
        nexusRepo.setExposed( true );
        nexusRepo.setWritePolicy( "ALLOW_WRITE" );
        nexusRepo.setBrowseable( true );

        nexusRepo.setNotFoundCacheTTL( 1440 );

        nexusRepo.setExternalConfiguration( ex );
        M2RepositoryConfiguration exConf = new M2RepositoryConfiguration( ex );

        if ( isSnapshot )
        {
            exConf.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
        }
        else
        {
            exConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        }

        nexusRepo.setLocalStorage( new CLocalStorage() );
        nexusRepo.getLocalStorage().setProvider( "file" );

        String url = repo.getUrl();
        if ( !StringUtils.isBlank( url ) )
        {
            exConf.setDownloadRemoteIndex( false );

            nexusRepo.setRemoteStorage( new CRemoteStorage() );
            nexusRepo.getRemoteStorage().setUrl( url );
            nexusRepo.getRemoteStorage().setProvider( "apacheHttpClient3x" );

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

                    nexusRepo.getRemoteStorage().setHttpProxySettings( nexusProxy );
                }
            }

            if ( !StringUtils.isBlank( repo.getUsername() ) || !StringUtils.isBlank( repo.getPassword() ) )
            {
                CRemoteAuthentication authentication = new CRemoteAuthentication();
                authentication.setUsername( repo.getUsername() );
                authentication.setPassword( repo.getPassword() );

                nexusRepo.getRemoteStorage().setAuthentication( authentication );
            }

        }


        try
        {
            ManuallyConfiguredRepositoryTemplate template = repositoryTemplateProvider
                .createManuallyTemplate( new CRepositoryCoreConfiguration( repositoryTemplateProvider
                    .getApplicationConfiguration(), nexusRepo, null ) );

            Repository r = template.create();
            this.nexusConfiguration.saveConfiguration();
            return r;
        }
        catch ( Exception e )
        {
            throw new MigrationException( "Unable to create repository group: " + repoId, e );
        }

    }

    private void importGroups( MigrationResult result, ArtifactoryConfig cfg )
        throws MigrationException
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
            String repoType;

            if ( ERepositoryTypeResolution.MAVEN_1_ONLY.equals( resolution.getRepositoryTypeResolution() ) )
            {
                repoType = MAVEN1;
            }
            else
            {
                repoType = MAVEN2;
            }

            List<String> repositoriesIds = new ArrayList<String>();

            for ( final String repoId : virtualRepo.getResolvedRepositories() )
            {
                RepositoryResolutionDTO repoResolution = result.getMigrationSummary().getRepositoryResolution( repoId );

                if ( repoResolution == null )
                {
                    // will happen if the user decide to not import any repo
                    continue;
                }

                if ( ERepositoryType.PROXY.equals( repoResolution.getType() )
                    && repoResolution.isMergeSimilarRepository() )
                {
                    repositoriesIds.add( repoResolution.getSimilarRepositoryId() );
                }
                else if ( !resolution.isMixed() )
                {
                    addRepository( repositoriesIds, repoId );
                }
                else
                {
                    ArtifactoryRepository repo = repositories.get( repoId );
                    String type = repo.getType();
                    if ( ERepositoryTypeResolution.MAVEN_1_ONLY.equals( resolution.getRepositoryTypeResolution() ) )
                    {
                        if ( MAVEN1.equals( type ) )
                        {
                            addRepository( repositoriesIds, repoId );
                        }
                    }
                    else if ( ERepositoryTypeResolution.VIRTUAL_BOTH.equals( resolution.getRepositoryTypeResolution() ) )
                    {
                        if ( MAVEN1.equals( type ) )
                        {
                            try
                            {
                                addVirtualRepository( repositoriesIds, repoId );
                            }
                            catch ( Exception e )
                            {
                                result.addErrorMessage( "Failed to add a 'maven1' virtual repository for repository '"
                                    + repoId + "': " + e.getMessage(), e );
                            }
                        }
                        else
                        {
                            addRepository( repositoriesIds, repoId );
                        }
                    }
                    else
                    // MAVEN2 only
                    {
                        if ( type == null || MAVEN2.equals( type ) )
                        {
                            addRepository( repositoriesIds, repoId );
                        }

                    }
                }
            }

            GroupRepository group =
                createGroup( virtualRepo.getKey(), repoType, result, repositoriesIds.toArray( new String[0] ) );

            CMapping map = new CMapping( virtualRepo.getKey(), group.getId(), null, null );
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

    private void addVirtualRepository( List<String> repositoriesIds, String repoId )
        throws MigrationException
    {

        CMapping map = mappingConfiguration.getMapping( repoId );

        if ( map.getNexusGroupId() == null )
        {
            ShadowRepository shadowRepo = createShadowRepo( map.getNexusRepositoryId() );
            repositoriesIds.add( shadowRepo.getId() );
        }
        else
        {
            ShadowRepository releasesRepo = createShadowRepo( map.getReleasesRepositoryId() );
            repositoriesIds.add( releasesRepo.getId() );
            ShadowRepository snapshotsRepo = createShadowRepo( map.getSnapshotsRepositoryId() );
            repositoriesIds.add( snapshotsRepo.getId() );
        }
    }

    private void addRepository( List<String> repositoriesIds, String repoId )
    {
        CMapping map = mappingConfiguration.getMapping( repoId );

        if ( map == null )
        {
            // no mapping
            return;
        }

        if ( map.getNexusGroupId() == null )
        {
            repositoriesIds.add( map.getNexusRepositoryId() );
        }
        else
        {
            repositoriesIds.add( map.getReleasesRepositoryId() );
            repositoriesIds.add( map.getSnapshotsRepositoryId() );
        }
    }

    private ShadowRepository createShadowRepo( String shadowOfRepoId )
        throws MigrationException
    {
        String shadowId = shadowOfRepoId + "-virtual";

        CRepository shadowRepo = new DefaultCRepository();
        shadowRepo.setId( shadowId );
        shadowRepo.setName( shadowId );

        shadowRepo.setExposed( true );
        shadowRepo.setUserManaged( true );
        shadowRepo.setIndexable( false );

        shadowRepo.setProviderRole( ShadowRepository.class.getName() );
        shadowRepo.setProviderHint( "m1-m2-shadow" );

        Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );
        shadowRepo.setExternalConfiguration( ex );

        M2LayoutedM1ShadowRepositoryConfiguration exConf = new M2LayoutedM1ShadowRepositoryConfiguration( ex );
        exConf.setMasterRepositoryId( shadowOfRepoId );
        exConf.setSynchronizeAtStartup( true );

        try
        {
            ManuallyConfiguredRepositoryTemplate template = repositoryTemplateProvider
                .createManuallyTemplate( new CRepositoryCoreConfiguration( repositoryTemplateProvider
                    .getApplicationConfiguration(), shadowRepo, null ) );
        
            Repository r = template.create();
            this.nexusConfiguration.saveConfiguration();
            return r.adaptToFacet( ShadowRepository.class );
        }
        catch ( Exception e )
        {
            throw new MigrationException( "Unable to create repository group: " + shadowId, e );
        }
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
                getStorage( nexusRepoSnapshots ), ARTIFACTORY_METADATA_FILE_FILTER );
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

            try
            {
                repositoryRegistry.getRepository( repoId );
            }
            catch ( NoSuchRepositoryException e )
            {
                // this should never ever happen, repo should be created before this point
                result.addErrorMessage( "The repository '" + repoId + "' does not exists! Unable to create metadatas!",
                    e );
                continue;
            }

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

            RepairIndexTask rt = nexusScheduler.createTaskInstance( RepairIndexTask.class );
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

        URL storageURI;
        try
        {
            storageURI = new URL( localStorage );
        }
        catch ( MalformedURLException e )
        {
            throw new MigrationException( "Invalid repository URL! " + localStorage, e );
        }

        File storage = new File( storageURI.getFile() );

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
        logInitializer.initialize();
    }

}
