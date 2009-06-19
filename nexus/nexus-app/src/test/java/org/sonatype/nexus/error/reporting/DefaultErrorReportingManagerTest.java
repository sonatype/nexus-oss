package org.sonatype.nexus.error.reporting;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.model.CErrorReporting;
import org.sonatype.nexus.util.StackTraceUtil;

public class DefaultErrorReportingManagerTest
    extends AbstractNexusTestCase
{
    private DefaultErrorReportingManager manager;
    
    private File unzipDir = new File( getBasedir(), "target/unzipdir" );
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        FileUtils.deleteDirectory( unzipDir );
        
        manager = ( DefaultErrorReportingManager ) lookup( ErrorReportingManager.class );
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
        
        IssueSubmissionRequest request = manager.buildRequest( config, exception );
        
        assertEquals( "NEXUS", request.getProjectId() );
        assertEquals( "Automated Problem Report: Test exception", request.getSummary() );
        assertEquals( "The following exception occurred: " + System.getProperty( "line.seperator" )
            + StackTraceUtil.getStackTraceString( exception ), request.getDescription() );
        assertNotNull( request.getProblemReportBundle() );
        
        extractZipFile( request.getProblemReportBundle(), unzipDir );
        
        assertTrue( unzipDir.exists() );
        
        File[] files = unzipDir.listFiles();
        
        assertNotNull( files );
        assertEquals( 2, files.length );
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
}
