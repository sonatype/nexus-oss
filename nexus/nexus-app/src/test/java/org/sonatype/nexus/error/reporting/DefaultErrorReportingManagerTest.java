package org.sonatype.nexus.error.reporting;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.Assert;

import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.util.ExceptionUtils;
import org.codehaus.swizzle.jira.Issue;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
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

    private File unzipHomeDir = null;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        unzipHomeDir = new File( getPlexusHomeDir(), "unzip" );
        unzipHomeDir.mkdirs();

        nexusConfig = lookup( NexusConfiguration.class );

        manager = (DefaultErrorReportingManager) lookup( ErrorReportingManager.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        cleanDir( unzipHomeDir );
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
            manager.retrieveIssues( "APR: " + request.getThrowable().getMessage(), manager.getValidJIRAUsername(),
                                    manager.getValidJIRAPassword() );

        Assert.assertNull( issues );

        manager.handleError( request );

        issues =
            manager.retrieveIssues( "APR: " + request.getThrowable().getMessage(), manager.getValidJIRAUsername(),
                                    manager.getValidJIRAPassword() );

        Assert.assertEquals( 1, issues.size() );

        manager.handleError( request );

        issues =
            manager.retrieveIssues( "APR: " + request.getThrowable().getMessage(), manager.getValidJIRAUsername(),
                                    manager.getValidJIRAPassword() );

        Assert.assertEquals( 1, issues.size() );
    }

    public void testPackageFiles()
        throws Exception
    {
        addBackupFiles( getConfHomeDir() );
        addDirectory( "test-directory", new String[] {"filename1.file", "filename2.file", "filename3.file"} );
        addDirectory( "nested-test-directory/more-nested-test-directory", new String[] { "filename1.file", "filename2.file", "filename3.file" } );

        Exception exception;

        try
        {
            throw new Exception( "Test exception" );
        }
        catch ( Exception e )
        {
            exception = e;
        }

        manager.setEnabled( true );
        manager.setJIRAProject( "NEXUS" );

        nexusConfiguration.saveConfiguration();

        ErrorReportRequest request = new ErrorReportRequest();
        request.setThrowable( exception );

        IssueSubmissionRequest subRequest =
            manager.buildRequest( request, manager.getValidJIRAUsername(), manager.isUseGlobalProxy() );

        assertEquals( "NEXUS", subRequest.getProjectId() );
        assertEquals( "APR: Test exception", subRequest.getSummary() );
        assertEquals( "The following exception occurred: " + StringDigester.LINE_SEPERATOR
            + ExceptionUtils.getFullStackTrace( exception ), subRequest.getDescription() );
        assertNotNull( subRequest.getProblemReportBundle() );

        extractZipFile( subRequest.getProblemReportBundle(), unzipHomeDir );

        assertTrue( unzipHomeDir.exists() );

        File[] files = unzipHomeDir.listFiles();

        assertNotNull( files );
        assertEquals( 6, files.length ); // TODO: was seven with the directory listing, but that was removed, as it OOM'd
        
        files = unzipHomeDir.listFiles( new FileFilter(){
            public boolean accept( File pathname )
            {
                if ( pathname.isDirectory()
                    && pathname.getName().equals( "test-directory" ) )
                {
                    return true;
                }
                
                return false;
            }
        });
        
        assertEquals( 1, files.length );
        
        files = files[0].listFiles();
        
        boolean file1found = false;
        boolean file2found = false;
        boolean file3found = false;
        for ( File file : files )
        {
            if ( file.getName().equals( "filename1.file" ) )
            {
                file1found = true;
            }
            else if ( file.getName().equals( "filename2.file" ) )
            {
                file2found = true;
            }
            else if ( file.getName().equals( "filename3.file" ) )
            {
                file3found = true;
            }
        }
        
        assertTrue( file1found && file2found && file3found );
        
        files = unzipHomeDir.listFiles( new FileFilter(){
            public boolean accept( File pathname )
            {
                if ( pathname.isDirectory()
                    && pathname.getName().equals( "nested-test-directory" ) )
                {
                    return true;
                }
                
                return false;
            }
        });
        
        files = files[0].listFiles( new FileFilter(){
           public boolean accept( File pathname )
            {
               if ( pathname.isDirectory()
                   && pathname.getName().equals( "more-nested-test-directory" ) )
               {
                   return true;
               }
               
               return false;
            } 
        });
        
        files = files[0].listFiles();
        
        file1found = false;
        file2found = false;
        file3found = false;
        for ( File file : files )
        {
            if ( file.getName().equals( "filename1.file" ) )
            {
                file1found = true;
            }
            else if ( file.getName().equals( "filename2.file" ) )
            {
                file2found = true;
            }
            else if ( file.getName().equals( "filename3.file" ) )
            {
                file3found = true;
            }
        }
        
        assertTrue( file1found && file2found && file3found );
    }

    private void addBackupFiles( File dir )
        throws Exception
    {
        new File( dir, "nexus.xml.bak" ).createNewFile();
        new File( dir, "security.xml.bak" ).createNewFile();
    }
    
    private void addDirectory( String path, String[] filenames )
        throws Exception
    {
        File confDir = new File( getConfHomeDir(), path );
        File unzipDir = new File( unzipHomeDir, path );
        confDir.mkdirs();
        unzipDir.mkdirs();
        
        for ( String filename : filenames ){
            new File( confDir, filename ).createNewFile();
        }
    }

    private void extractZipFile( File zipFile, File outputDirectory )
        throws Exception
    {
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
        task.setMessage( msg );

        // First make sure item doesn't already exist
        List<Issue> issues =
            manager.retrieveIssues( "APR: " + new RuntimeException( msg ).getMessage(), manager.getValidJIRAUsername(),
                                    manager.getValidJIRAPassword() );

        Assert.assertNull( issues );

        doCall( task );

        issues =
            manager.retrieveIssues( "APR: " + new RuntimeException( msg ).getMessage(), manager.getValidJIRAUsername(),
                                    manager.getValidJIRAPassword() );

        Assert.assertEquals( 1, issues.size() );

        doCall( task );

        issues =
            manager.retrieveIssues( "APR: " + new RuntimeException( msg ).getMessage(), manager.getValidJIRAUsername(),
                                    manager.getValidJIRAPassword() );

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
