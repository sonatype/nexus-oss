package org.sonatype.nexus.integrationtests.nexus2190;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;

public class Nexus2190ErrorReportBundleTest
    extends AbstractNexusIntegrationTest
{
    @Test
    public void validateBundle()
        throws Exception
    {
        RequestFacade.sendMessage( "service/local/error_reporting", Method.POST, null );
        
        File errorBundleDir = new File( nexusWorkDir + "/error-report-bundles" );
        
        File[] files = errorBundleDir.listFiles();
        
        Assert.assertTrue( files != null );
        Assert.assertEquals( 1, files.length );
        Assert.assertTrue( files[0].getName().startsWith( "nexus-error-bundle." ) );
        Assert.assertTrue( files[0].getName().endsWith( ".zip" ) );
        
        validateZipContents( files[0] );
    }
    
    private void validateZipContents( File file ) 
        throws IOException
    {
        boolean foundFileList = false;
        boolean foundContextList = false;
        boolean foundLog4j = false;
        boolean foundNexusXml = false;
        boolean foundSecurityXml = false;
        boolean foundSecurityConfigXml = false;
        boolean foundOthers = false;
        
        ZipFile zipFile = new ZipFile( file );
        
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        
        while ( enumeration.hasMoreElements() )
        {
            ZipEntry entry = enumeration.nextElement();
            
            if ( entry.getName().equals( "fileListing.txt" ) )
            {
                foundFileList = true;
            }
            else if ( entry.getName().equals( "contextListing.txt" ) )
            {
                foundContextList = true;
            }
            else if ( entry.getName().equals( "log4j.properties" ) )
            {
                foundLog4j = true;
            }
            else if ( entry.getName().equals( "nexus.xml" ) )
            {
                foundNexusXml = true;
            }
            else if ( entry.getName().equals( "security.xml" ) )
            {
                foundSecurityXml = true;
            }
            else if ( entry.getName().equals( "security-configuration.xml" ) )
            {
                foundSecurityConfigXml = true;
            }
            else
            {
                foundOthers = true;
            }
        }
        
        Assert.assertTrue( foundFileList );
        Assert.assertTrue( foundContextList );
        Assert.assertTrue( foundLog4j );
        Assert.assertTrue( foundNexusXml );
        Assert.assertTrue( foundSecurityXml );
        Assert.assertTrue( foundSecurityConfigXml );
        Assert.assertFalse( foundOthers );
    }
}
