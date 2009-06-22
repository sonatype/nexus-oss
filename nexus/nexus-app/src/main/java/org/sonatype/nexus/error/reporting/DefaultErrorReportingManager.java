package org.sonatype.nexus.error.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;

import com.thoughtworks.xstream.XStream;

@Component( role = ErrorReportingManager.class )
public class DefaultErrorReportingManager
    implements ErrorReportingManager
{       
    @Requirement
    NexusConfiguration nexusConfig;
    
    @Requirement( role = SecurityModelConfigurationSource.class, hint = "file" )
    SecurityModelConfigurationSource securityXml;
    
    @Requirement( role = SecurityConfigurationSource.class, hint = "file" )
    SecurityConfigurationSource securityConfigurationXml;
    
    @Requirement
    ConfigurationHelper configHelper;
    
    private static final String ERROR_REPORT_DIR = "error-report-bundles";
    
    /**
     * XStream is used for a deep clone (TODO: not sure if this is a great idea)
     */
    private static XStream xstream = new XStream();
    
    private static final String PASSWORD_MASK = "*****";
    
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
    
    public File assembleBundle()
        throws IOException
    {
        File nexusXml = getNexusXml();
        File securityXml = getSecurityXml();
        File securityConfigurationXml = getSecurityConfigurationXml();
        File fileListing = getFileListing();
        
        File zipFile = getZipFile();
        
        ZipOutputStream zStream = null;
        
        try
        {
            FileOutputStream fStream = new FileOutputStream( zipFile );
            zStream = new ZipOutputStream( fStream );
            
            addFileToZip( nexusXml, zStream, "nexus.xml" );
            addFileToZip( securityXml, zStream, "security.xml" );
            addFileToZip( securityConfigurationXml, zStream, "security-configuration.xml" );
            addFileToZip( fileListing, zStream, "fileListing.txt" );
            
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
            if ( nexusXml != null )
            {
                nexusXml.delete();
            }
            
            if ( securityXml != null )
            {
                securityXml.delete();
            }
            
            if ( securityConfigurationXml != null )
            {
                securityConfigurationXml.delete();
            }
            
            if ( fileListing != null )
            {
                fileListing.delete();
            }
            
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
        if ( file != null && file.exists() )
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
        
        File[] confFiles = confDir.listFiles( new FileFilter(){
            public boolean accept( File pathname )
            {
                return !pathname.getName().endsWith( ".bak" )
                    && !pathname.getName().endsWith( "nexus.xml" )
                    && !pathname.getName().endsWith( "security.xml" )
                    && !pathname.getName().endsWith( "security-configuration.xml" );
            }
        });
        
        files.addAll( Arrays.asList( confFiles ) );
        
        return files;
    }
    
    private File getNexusXml()
        throws IOException
    {
        Configuration configuration = configHelper.clone( nexusConfig.getConfiguration() );
        
        // No config ?
        if ( configuration == null )
        {
            return null;
        }
        
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
    
    private File getSecurityXml()
        throws IOException
    {
        org.sonatype.security.model.Configuration configuration = 
            ( org.sonatype.security.model.Configuration )cloneViaXml( securityXml.getConfiguration() );
        
        // No config ?
        if ( configuration == null )
        {
            return null;
        }
        
        for ( CUser user : ( List<CUser> ) configuration.getUsers() )
        {
            user.setPassword( PASSWORD_MASK );
            user.setEmail( PASSWORD_MASK );
        }
        
        SecurityConfigurationXpp3Writer writer = new SecurityConfigurationXpp3Writer();
        FileWriter fWriter = null;
        File tempFile = null;
        
        try
        {
            tempFile = new File( nexusConfig.getTemporaryDirectory(), "security.xml." + System.currentTimeMillis() );
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
    
    private File getSecurityConfigurationXml()
        throws IOException
    {
        SecurityConfiguration configuration = ( SecurityConfiguration )cloneViaXml( securityConfigurationXml.getConfiguration() );
        
        // No config ??
        if ( configuration == null )
        {
            return null;
        }
        
        configuration.setAnonymousPassword( PASSWORD_MASK );
        
        org.sonatype.security.configuration.model.io.xpp3.SecurityConfigurationXpp3Writer writer = 
            new org.sonatype.security.configuration.model.io.xpp3.SecurityConfigurationXpp3Writer();
        
        FileWriter fWriter = null;
        File tempFile = null;
        
        try
        {
            tempFile = new File( nexusConfig.getTemporaryDirectory(), "security-configuration.xml." + System.currentTimeMillis() );
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
    
    private File getFileListing() 
        throws IOException
    {
        File tempFile = null;
        
        String fileListing = FileListingHelper.buildFileListing( 
            nexusConfig.getWorkingDirectory() );
        
        BufferedWriter bWriter = null;
        
        try
        {
            tempFile = new File( nexusConfig.getTemporaryDirectory(), "filteListing.txt." + System.currentTimeMillis() );
            
            bWriter = new BufferedWriter( new FileWriter( tempFile ) );

            bWriter.write( fileListing );
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
    
    private Object cloneViaXml( Object configuration )
    {
        if ( configuration == null )
        {
            return null;
        }
        
        return xstream.fromXML( xstream.toXML( configuration ) );
    }
    
    private File getZipFile()
    {
        File zipDir = nexusConfig.getWorkingDirectory( ERROR_REPORT_DIR );
        
        if ( !zipDir.exists() )
        {
            zipDir.mkdirs();
        }
         
        return new File( zipDir, "nexus-error-bundle." + System.currentTimeMillis() + ".zip" );
    }
}
