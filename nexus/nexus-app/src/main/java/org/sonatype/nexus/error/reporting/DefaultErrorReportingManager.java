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
package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.swizzle.IssueSubmissionResult;
import org.codehaus.plexus.swizzle.IssueSubmitter;
import org.codehaus.plexus.swizzle.jira.authentication.AuthenticationSource;
import org.codehaus.plexus.swizzle.jira.authentication.DefaultAuthenticationSource;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.jira.AttachmentHandlerConfiguration;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CErrorReporting;
import org.sonatype.nexus.configuration.model.CErrorReportingCoreConfiguration;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;
import org.sonatype.nexus.util.StringDigester;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.sisu.issue.IssueRetriever;
import org.sonatype.sisu.pr.ProjectManager;
import org.sonatype.sisu.pr.bundle.Archiver;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.BundleManager;
import sun.applet.AppletEventMulticaster;

@Named
public class DefaultErrorReportingManager
    extends AbstractConfigurable
    implements ErrorReportingManager
{

    private Logger logger = LoggerFactory.getLogger( getClass() );

    ApplicationStatusSource applicationStatus;

    private NexusConfiguration nexusConfig;

    private IssueSubmitter issueSubmitter;

    private IssueRetriever issueRetriever;

    private BundleManager assembler;

    private Archiver archiver;

    private AttachmentHandlerConfiguration remoteCfg;

    private ProjectManager projectManager;

    private UserAgentBuilder uaBuilder;

    private final ApplicationEventMulticaster applicationEventMulticaster;

    private static final String DEFAULT_USERNAME = "sonatype_problem_reporting";

    @VisibleForTesting
    static final String ERROR_REPORT_DIR = "error-report-bundles";

    private Set<String> errorHashSet = new HashSet<String>();

    // ==

    @Inject
    public DefaultErrorReportingManager( final ApplicationStatusSource applicationStatus, final Archiver archiver,
                                         final BundleManager assembler,
                                         final IssueRetriever issueRetriever, final IssueSubmitter issueSubmitter,
                                         final NexusConfiguration nexusConfig,
                                         final ProjectManager projectManager,
                                         final AttachmentHandlerConfiguration remoteCfg,
                                         final UserAgentBuilder uaBuilder, final ApplicationEventMulticaster applicationEventMulticaster )
    {
        this.applicationStatus = applicationStatus;
        this.archiver = archiver;
        this.assembler = assembler;
        this.issueRetriever = issueRetriever;
        this.issueSubmitter = issueSubmitter;
        this.nexusConfig = nexusConfig;
        this.projectManager = projectManager;
        this.remoteCfg = remoteCfg;
        this.uaBuilder = uaBuilder;
        this.applicationEventMulticaster = applicationEventMulticaster;
    }

    private Logger getLogger()
    {
        return logger;
    }

    // ==

    @Override
    protected void initializeConfiguration()
        throws ConfigurationException
    {
        if ( getApplicationConfiguration().getConfigurationModel() != null )
        {
            configure( getApplicationConfiguration() );
        }


        CErrorReporting config = getCurrentConfiguration( false );
        if ( config != null )
        {
            issueSubmitter.setServerUrl( config.getJiraUrl() );
            issueRetriever.setServerUrl( config.getJiraUrl() );
        }
//        else
//        {
//            issueSubmitter.setServerUrl( DEFAULT_URL );
//            issueRetriever.setServerUrl( DEFAULT_URL );
//        }

        AuthenticationSource credentials =
            new DefaultAuthenticationSource( getValidJIRAUsername(), getValidJIRAPassword() );
        issueSubmitter.setCredentials( credentials );
        issueRetriever.setCredentials( credentials );
    }

    @Override
    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return nexusConfig;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return null;
    }

    @Override
    protected CErrorReporting getCurrentConfiguration( boolean forWrite )
    {
        return ( (CErrorReportingCoreConfiguration) getCurrentCoreConfiguration() ).getConfiguration( forWrite );
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException
    {
        if ( configuration instanceof ApplicationConfiguration )
        {
            return new CErrorReportingCoreConfiguration( getApplicationConfiguration() );
        }
        else
        {
            throw new ConfigurationException( "The passed configuration object is of class \""
                                                  + configuration.getClass().getName() + "\" and not the required \""
                                                  + ApplicationConfiguration.class.getName() + "\"!" );
        }
    }

    // ==

    public boolean isEnabled()
    {
        return getCurrentConfiguration( false ).isEnabled();
    }

    public void setEnabled( boolean value )
    {
        getCurrentConfiguration( true ).setEnabled( value );
    }

    public String getJIRAUrl()
    {
        return getCurrentConfiguration( false ).getJiraUrl();
    }

    public void setJIRAUrl( String url )
    {
        getCurrentConfiguration( true ).setJiraUrl( url );
        issueSubmitter.setServerUrl( url );
        issueRetriever.setServerUrl( url );
    }

    public String getJIRAUsername()
    {
        return getCurrentConfiguration( false ).getJiraUsername();
    }

    protected String getValidJIRAUsername()
    {
        String username = getJIRAUsername();

        if ( StringUtils.isEmpty( username ) )
        {
            username = DEFAULT_USERNAME;
        }

        return username;
    }

    public void setJIRAUsername( String username )
    {
        getCurrentConfiguration( true ).setJiraUsername( username );
    }

    public String getJIRAPassword()
    {
        return getCurrentConfiguration( false ).getJiraPassword();
    }

    protected String getValidJIRAPassword()
    {
        String password = getJIRAPassword();

        if ( StringUtils.isEmpty( password ) )
        {
            password = DEFAULT_USERNAME;
        }

        return password;
    }

    public void setJIRAPassword( String password )
    {
        getCurrentConfiguration( true ).setJiraPassword( password );
    }

    public String getJIRAProject()
    {
        return getCurrentConfiguration( false ).getJiraProject();
    }

    public void setJIRAProject( String pkey )
    {
        getCurrentConfiguration( true ).setJiraProject( pkey );
    }

    /**
     * the useGlobalProxy config is always ignored <br/>
     * TODO: remove this config? <br/>
     */
    public boolean isUseGlobalProxy()
    {
        return true;
    }

    public void setUseGlobalProxy( boolean val )
    {
        getCurrentConfiguration( true ).setUseGlobalProxy( val );
    }

    // ==

    @Override
    public ErrorReportResponse handleError( ErrorReportRequest request )
        throws IssueSubmissionException, IOException, GeneralSecurityException
    {
        return handleError( request, getJIRAUsername(), getJIRAPassword(), true );
    }

    public ErrorReportResponse handleError( ErrorReportRequest request, String username, String password,
                                            boolean useGlobalHttpProxy )
        throws IssueSubmissionException, IOException, GeneralSecurityException
    {
        Preconditions.checkState( username != null, "No username for error reporting given" );
        Preconditions.checkState( password != null, "No password for error reporting given" );

        return handleError( request, new DefaultAuthenticationSource( username, password ) );
    }

    public ErrorReportResponse handleError( final ErrorReportRequest request, final AuthenticationSource auth )
    {
        getLogger().error( "Detected Error in Nexus", request.getThrowable() );

        ErrorReportResponse response = new ErrorReportResponse();

        try
        {
            if ( request.isManual() )
            {
                IssueSubmissionRequest subRequest = buildRequest( request, auth.getLogin(), true );
                submitIssue( auth, response, subRequest );
            }
            else if ( ( isEnabled() && shouldHandleReport( request ) 
                && !shouldIgnore( request.getThrowable() ) ) )
            {
                IssueSubmissionRequest subRequest = buildRequest( request, auth.getLogin(), true );

                List<Issue> existingIssues = retrieveIssues( subRequest.getSummary(), auth );

                if ( existingIssues.isEmpty() )
                {
                    submitIssue( auth, response, subRequest );
                }
                else
                {
                    response.setJiraUrl( existingIssues.get( 0 ).getLink() );
                    writeArchive( subRequest.getBundles(), existingIssues.get( 0 ).getKey() );
                    getLogger().info(
                        "Not reporting problem as it already exists in database: "
                            + existingIssues.iterator().next().getLink() );
                }
            }
            response.setSuccess( true );
        }
        catch ( Exception e )
        {
            getLogger().warn( "Error while submitting problem report: " + e.getMessage() );
            getLogger().debug( e.getMessage(), e );
        }

        return response;
    }

    private void submitIssue( final AuthenticationSource auth, final ErrorReportResponse response,
                              final IssueSubmissionRequest subRequest )
        throws IssueSubmissionException, IOException
    {
        IssueSubmissionResult result = issueSubmitter.submit( subRequest, auth );
        response.setCreated( true );
        response.setJiraUrl( result.getIssueUrl() );
        writeArchive( result.getBundles(), result.getKey() );
        getLogger().info( "Problem report ticket " + result.getIssueUrl() + " was created." );
    }

    protected boolean shouldHandleReport( ErrorReportRequest request )
    {
        // if there is a title, we are talking about user generated, simply use it
        if ( request.getTitle() != null )
        {
            return true;
        }

        if ( request.getThrowable() != null && StringUtils.isNotEmpty( request.getThrowable().getMessage() ) )
        {
            String hash = StringDigester.getSha1Digest( request.getThrowable().getMessage() );

            if ( errorHashSet.contains( hash ) )
            {
                getLogger().debug( "Received an exception we already processed, ignoring." );
                return false;
            }
            else
            {
                errorHashSet.add( hash );
                return true;
            }
        }
        else
        {
            getLogger().debug( "Received an empty message in exception, will not handle" );
        }

        return false;
    }

    @SuppressWarnings( "unchecked" )
    protected List<Issue> retrieveIssues( String description, AuthenticationSource auth )
    {
        try
        {
            issueRetriever.setCredentials( auth );
            Project project = issueRetriever.getProject( projectManager.getProject( null ) );
            List<Issue> issues = issueRetriever.getIssues( "\"" + description + "\"", project );
            return issues;
        }
        catch ( Exception e )
        {
            getLogger().error( "Unable to query JIRA server to find if error report already exists", e );
            return null;
        }

    }

    protected IssueSubmissionRequest buildRequest( ErrorReportRequest request, String username, boolean useGlobalProxy )
    {
        IssueSubmissionRequest subRequest = new IssueSubmissionRequest();

        subRequest.setContext( request );
        subRequest.setError( request.getThrowable() );

        subRequest.setProjectId( getJIRAProject() );
//        subRequest.setProblemReportBundle( assembleBundle( request ) );
//        subRequest.setEnvironment( assembleEnvironment( request ) );

        // use description if set
        if ( request.isManual() )
        {
            subRequest.setSummary( request.getTitle() );
            subRequest.setDescription( request.getDescription() );
        }

        if ( useGlobalProxy )
        {
            final RemoteStorageContext ctx = nexusConfig.getGlobalRemoteStorageContext();
            final String ua = uaBuilder.formatUserAgentString( ctx );
            remoteCfg.setUserAgent( ua );

            final RemoteProxySettings proxySettings = ctx.getRemoteProxySettings();
            final RemoteAuthenticationSettings proxyAuthentication = proxySettings.getProxyAuthentication();

            if ( proxySettings.isEnabled() )
            {
                if ( proxyAuthentication instanceof UsernamePasswordRemoteAuthenticationSettings )
                {
                    final UsernamePasswordRemoteAuthenticationSettings auth =
                        (UsernamePasswordRemoteAuthenticationSettings) proxyAuthentication;

                    remoteCfg.setProxyHost( proxySettings.getHostname() );
                    remoteCfg.setProxyPort( proxySettings.getPort() );
//                    remoteCfg.setNonProxyHosts( proxySettings.getNonProxyHosts() );
                    remoteCfg.setProxyPrincipal( auth.getUsername() );
                    remoteCfg.setProxyPassword( auth.getPassword() );
                }
                else
                {
                    logger.warn( "Proxy type unsupported for problem reporting. Trying without proxy..." );
                }
            }
        }

        return subRequest;
    }

    @VisibleForTesting
    File writeArchive( Collection<Bundle> bundles, String suffix )
        throws IOException
    {
        Bundle bundle;
        if ( !( bundles.size() == 1 && "application/zip".equals(
            ( bundle = bundles.iterator().next() ).getContentType() ) ) )
        {
            bundle = archiver.createArchive( bundles );
        }
        
        File zipFile = getZipFile( "nexus-error-bundle-" + suffix, "zip" );
        OutputStream output = null;
        InputStream input = null;

        try
        {
            output = new FileOutputStream( zipFile );
            input = bundle.getInputStream();
            IOUtil.copy( input, output );
        }
        finally
        {
            IOUtil.close( input );
            IOUtil.close( output );
        }
        
        return zipFile;
    }

    private void addDirToZip( File directory, ZipOutputStream zStream, String path )
        throws IOException
    {
        if ( directory != null && directory.isDirectory() )
        {
            File[] files = directory.listFiles();

            for ( File file : files )
            {
                String pathname =
                    path != null ? ( path + "/" + file.getName() ) : directory.getName() + "/" + file.getName();
                if ( file.isDirectory() )
                {
                    addDirToZip( file, zStream, pathname );
                }
                else
                {
                    addFileToZip( file, zStream, pathname );
                }
            }
        }
    }

    private void addFileToZip( File file, ZipOutputStream zStream, String filename )
        throws IOException
    {
        if ( file != null && file.exists() )
        {
            byte[] buffer = new byte[1024];

            FileInputStream inStream = null;

            try
            {
                inStream = new FileInputStream( file );

                zStream.putNextEntry( new ZipEntry( filename != null ? filename : file.getName() ) );

                int len;
                while ( ( len = inStream.read( buffer ) ) > 0 )
                {
                    zStream.write( buffer, 0, len );
                }

                zStream.closeEntry();
            }
            finally
            {
                if ( inStream != null )
                {
                    inStream.close();
                }
            }
        }
    }

    @VisibleForTesting
    File getZipFile( String prefix, String suffix )
    {
        File zipDir = nexusConfig.getWorkingDirectory( ERROR_REPORT_DIR );

        if ( !zipDir.exists() )
        {
            zipDir.mkdirs();
        }

        return new File( zipDir, prefix + "." + System.currentTimeMillis() + "." + suffix );
    }

    private void deleteFile( File file )
    {
        if ( file != null )
        {
            file.delete();
        }
    }

    public String getName()
    {
        return "Error Report Settings";
    }

    protected boolean shouldIgnore( Throwable throwable )
    {
        if ( throwable != null )
        {
            if ( "org.mortbay.jetty.EofException".equals( throwable.getClass().getName() )
                || "org.eclipse.jetty.io.EofException".equals( throwable.getClass().getName() ) )
            {
                return true;
            }
            else if ( throwable.getMessage() != null
                && ( throwable.getMessage().contains( "An exception occured writing the response entity" )
                || throwable.getMessage().contains(
                "Error while handling an HTTP server call" ) ) )
            {
                return true;
            }
        }

        return false;

    }

    /**
     * FIXME when switching from plexus to sisu: AbstractConfigurable is still plexus, so we need to circumvent the @Requirement on the multicaster.
     */
    @Override
    public void initialize()
        throws InitializationException
    {
        applicationEventMulticaster.addEventListener( this );

        try
        {
            initializeConfiguration();
        }
        catch ( ConfigurationException e )
        {
            throw new InitializationException( "Cannot configure the component!", e );
        }
    }
}
