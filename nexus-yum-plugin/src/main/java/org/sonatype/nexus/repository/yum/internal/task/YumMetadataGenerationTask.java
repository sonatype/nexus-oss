/**
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
package org.sonatype.nexus.repository.yum.internal.task;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.sonatype.nexus.repository.yum.YumRepository.REPOMD_XML;
import static org.sonatype.nexus.repository.yum.YumRepository.YUM_REPOSITORY_DIR_NAME;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.yum.YumRepository;
import org.sonatype.nexus.repository.yum.internal.ListFileFactory;
import org.sonatype.nexus.repository.yum.internal.RepositoryUtils;
import org.sonatype.nexus.repository.yum.internal.RpmListWriter;
import org.sonatype.nexus.repository.yum.internal.YumRepositoryGenerateEvent;
import org.sonatype.nexus.repository.yum.internal.YumRepositoryImpl;
import org.sonatype.nexus.repository.yum.internal.config.YumPluginConfiguration;
import org.sonatype.nexus.rest.RepositoryURLBuilder;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import com.google.common.annotations.VisibleForTesting;

/**
 * Create a yum-repository directory via 'createrepo' command line tool.
 *
 * @author sherold
 */
@Component( role = SchedulerTask.class, hint = YumMetadataGenerationTask.ID, instantiationStrategy = "per-lookup" )
public class YumMetadataGenerationTask
    extends AbstractNexusTask<YumRepository>
    implements ListFileFactory
{

    public static final String ID = "YumMetadataGenerationTask";

    private static final String PACKAGE_FILE_DIR_NAME = ".packageFiles";

    private static final String CACHE_DIR_PREFIX = ".cache-";

    private static final Logger LOG = LoggerFactory.getLogger( YumMetadataGenerationTask.class );

    public static final String PARAM_REPO_ID = "yumMetadataGenerationRepoId";

    public static final String PARAM_RPM_DIR = "yumMetadataGenerationRpmDir";

    public static final String PARAM_REPO_DIR = "yumMetadataGenerationRepoDir";

    public static final String PARAM_VERSION = "yumMetadataGenerationVersion";

    public static final String PARAM_CACHE_DIR = "yumMetadataGenerationCacheDir";

    public static final String PARAM_RPM_URL = "yumMetadataGenerationRpmUrl";

    public static final String PARAM_REPO_URL = "yumMetadataGenerationRepoUrl";

    public static final String PARAM_ADDED_FILES = "yumMetadataGenerationAddedFiles";

    public static final String PARAM_SINGLE_RPM_PER_DIR = "yumMetadataGenerationSingleRpmPerDir";

    private final EventBus eventBus;

    private final RepositoryRegistry repositoryRegistry;

    private final YumPluginConfiguration yumConfig;

    private final RepositoryURLBuilder repositoryURLBuilder;

    @VisibleForTesting
    protected YumMetadataGenerationTask()
    {
        this.eventBus = null;
        this.repositoryRegistry = null;
        this.yumConfig = null;
        this.repositoryURLBuilder = null;
    }

    @Inject
    public YumMetadataGenerationTask( final EventBus eventBus,
                                      final RepositoryRegistry repositoryRegistry,
                                      final YumPluginConfiguration yumConfig,
                                      final RepositoryURLBuilder repositoryURLBuilder )
    {
        super( null );

        this.eventBus = checkNotNull( eventBus );
        this.repositoryRegistry = checkNotNull( repositoryRegistry );
        this.yumConfig = checkNotNull( yumConfig );
        this.repositoryURLBuilder = checkNotNull( repositoryURLBuilder );

        getParameters().put( PARAM_SINGLE_RPM_PER_DIR, Boolean.toString( true ) );
    }

    @Override
    protected YumRepository doRun()
        throws Exception
    {
        if ( yumConfig.isActive() )
        {
            setDefaults();

            LOG.info( "Generating Yum-Repository for '{}' ...", getRpmDir() );
            try
            {
                getRepoDir().mkdirs();

                File rpmListFile = createRpmListFile();
                new CommandLineExecutor().exec( buildCreateRepositoryCommand( rpmListFile ) );

                replaceUrl();
            }
            catch ( IOException e )
            {
                LOG.warn( "Generating Yum-Repo failed", e );
                throw new IOException( "Generating Yum-Repo failed", e );
            }
            Thread.sleep( 100 );
            LOG.info( "Generation complete." );

            sendNotificationEvent();
            return new YumRepositoryImpl( getRepoDir(), getRepositoryId(), getVersion() );
        }

        return null;
    }

    protected void setDefaults()
    {
        final Repository repository = findRepository();
        if ( isBlank( getRpmDir() ) && repository != null )
        {
            try
            {
                setRpmDir( RepositoryUtils.getBaseDir( repository ).getAbsolutePath() );
            }
            catch ( MalformedURLException e )
            {
            }
            catch ( URISyntaxException e )
            {
            }
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
        return "Generation YUM repository metadata";
    }

    @Override
    protected String getMessage()
    {
        return "Generation YUM repository metadata";
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
                    if ( conflictsWith( (YumMetadataGenerationTask) scheduledTask.getTask() ) )
                    {
                        return false;
                    }
                    activeRunningTasks++;
                }
            }
            return activeRunningTasks < yumConfig.getMaxParallelThreadCount();
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
                    && conflictsWith( (YumMetadataGenerationTask) scheduledTask.getTask() ) )
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

    private void sendNotificationEvent()
    {
        if ( StringUtils.isBlank( getVersion() ) )
        {
            try
            {
                final Repository repository = repositoryRegistry.getRepository( getRepositoryId() );
                eventBus.post( new YumRepositoryGenerateEvent( repository ) );
            }
            catch ( NoSuchRepositoryException e )
            {
            }
        }
    }

    private boolean conflictsWith( YumMetadataGenerationTask task )
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
        return new RpmListWriter( getRepositoryId(), getRpmDir(), getAddedFiles(), getVersion(),
                                  isSingleRpmPerDirectory(), this ).writeList();
    }

    private File createCacheDir()
    {
        File cacheDir = new File( getCacheDir(), getRepositoryIdVersion() );
        cacheDir.mkdirs();
        return cacheDir;
    }

    private String getRepositoryIdVersion()
    {
        return getRepositoryId() + ( isNotBlank( getVersion() ) ? ( "-version-" + getVersion() ) : "" );
    }

    private void replaceUrl()
        throws IOException
    {
        File repomd = new File( getRepoDir(), YUM_REPOSITORY_DIR_NAME + File.separator + REPOMD_XML );
        if ( yumConfig.isActive() && repomd.exists() && getRepoUrl() != null )
        {
            String repomdStr = FileUtils.readFileToString( repomd );
            repomdStr = repomdStr.replace( getRpmUrl(), getRepoUrl() );
            writeStringToFile( repomd, repomdStr );
        }
    }

    private String buildCreateRepositoryCommand( File packageList )
    {
        String packageFile = packageList.getAbsolutePath();
        String cacheDir = createCacheDir().getAbsolutePath();
        return format( "createrepo --update -o %s -u %s  -v -d -i %s -c %s %s", getRepoDir().getAbsolutePath(),
                       getRpmUrl(), packageFile, cacheDir, getRpmDir() );
    }

    @Override
    public File getRpmListFile( String repositoryId )
    {
        return new File( createPackageDir(), getRepositoryId() + ".txt" );
    }

    private File createPackageDir()
    {
        File PackageDir = new File( getCacheDir(), PACKAGE_FILE_DIR_NAME );
        PackageDir.mkdirs();
        return PackageDir;
    }

    private File getCacheDir()
    {
        return new File( yumConfig.getBaseTempDir(), CACHE_DIR_PREFIX + getRepositoryId() );
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

    public void setRepository( Repository repository )
    {
        setRepositoryId( repository.getId() );
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
