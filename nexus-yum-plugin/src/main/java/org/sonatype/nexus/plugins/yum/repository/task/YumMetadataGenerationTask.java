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
package org.sonatype.nexus.plugins.yum.repository.task;

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.sonatype.nexus.plugins.yum.repository.YumRepository.REPOMD_XML;
import static org.sonatype.nexus.plugins.yum.repository.YumRepository.YUM_REPOSITORY_DIR_NAME;
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
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.plugins.yum.config.YumConfiguration;
import org.sonatype.nexus.plugins.yum.execution.CommandLineExecutor;
import org.sonatype.nexus.plugins.yum.plugin.event.YumRepositoryGenerateEvent;
import org.sonatype.nexus.plugins.yum.repository.ListFileFactory;
import org.sonatype.nexus.plugins.yum.repository.RepositoryUtils;
import org.sonatype.nexus.plugins.yum.repository.RpmListWriter;
import org.sonatype.nexus.plugins.yum.repository.YumRepository;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;

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

    public YumMetadataGenerationTask()
    {
        this( null );
    }

    public YumMetadataGenerationTask( String name )
    {
        super( name );
        getParameters().put( PARAM_SINGLE_RPM_PER_DIR, Boolean.toString( true ) );
    }

    @Requirement
    private ApplicationEventMulticaster eventMulticaster;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private YumConfiguration yumConfig;

    @Inject
    private GlobalRestApiSettings restApiSettings;

    @Override
    protected YumRepository doRun()
        throws Exception
    {
        setDefaults();
        if ( yumConfig.isActive() )
        {
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
            return new YumRepository( getRepoDir(), getRepositoryId(), getVersion() );
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
            setRpmUrl( getBaseUrl( repository ) );
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

    private String getBaseUrl( Repository repository )
    {
        return String.format( "%s/content/repositories/%s", restApiSettings.getBaseUrl(), repository.getId() );
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
                    throw new TaskDoubledException( scheduledTask, "Found same task in scheduler queue." );
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
                eventMulticaster.notifyEventListeners( new YumRepositoryGenerateEvent( repository ) );
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
