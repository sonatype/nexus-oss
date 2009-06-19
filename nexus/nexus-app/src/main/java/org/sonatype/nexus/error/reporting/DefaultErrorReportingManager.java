package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.swizzle.IssueSubmitter;
import org.codehaus.plexus.swizzle.JiraIssueSubmitter;
import org.codehaus.plexus.swizzle.jira.authentication.DefaultAuthenticationSource;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CErrorReporting;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.ConfigurationHelper;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.util.StackTraceUtil;

@Component( role = ErrorReportingManager.class )
public class DefaultErrorReportingManager
    implements ErrorReportingManager
{       
    @Requirement
    NexusConfiguration nexusConfig;
    
    @Requirement
    ConfigurationHelper configHelper;
    
    public void handleError( Throwable t ) 
        throws IssueSubmissionException,
            IOException
    {
        CErrorReporting errorConfig = nexusConfig.readErrorReporting();
        
        if ( errorConfig != null && errorConfig.isEnabled() )
        {
            getIssueSubmitter( errorConfig ).submitIssue( buildRequest( errorConfig, t ) );
        }
    }
    
    protected IssueSubmissionRequest buildRequest( CErrorReporting errorConfig, Throwable t ) 
        throws IOException
    {
        IssueSubmissionRequest request = new IssueSubmissionRequest();
        
        request.setProjectId( errorConfig.getJiraProject() );
        request.setSummary( "Automated Problem Report: " + t.getMessage() );
        request.setDescription( "The following exception occurred: "
            + System.getProperty( "line.seperator" )
            + StackTraceUtil.getStackTraceString( t ) );
        request.setProblemReportBundle( assembleBundle() );
        
        return request;
    }
    
    private IssueSubmitter getIssueSubmitter( CErrorReporting errorConfig )
        throws IssueSubmissionException
    {
        try
        {
            return new JiraIssueSubmitter( 
                errorConfig.getJiraUrl(), 
                new DefaultAuthenticationSource(
                    errorConfig.getJiraUsername(),
                    errorConfig.getJiraPassword() ) );
        }
        catch ( InitializationException e )
        {
            throw new IssueSubmissionException( "Unable to initalized jira issue submitter", e );
        }
    }
    
    private File assembleBundle()
        throws IOException
    {
        Set<File> confFiles = getConfigurationFiles();
        File nexusXml = getNexusConfiguration();
        
        File zipFile = getZipFile();
        
        ZipOutputStream zStream = null;
        
        try
        {
            FileOutputStream fStream = new FileOutputStream( zipFile );
            zStream = new ZipOutputStream( fStream );
            
            for ( File confFile : confFiles )
            {
                addFileToZip( confFile, zStream, null );
            }
            
            addFileToZip( nexusXml, zStream, "nexus.xml" );
        }
        finally
        {
            nexusXml.delete();
            
            if ( zStream != null )
            {
                zStream.close();
            }
        }
        
        return zipFile;
    }
    
    private void addFileToZip( File file, ZipOutputStream zStream, String filename )
        throws IOException
    {
        byte[] buffer = new byte[1024];
        
        FileInputStream inStream = null;
        
        try
        {
            inStream = new FileInputStream( file );
            
            zStream.putNextEntry( new ZipEntry( filename != null ? filename : file.getName() ) );
            
            int len;
            while ( (len = inStream.read( buffer ) ) > 0) 
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
    
    private Set<File> getConfigurationFiles()
    {
        Set<File> files = new HashSet<File>();
        
        File confDir = nexusConfig.getWorkingDirectory( "conf" );
        
        File[] confFiles = confDir.listFiles( new FileFilter(){
            public boolean accept( File pathname )
            {
                return !pathname.getName().endsWith( ".bak" )
                    && !pathname.getName().endsWith( "nexus.xml" );
            }
        });
        
        files.addAll( Arrays.asList( confFiles ) );
        
        return files;
    }
    
    private File getNexusConfiguration()
        throws IOException
    {
        Configuration configuration = configHelper.clone( nexusConfig.getConfiguration() );
        
        configHelper.maskPasswords( configuration );
        
        NexusConfigurationXpp3Writer writer = new NexusConfigurationXpp3Writer();
        FileWriter fWriter = null;
        File tempFile = null;
        
        try
        {
            tempFile = new File( nexusConfig.getTemporaryDirectory(), "nexus.xml." + System.currentTimeMillis() );
            fWriter = new FileWriter( tempFile );
            writer.write( fWriter, configuration );
        }
        finally
        {
            if ( fWriter != null )
            {
                fWriter.close();
            }
        }
        
        return tempFile;
    }
    
    private File getZipFile()
    {
        File zipDir = nexusConfig.getWorkingDirectory( "error-report-bundles" );
        
        if ( !zipDir.exists() )
        {
            zipDir.mkdirs();
        }
         
        return new File( zipDir, "nexus-error-bundle." + System.currentTimeMillis() + ".zip" );
    }
}
