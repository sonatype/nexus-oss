package org.sonatype.nexus.error.reporting;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.Assert;

import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.util.ExceptionUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.swizzle.jira.Issue;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CErrorReporting;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.util.StringDigester;
import org.sonatype.scheduling.SchedulerTask;

public class DefaultErrorReportingManagerTest
    extends AbstractNexusTestCase
{
    private DefaultErrorReportingManager manager;

    private NexusConfiguration nexusConfig;

    private File unzipDir = new File( getBasedir(), "target/unzipdir" );

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.deleteDirectory( unzipDir );

        nexusConfig = lookup( NexusConfiguration.class );

        manager = (DefaultErrorReportingManager) lookup( ErrorReportingManager.class );
    }

    private void enableErrorReports( boolean useProxy )
        throws ConfigurationException, IOException
    {
        manager.setEnabled( true );
        manager.setJIRAUrl( "https://issues.sonatype.org" );
        manager.setJIRAProject( "SBOX" );
        // manager.setJIRAUsername( "********" );
        // manager.setJIRAPassword( "********" );
        manager.setUseGlobalProxy( useProxy );

        nexusConfig.saveConfiguration();
    }

    private void enableProxy()
        throws ConfigurationException, IOException
    {
        RemoteProxySettings proxy = nexusConfig.getGlobalRemoteStorageContext().getRemoteProxySettings();
        proxy.setHostname( "localhost" );
        proxy.setPort( 8111 );
        proxy.setProxyAuthentication( new UsernamePasswordRemoteAuthenticationSettings( "*****", "*****" ) );

        nexusConfig.saveConfiguration();
    }

    public void testJiraAccess()
        throws Exception
    {
        // enableProxy();
        enableErrorReports( false );

        ErrorReportRequest request = new ErrorReportRequest();

        try
        {
            throw new Exception( "Test exception " + Long.toHexString( System.currentTimeMillis() ) );
        }
        catch ( Exception e )
        {
            request.setThrowable( e );
        }

        // First make sure item doesn't already exist
        List<Issue> issues =
            manager.retrieveIssues( manager.getCurrentConfiguration( false ), "APR: "
                + request.getThrowable().getMessage() );

        Assert.assertNull( issues );

        manager.handleError( request );

        issues =
            manager.retrieveIssues( manager.getCurrentConfiguration( false ), "APR: "
                + request.getThrowable().getMessage() );

        Assert.assertEquals( 1, issues.size() );

        manager.handleError( request );

        issues =
            manager.retrieveIssues( manager.getCurrentConfiguration( false ), "APR: "
                + request.getThrowable().getMessage() );

        Assert.assertEquals( 1, issues.size() );
    }

    public void testPackageFiles()
        throws Exception
    {
        addBackupFiles( CONF_HOME );

        Exception exception;

        try
        {
            throw new Exception( "Test exception" );
        }
        catch ( Exception e )
        {
            exception = e;
        }

        CErrorReporting config = new CErrorReporting();
        config.setEnabled( true );
        config.setJiraProject( "NEXUS" );

        ErrorReportRequest request = new ErrorReportRequest();
        request.setThrowable( exception );

        IssueSubmissionRequest subRequest = manager.buildRequest( config, request );

        assertEquals( "NEXUS", subRequest.getProjectId() );
        assertEquals( "APR: Test exception", subRequest.getSummary() );
        assertEquals( "The following exception occurred: " + StringDigester.LINE_SEPERATOR
            + ExceptionUtils.getFullStackTrace( exception ), subRequest.getDescription() );
        assertNotNull( subRequest.getProblemReportBundle() );

        extractZipFile( subRequest.getProblemReportBundle(), unzipDir );

        assertTrue( unzipDir.exists() );

        File[] files = unzipDir.listFiles();

        assertNotNull( files );
        assertEquals( 5, files.length );
    }

    private void addBackupFiles( File dir )
        throws Exception
    {
        new File( dir, "nexus.xml.bak" ).createNewFile();
        new File( dir, "security.xml.bak" ).createNewFile();
    }

    private void extractZipFile( File zipFile, File outputDirectory )
        throws Exception
    {
        unzipDir.mkdirs();

        FileInputStream fis = new FileInputStream( zipFile );
        ZipInputStream zin = null;

        try
        {
            zin = new ZipInputStream( new BufferedInputStream( fis ) );

            ZipEntry entry;
            while ( ( entry = zin.getNextEntry() ) != null )
            {
                FileOutputStream fos = new FileOutputStream( new File( outputDirectory, entry.getName() ) );
                BufferedOutputStream bos = null;

                try
                {
                    byte[] buffer = new byte[2048];
                    bos = new BufferedOutputStream( fos, 2048 );

                    int count;
                    while ( ( count = zin.read( buffer, 0, buffer.length ) ) != -1 )
                    {
                        bos.write( buffer, 0, count );
                    }

                }
                finally
                {
                    if ( bos != null )
                    {
                        bos.close();
                    }
                }
            }
        }
        finally
        {
            if ( zin != null )
            {
                zin.close();
            }
        }
    }

    public void testTaskFailure()
        throws Exception
    {
        enableErrorReports( false );

        String msg = "Runtime exception " + Long.toHexString( System.currentTimeMillis() );
        ExceptionTask task = (ExceptionTask) lookup( SchedulerTask.class, "ExceptionTask" );
        task.setMessage(msg);

        // First make sure item doesn't already exist
        List<Issue> issues =
            manager.retrieveIssues( manager.getCurrentConfiguration( false ), "APR: "
                + new RuntimeException( msg ).getMessage() );

        Assert.assertNull( issues );

        doCall( task );

        issues =
            manager.retrieveIssues( manager.getCurrentConfiguration( false ), "APR: "
                + new RuntimeException( msg ).getMessage() );

        Assert.assertEquals( 1, issues.size() );

        doCall( task );

        issues =
            manager.retrieveIssues( manager.getCurrentConfiguration( false ), "APR: "
                + new RuntimeException( msg ).getMessage() );

        Assert.assertEquals( 1, issues.size() );
    }

    private void doCall( NexusTask<?> task )
    {
        try
        {
            task.call();
            Thread.sleep( 100 );
        }
        catch ( Throwable t )
        {
        }
    }

}
