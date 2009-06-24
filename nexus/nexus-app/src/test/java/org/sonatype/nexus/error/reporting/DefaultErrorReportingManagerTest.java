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
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.swizzle.jira.Issue;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CErrorReporting;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.util.StackTraceUtil;
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
        
        nexusConfig = ( NexusConfiguration ) lookup( NexusConfiguration.class );
        
        manager = ( DefaultErrorReportingManager ) lookup( ErrorReportingManager.class );
    }
    
    private CErrorReporting enableErrorReports() 
        throws ConfigurationException, IOException
    {
        CErrorReporting config = new CErrorReporting();
        config.setEnabled( true );
        config.setJiraUrl( "https://issues.sonatype.org" );
        config.setJiraProject( "******" );
        config.setJiraUsername( "*****" );
        config.setJiraPassword( "*****" );
        
        nexusConfig.updateErrorReporting( config );
        
        return config;
    }
    
    public void donttestJiraAccess()
        throws Exception
    {
        CErrorReporting config = enableErrorReports();
        
        ErrorReportRequest request = new ErrorReportRequest();

        try
        {
            throw new Exception( "Test exception" );
        }
        catch ( Exception e )
        {
            request.setThrowable( e );
        }
        
        //First make sure item doesn't already exist
        List<Issue> issues = manager.retrieveIssues( config, "Automated Problem Report: " + request.getThrowable().getMessage() );
        
        Assert.assertNull( issues );
        
        manager.handleError( request );
        
        issues = manager.retrieveIssues( config, "Automated Problem Report: " + request.getThrowable().getMessage() );
        
        Assert.assertEquals( 1, issues.size() );
        
        manager.handleError( request );
        
        issues = manager.retrieveIssues( config, "Automated Problem Report: " + request.getThrowable().getMessage() );
        
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
        assertEquals( "Automated Problem Report: Test exception", subRequest.getSummary() );
        assertEquals( "The following exception occurred: " + System.getProperty( "line.seperator" )
            + StackTraceUtil.getStackTraceString( exception ), subRequest.getDescription() );
        assertNotNull( subRequest.getProblemReportBundle() );
        
        extractZipFile( subRequest.getProblemReportBundle(), unzipDir );
        
        assertTrue( unzipDir.exists() );
        
        File[] files = unzipDir.listFiles();
        
        assertNotNull( files );
        assertEquals( 4, files.length );
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
            while( ( entry = zin.getNextEntry() ) != null ) 
            {
                FileOutputStream fos = new 
                  FileOutputStream( new File( outputDirectory, entry.getName() ) );
                BufferedOutputStream bos = null;
                
                try
                {
                    byte[] buffer = new byte[2048];
                    bos = new BufferedOutputStream( fos, 2048 );
                    
                    int count;
                    while ( ( count = zin.read( buffer, 0, buffer.length ) ) != -1 ) 
                    {
                        bos.write(buffer, 0, count);
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
    
    public void donttestTaskFailure()
        throws Exception
    {
        CErrorReporting config = enableErrorReports();
        
        NexusTask<?> task = ( NexusTask<?> ) lookup( SchedulerTask.class, "ExceptionTask" );

        //First make sure item doesn't already exist
        List<Issue> issues = manager.retrieveIssues( config, "Automated Problem Report: " + new RuntimeException( "Runtime exception" ).getMessage() );
        
        Assert.assertNull( issues );
        
        doCall( task );
        
        issues = manager.retrieveIssues( config, "Automated Problem Report: " + new RuntimeException( "Runtime exception" ).getMessage() );
        
        Assert.assertEquals( 1, issues.size() );
        
        doCall( task );
        
        issues = manager.retrieveIssues( config, "Automated Problem Report: " + new RuntimeException( "Runtime exception" ).getMessage() );
        
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
