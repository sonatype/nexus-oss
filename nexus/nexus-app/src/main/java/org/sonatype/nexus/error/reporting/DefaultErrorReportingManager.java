package org.sonatype.nexus.error.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.swizzle.IssueSubmissionResult;
import org.codehaus.plexus.swizzle.IssueSubmitter;
import org.codehaus.plexus.swizzle.JiraIssueSubmitter;
import org.codehaus.plexus.swizzle.jira.authentication.DefaultAuthenticationSource;
import org.codehaus.plexus.util.ExceptionUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.Jira;
import org.mortbay.jetty.EofException;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CErrorReporting;
import org.sonatype.nexus.configuration.model.CErrorReportingCoreConfiguration;
import org.sonatype.nexus.configuration.model.ConfigurationHelper;
import org.sonatype.nexus.util.StringDigester;
import org.sonatype.plexus.encryptor.PlexusEncryptor;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;

@Component( role = ErrorReportingManager.class )
public class DefaultErrorReportingManager
    extends AbstractConfigurable
    implements ErrorReportingManager
{
    @Requirement
    private Logger logger;

    @Requirement
    private NexusConfiguration nexusConfig;

    @Requirement( role = SecurityModelConfigurationSource.class, hint = "file" )
    private SecurityModelConfigurationSource securityXmlSource;

    @Requirement( role = ApplicationStatusSource.class )
    ApplicationStatusSource applicationStatus;

    @Requirement( role = SecurityConfigurationSource.class, hint = "file" )
    private SecurityConfigurationSource securityConfigurationXmlSource;

    @Requirement
    private ConfigurationHelper configHelper;

    @Requirement( hint = "rsa-aes" )
    private PlexusEncryptor plexusEncryptor;

    private static final String DEFAULT_USERNAME = "sonatype_problem_reporting";

    private static final String COMPONENT = "Nexus";

    private static final String ERROR_REPORT_DIR = "error-report-bundles";

    private Set<String> errorHashSet = new HashSet<String>();

    // ==

    protected Logger getLogger()
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

    public boolean isUseGlobalProxy()
    {
        return getCurrentConfiguration( false ).isUseGlobalProxy();
    }

    public void setUseGlobalProxy( boolean val )
    {
        getCurrentConfiguration( true ).setUseGlobalProxy( val );
    }

    // ==

    public ErrorReportResponse handleError( ErrorReportRequest request, String jiraUsername, String jiraPassword,
                                            boolean useGlobalHttpProxy )
        throws IssueSubmissionException, IOException, GeneralSecurityException
    {
        getLogger().error( "Detected Error in Nexus", request.getThrowable() );

        ErrorReportResponse response = new ErrorReportResponse();

        // if title is not null, this is a manual report, so we will generate regardless
        // of other checks
        if ( request.getTitle() != null
            || ( isEnabled() && shouldHandleReport( request ) && !shouldIgnore( request.getThrowable() ) ) )
        {
            IssueSubmissionRequest subRequest = buildRequest( request, jiraUsername, useGlobalHttpProxy );

            File unencryptedFile = subRequest.getProblemReportBundle();

            encryptRequest( subRequest );

            File encryptedFile = subRequest.getProblemReportBundle();

            try
            {
                // manual, no check for existing
                if ( request.getTitle() != null )
                {
                    IssueSubmissionResult result =
                        getIssueSubmitter( jiraUsername, jiraPassword ).submitIssue( subRequest );
                    response.setCreated( true );
                    response.setJiraUrl( result.getIssueUrl() );
                    renameBundle( unencryptedFile, result.getKey() );
                    getLogger().info( "Generated problem report, ticket " + result.getIssueUrl() + " was created." );
                }
                else
                {
                    List<Issue> existingIssues = retrieveIssues( subRequest.getSummary(), jiraUsername, jiraPassword );

                    if ( existingIssues == null )
                    {
                        IssueSubmissionResult result =
                            getIssueSubmitter( jiraUsername, jiraPassword ).submitIssue( subRequest );
                        response.setCreated( true );
                        response.setJiraUrl( result.getIssueUrl() );
                        renameBundle( unencryptedFile, result.getKey() );
                        getLogger().info( "Generated problem report, ticket " + result.getIssueUrl() + " was created." );
                    }
                    else
                    {
                        response.setJiraUrl( existingIssues.get( 0 ).getLink() );
                        renameBundle( unencryptedFile, existingIssues.iterator().next().getKey() );
                        getLogger().info(
                                          "Not reporting problem as it already exists in database: "
                                              + existingIssues.iterator().next().getLink() );
                    }
                }
                response.setSuccess( true );
            }
            finally
            {
                if ( encryptedFile != null )
                {
                    encryptedFile.delete();
                }
            }
        }
        else
        {
            response.setSuccess( true );
        }

        return response;
    }

    public ErrorReportResponse handleError( ErrorReportRequest request )
        throws IssueSubmissionException, IOException, GeneralSecurityException
    {
        return handleError( request, getValidJIRAUsername(), getValidJIRAPassword(), isUseGlobalProxy() );
    }

    private void encryptRequest( IssueSubmissionRequest subRequest )
        throws IOException, GeneralSecurityException
    {
        File encryptedZip = getZipFile( "nexus-error-bundle", "ezip" );

        InputStream publicKey = null;
        try
        {
            publicKey = getClass().getResourceAsStream( "/apr/public-key.txt" );

            plexusEncryptor.encrypt( subRequest.getProblemReportBundle(), encryptedZip, publicKey );
        }
        finally
        {
            IOUtil.close( publicKey );
        }

        subRequest.setProblemReportBundle( encryptedZip );
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
    protected List<Issue> retrieveIssues( String description, String jiraUsername, String jiraPassword )
    {
        Jira jira = null;

        try
        {
            jira = new Jira( getJIRAUrl() + "/rpc/xmlrpc" );
            jira.login( jiraUsername, jiraPassword );

            List<Issue> issues =
                jira.getIssuesFromTextSearchWithProject( Arrays.asList( getJIRAProject() ), "\"" + description + "\"",
                                                         20 );

            if ( !issues.isEmpty() )
            {
                return issues;
            }
        }
        catch ( Exception e )
        {
            getLogger().error( "Unable to query JIRA server to find if error report already exists", e );
        }
        finally
        {
            if ( jira != null )
            {
                try
                {
                    jira.logout();
                }
                catch ( Exception e )
                {
                }
            }
        }

        return null;
    }

    protected void renameBundle( File bundle, String jiraTicket )
        throws IOException
    {
        if ( StringUtils.isNotEmpty( jiraTicket ) )
        {
            String filename = bundle.getAbsolutePath();

            String newfilename = filename.replace( "nexus-error-bundle", "nexus-error-bundle-" + jiraTicket );

            FileUtils.rename( bundle, new File( newfilename ) );
        }
    }

    protected IssueSubmissionRequest buildRequest( ErrorReportRequest request, String username, boolean useGlobalProxy )
        throws IOException
    {
        String summary = null;

        if ( request.getTitle() != null )
        {
            summary = "MPR: " + request.getTitle();
        }
        else
        {
            summary = "APR: " + request.getThrowable().getMessage();
        }

        if ( summary.length() > 255 )
        {
            summary = summary.substring( 0, 254 );
        }

        IssueSubmissionRequest subRequest = new IssueSubmissionRequest();

        subRequest.setProjectId( getJIRAProject() );
        subRequest.setSummary( summary );
        subRequest.setProblemReportBundle( assembleBundle( request ) );
        subRequest.setReporter( username );
        subRequest.setComponent( COMPONENT );
        subRequest.setEnvironment( assembleEnvironment( request ) );

        // use description if set
        if ( request.getDescription() != null )
        {
            subRequest.setDescription( request.getDescription() );
        }
        // otherwise pull from throwable
        else if ( request.getThrowable() != null )
        {
            subRequest.setDescription( "The following exception occurred: " + StringDigester.LINE_SEPERATOR
                + ExceptionUtils.getFullStackTrace( request.getThrowable() ) );
        }

        if ( useGlobalProxy )
        {
            subRequest.setProxyConfigurator( new NexusProxyServerConfigurator(
                                                                               nexusConfig.getGlobalRemoteStorageContext(),
                                                                               getLogger() ) );
        }

        return subRequest;
    }

    private String assembleEnvironment( ErrorReportRequest request )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "Nexus Version: " );
        sb.append( applicationStatus.getSystemStatus().getVersion() );
        sb.append( StringDigester.LINE_SEPERATOR );

        sb.append( "Nexus Edition: " );
        sb.append( applicationStatus.getSystemStatus().getEditionLong() );
        sb.append( StringDigester.LINE_SEPERATOR );

        sb.append( "java.vendor: " );
        sb.append( System.getProperty( "java.vendor" ) );
        sb.append( StringDigester.LINE_SEPERATOR );

        sb.append( "java.version: " );
        sb.append( System.getProperty( "java.version" ) );
        sb.append( StringDigester.LINE_SEPERATOR );

        sb.append( "os.name: " );
        sb.append( System.getProperty( "os.name" ) );
        sb.append( StringDigester.LINE_SEPERATOR );

        sb.append( "os.version: " );
        sb.append( System.getProperty( "os.version" ) );
        sb.append( StringDigester.LINE_SEPERATOR );

        sb.append( "os.arch: " );
        sb.append( System.getProperty( "os.arch" ) );

        return sb.toString();
    }

    private IssueSubmitter getIssueSubmitter( String jiraUsername, String jiraPassword )
        throws IssueSubmissionException
    {
        try
        {
            return new JiraIssueSubmitter( getJIRAUrl(), new DefaultAuthenticationSource( jiraUsername, jiraPassword ) );
        }
        catch ( InitializationException e )
        {
            throw new IssueSubmissionException( "Unable to initalized jira issue submitter", e );
        }
    }

    public File assembleBundle( ErrorReportRequest request )
        throws IOException
    {
        File nexusXml = new NexusXmlHandler().getFile( configHelper, nexusConfig );
        File securityXml = new SecurityXmlHandler().getFile( securityXmlSource, nexusConfig );
        File securityConfigurationXml =
            new SecurityConfigurationXmlHandler().getFile( securityConfigurationXmlSource, nexusConfig );
        // File fileListing = getFileListing(); //TODO: replace with FileUtil call
        File contextListing = getContextListing( request.getContext() );
        File exceptionListing = getExceptionListing( request.getThrowable() );

        File zipFile = getZipFile( "nexus-error-bundle", "zip" );

        ZipOutputStream zStream = null;

        try
        {
            FileOutputStream fStream = new FileOutputStream( zipFile );
            zStream = new ZipOutputStream( fStream );

            addFileToZip( nexusXml, zStream, "nexus.xml" );
            addFileToZip( securityXml, zStream, "security.xml" );
            addFileToZip( securityConfigurationXml, zStream, "security-configuration.xml" );
            // addFileToZip( fileListing, zStream, "fileListing.txt" );
            addFileToZip( contextListing, zStream, "contextListing.txt" );
            addFileToZip( exceptionListing, zStream, "exception.txt" );

            for ( File confFile : getConfigurationFiles() )
            {
                addFileToZip( confFile, zStream, null );
            }

            for ( File logFile : getLogFiles() )
            {
                addFileToZip( logFile, zStream, null );
            }
        }
        finally
        {
            deleteFile( nexusXml );
            deleteFile( securityXml );
            deleteFile( securityConfigurationXml );
            // deleteFile( fileListing );
            deleteFile( contextListing );
            deleteFile( exceptionListing );

            IOUtil.close( zStream );
        }

        return zipFile;
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

    private Set<File> getLogFiles()
    {
        Set<File> files = new HashSet<File>();

        files.add( new File( nexusConfig.getWorkingDirectory( "logs" ), "nexus.log" ) );

        return files;
    }

    private Set<File> getConfigurationFiles()
    {
        Set<File> files = new HashSet<File>();

        File confDir = nexusConfig.getWorkingDirectory( "conf" );

        File[] confFiles = confDir.listFiles( new FileFilter()
        {
            public boolean accept( File pathname )
            {
                return !pathname.getName().endsWith( ".bak" ) && !pathname.getName().endsWith( "nexus.xml" )
                    && !pathname.getName().endsWith( "security.xml" )
                    && !pathname.getName().endsWith( "security-configuration.xml" );
            }
        } );

        files.addAll( Arrays.asList( confFiles ) );

        return files;
    }

    // TODO: this should be replaced with a call to FileUtil, but first we need to make sure that will not eat memory
    // too
    // private File getFileListing()
    // throws IOException
    // {
    // return writeStringToTempFile( FileListingHelper.buildFileListing( nexusConfig.getWorkingDirectory() ),
    // "fileListing.txt" );
    // }

    private File getExceptionListing( Throwable t )
        throws IOException
    {
        return writeStringToTempFile( ExceptionUtils.getFullStackTrace( t ), "exceptionListing.txt" );
    }

    private File getContextListing( Map<String, Object> context )
        throws IOException
    {
        StringBuffer sb = new StringBuffer();

        for ( String key : context.keySet() )
        {
            sb.append( "key: " + key );
            sb.append( FileListingHelper.LINE_SEPERATOR );

            Object o = context.get( key );
            sb.append( "value: " + o == null ? "null" : o.toString() );
            sb.append( FileListingHelper.LINE_SEPERATOR );
            sb.append( FileListingHelper.LINE_SEPERATOR );
        }

        return writeStringToTempFile( sb.toString(), "contextListing.txt" );
    }

    private File writeStringToTempFile( String text, String name )
        throws IOException
    {
        File tempFile = null;

        BufferedWriter bWriter = null;

        try
        {
            tempFile = new File( nexusConfig.getTemporaryDirectory(), name + "." + System.currentTimeMillis() );

            bWriter = new BufferedWriter( new FileWriter( tempFile ) );

            bWriter.write( text );
        }
        finally
        {
            if ( bWriter != null )
            {
                bWriter.close();
            }
        }

        return tempFile;
    }

    private File getZipFile( String prefix, String suffix )
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
            if ( throwable instanceof EofException )
            {
                return true;
            }
            else if ( throwable.getMessage() != null
                && ( throwable.getMessage().contains( "An exception occured writing the response entity" ) || throwable.getMessage().contains(
                                                                                                                                               "Error while handling an HTTP server call" ) ) )
            {
                return true;
            }
        }

        return false;

    }

}
