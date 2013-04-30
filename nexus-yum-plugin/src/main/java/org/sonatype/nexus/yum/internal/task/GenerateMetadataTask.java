/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.yum.internal.task;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.sonatype.scheduling.TaskState.RUNNING;
import static org.sonatype.scheduling.TaskState.SLEEPING;
import static org.sonatype.scheduling.TaskState.SUBMITTED;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.routing.Manager;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.RepositoryURLBuilder;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.yum.Yum;
import org.sonatype.nexus.yum.YumRegistry;
import org.sonatype.nexus.yum.YumRepository;
import org.sonatype.nexus.yum.internal.ListFileFactory;
import org.sonatype.nexus.yum.internal.RepositoryUtils;
import org.sonatype.nexus.yum.internal.RpmListWriter;
import org.sonatype.nexus.yum.internal.RpmScanner;
import org.sonatype.nexus.yum.internal.YumRepositoryImpl;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.RunNowSchedule;
import org.sonatype.sisu.goodies.eventbus.EventBus;

/**
 * Create a yum-repository directory via 'createrepo' command line tool.
 *
 * @since 3.0
 */
@Named( GenerateMetadataTask.ID )
public class GenerateMetadataTask
    extends AbstractNexusTask<YumRepository>
    implements ListFileFactory
{

    public static final String ID = "GenerateMetadataTask";

    private static final String PACKAGE_FILE_DIR_NAME = ".packageFiles";

    private static final String CACHE_DIR_PREFIX = ".cache-";

    private static final Logger LOG = LoggerFactory.getLogger( GenerateMetadataTask.class );

    public static final String PARAM_REPO_ID = "repoId";

    public static final String PARAM_RPM_DIR = "rpmDir";

    public static final String PARAM_REPO_DIR = "repoDir";

    public static final String PARAM_VERSION = "version";

    public static final String PARAM_RPM_URL = "rpmUrl";

    public static final String PARAM_REPO_URL = "repoUrl";

    public static final String PARAM_ADDED_FILES = "addedFiles";

    public static final String PARAM_SINGLE_RPM_PER_DIR = "singleRpmPerDir";

    private final RepositoryRegistry repositoryRegistry;

    private final RepositoryURLBuilder repositoryURLBuilder;

    private final RpmScanner scanner;

    private final NexusScheduler nexusScheduler;

    private final YumRegistry yumRegistry;

    private final Manager routingManager;

    @Inject
    public GenerateMetadataTask( final EventBus eventBus,
                                 final RepositoryRegistry repositoryRegistry,
                                 final YumRegistry yumRegistry,
                                 final RepositoryURLBuilder repositoryURLBuilder,
                                 final RpmScanner scanner,
                                 final NexusScheduler nexusScheduler,
                                 final Manager routingManager)
    {
        super( eventBus, null );

        this.yumRegistry = checkNotNull( yumRegistry );
        this.nexusScheduler = checkNotNull( nexusScheduler );
        this.scanner = checkNotNull( scanner );
        this.repositoryRegistry = checkNotNull( repositoryRegistry );
        this.repositoryURLBuilder = checkNotNull( repositoryURLBuilder );
        this.routingManager = checkNotNull( routingManager );

        getParameters().put( PARAM_SINGLE_RPM_PER_DIR, Boolean.toString( true ) );
    }

    @Override
    protected YumRepository doRun()
        throws Exception
    {
        setDefaults();

        LOG.debug( "Generating Yum-Repository for '{}' ...", getRpmDir() );
        try
        {
            getRepoDir().mkdirs();

            File rpmListFile = createRpmListFile();
            new CommandLineExecutor().exec( buildCreateRepositoryCommand( rpmListFile ) );

            if ( isUseAbsoluteUrls() )
            {
                replaceUrlInRepomdXml();
            }

        }
        catch ( IOException e )
        {
            LOG.warn( "Yum metadata generation failed", e );
            throw new IOException( "Yum metadata generation failed", e );
        }
        // TODO dubious
        Thread.sleep( 100 );

        final Repository repository = findRepository();
        if ( repository != null )
        {
            final MavenRepository mavenRepository = repository.adaptToFacet( MavenRepository.class );
            if ( mavenRepository != null )
            {
                try
                {
                    routingManager.forceUpdatePrefixFile( mavenRepository );
                }
                catch ( Exception e )
                {
                    logger.warn( "Could not update Whitelist for repository '{}'", mavenRepository, e );
                }
            }
        }

        regenerateMetadataForGroups();
        return new YumRepositoryImpl( getRepoDir(), getRepositoryId(), getVersion() );
    }

    protected void setDefaults()
        throws MalformedURLException, URISyntaxException
    {
        final Repository repository = findRepository();
        if ( isBlank( getRpmDir() ) && repository != null )
        {
            setRpmDir( RepositoryUtils.getBaseDir( repository ).getAbsolutePath() );
        }
        if ( isBlank( getRpmUrl() ) && repository != null )
        {
            setRpmUrl( repositoryURLBuilder.getRepositoryContentUrl( repository ) );
        }
        if ( isBlank( getParameter( PARAM_REPO_DIR ) ) && isNotBlank( getRpmDir() ) )
        {
            setRepoDir( new File( getRpmDir() ) );
        }
        if ( isBlank( getRepoUrl() ) && isNotBlank( getRpmUrl() ) )
        {
            setRepoUrl( getRpmUrl() );
        }
    }

    private Repository findRepository()
    {
        try
        {
            return repositoryRegistry.getRepository( getRepositoryId() );
        }
        catch ( NoSuchRepositoryException e )
        {
            return null;
        }
    }

    @Override
    protected String getAction()
    {
        return "GENERATE_YUM_METADATA";
    }

    @Override
    protected String getMessage()
    {
        return format( "Generate Yum metadata of repository '%s'", getRepositoryId() );
    }

    @Override
    public boolean allowConcurrentExecution( Map<String, List<ScheduledTask<?>>> activeTasks )
    {

        if ( activeTasks.containsKey( ID ) )
        {
            int activeRunningTasks = 0;
            for ( ScheduledTask<?> scheduledTask : activeTasks.get( ID ) )
            {
                if ( RUNNING.equals( scheduledTask.getTaskState() ) )
                {
                    if ( conflictsWith( (GenerateMetadataTask) scheduledTask.getTask() ) )
                    {
                        return false;
                    }
                    activeRunningTasks++;
                }
            }
            return activeRunningTasks < yumRegistry.maxNumberOfParallelThreads();
        }

        return true;
    }

    @Override
    public boolean allowConcurrentSubmission( Map<String, List<ScheduledTask<?>>> activeTasks )
    {
        if ( activeTasks.containsKey( ID ) )
        {
            for ( ScheduledTask<?> scheduledTask : activeTasks.get( ID ) )
            {
                if ( isSubmitted( scheduledTask )
                    && conflictsWith( (GenerateMetadataTask) scheduledTask.getTask() )
                    && scheduledTask.getSchedule() instanceof RunNowSchedule )
                {
                    throw new TaskAlreadyScheduledException( scheduledTask, "Found same task in scheduler queue." );
                }
            }
        }

        return true;
    }

    private boolean isSubmitted( ScheduledTask<?> scheduledTask )
    {
        return SUBMITTED.equals( scheduledTask.getTaskState() ) || SLEEPING.equals( scheduledTask.getTaskState() );
    }

    private void regenerateMetadataForGroups()
    {
        if ( StringUtils.isBlank( getVersion() ) )
        {
            try
            {
                final Repository repository = repositoryRegistry.getRepository( getRepositoryId() );
                for ( GroupRepository groupRepository : repositoryRegistry.getGroupsOfRepository( repository ) )
                {
                    if ( yumRegistry.isRegistered( repository.getId() ) )
                    {
                        MergeMetadataTask.createTaskFor( nexusScheduler, groupRepository );
                    }
                }
            }
            catch ( NoSuchRepositoryException e )
            {
                logger.warn(
                    "Repository '{}' does not exist anymore. Backing out from triggering group merge for it.",
                    getRepositoryId()
                );
            }
        }
    }

    private boolean conflictsWith( GenerateMetadataTask task )
    {
        if ( StringUtils.equals( getRepositoryId(), task.getRepositoryId() ) )
        {
            return StringUtils.equals( getVersion(), task.getVersion() );
        }
        return false;
    }

    private File createRpmListFile()
        throws IOException
    {
        return new RpmListWriter(
            getRepositoryId(),
            new File( getRpmDir() ),
            getAddedFiles(),
            getVersion(),
            isSingleRpmPerDirectory(),
            this,
            scanner
        ).writeList();
    }

    private String getRepositoryIdVersion()
    {
        return getRepositoryId() + ( isNotBlank( getVersion() ) ? ( "-version-" + getVersion() ) : "" );
    }

    private void replaceUrlInRepomdXml()
        throws IOException
    {
        File repomd = new File( getRepoDir(), Yum.PATH_OF_REPOMD_XML );
        if ( repomd.exists() && getRepoUrl() != null )
        {
            String repomdStr = FileUtils.readFileToString( repomd );
            repomdStr = repomdStr.replace( getRpmUrl(), getRepoUrl() );
            writeStringToFile( repomd, repomdStr );
        }
    }

    private String buildCreateRepositoryCommand( File packageList )
    {
        StringBuilder commandLine = new StringBuilder( "createrepo --update --verbose --database" );
        commandLine.append( " --outputdir " ).append( getRepoDir().getAbsolutePath() );
        commandLine.append( " --pkglist " ).append( packageList.getAbsolutePath() );
        commandLine.append( " --cachedir " ).append( createCacheDir().getAbsolutePath() );
        if ( isUseAbsoluteUrls() )
        {
            commandLine.append( " --baseurl " ).append( getRpmUrl() );
        }
        commandLine.append( " " ).append( getRpmDir() );

        return commandLine.toString();
    }

    @Override
    public File getRpmListFile( String repositoryId )
    {
        return new File( createPackageDir(), getRepositoryId() + ".txt" );
    }

    private File createCacheDir()
    {
        return getCacheDir( getRepositoryIdVersion() );
    }

    private File createPackageDir()
    {
        return getCacheDir( PACKAGE_FILE_DIR_NAME );
    }

    private File getCacheDir( final String name )
    {
        final File cacheDir = new File(
            new File( yumRegistry.getTemporaryDirectory(), CACHE_DIR_PREFIX + getRepositoryId() ), name
        );
        cacheDir.mkdirs();
        return cacheDir;
    }

    private boolean isUseAbsoluteUrls()
    {
        return isNotBlank( getVersion() );
    }

    @Override
    public File getRpmListFile( String repositoryId, String version )
    {
        return new File( createPackageDir(), getRepositoryId() + "-" + version + ".txt" );
    }

    public String getRepositoryId()
    {
        return getParameter( PARAM_REPO_ID );
    }

    public void setRepositoryId( String repositoryId )
    {
        getParameters().put( PARAM_REPO_ID, repositoryId );
    }

    public String getAddedFiles()
    {
        return getParameter( PARAM_ADDED_FILES );
    }

    public void setAddedFiles( String addedFiles )
    {
        getParameters().put( PARAM_ADDED_FILES, addedFiles );
    }

    public File getRepoDir()
    {
        return new File( getParameter( PARAM_REPO_DIR ) );
    }

    public void setRepoDir( File RepoDir )
    {
        getParameters().put( PARAM_REPO_DIR, RepoDir.getAbsolutePath() );
    }

    public String getRepoUrl()
    {
        return getParameter( PARAM_REPO_URL );
    }

    public void setRepoUrl( String RepoUrl )
    {
        getParameters().put( PARAM_REPO_URL, RepoUrl );
    }

    public String getRpmDir()
    {
        return getParameter( PARAM_RPM_DIR );
    }

    public void setRpmDir( String RpmDir )
    {
        getParameters().put( PARAM_RPM_DIR, RpmDir );
    }

    public String getRpmUrl()
    {
        return getParameter( PARAM_RPM_URL );
    }

    public void setRpmUrl( String RpmUrl )
    {
        getParameters().put( PARAM_RPM_URL, RpmUrl );
    }

    public String getVersion()
    {
        return getParameter( PARAM_VERSION );
    }

    public void setVersion( String version )
    {
        getParameters().put( PARAM_VERSION, version );
    }

    public boolean isSingleRpmPerDirectory()
    {
        return Boolean.valueOf( getParameter( PARAM_SINGLE_RPM_PER_DIR ) );
    }

    public void setSingleRpmPerDirectory( boolean singleRpmPerDirectory )
    {
        getParameters().put( PARAM_SINGLE_RPM_PER_DIR, Boolean.toString( singleRpmPerDirectory ) );
    }
}
