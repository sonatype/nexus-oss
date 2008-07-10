package org.sonatype.nexus.test.proxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.test.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class BlockProxyDownloadTest extends AbstractNexusIntegrationTest
{

    
    public static final String TEST_RELEASE_REPO = "release-proxy-repo-1";

    public BlockProxyDownloadTest()
    {
        super( REPOSITORY_RELATIVE_URL + TEST_RELEASE_REPO + "/" );
    }
    
    @Test
    public void blockProxy() throws IOException
    {
        
        ProxyRepo proxyRepo = ProxyRepo.getInstance();
        
        Gav gav =
            new Gav( this.getClass().getName(), "block-proxy-download-test", "1.1.a", null, "jar", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null );
     
        // download file
        File originalFile = this.downloadArtifact( gav, "target/downloads/original" );
        
        // blockProxy
        proxyRepo.setBlockProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, true);
        
        //change file on server
        File localFile = proxyRepo.getLocalFile( TEST_RELEASE_REPO, gav );
        // we need to edit the file now, (its just a text file)
        this.changeFile( localFile );
        
        //redownload file
        File newFile = this.downloadArtifact( gav, "target/downloads/new" );
        
        // check to see if file matches original file
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, newFile ) );
        
        // check to see if file does match new file.
        Assert.assertFalse( FileTestingUtils.compareFileSHA1s( originalFile, localFile ) );
        
        // if we don't unblock the proxy the other tests will be mad
        proxyRepo.setBlockProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, false);
        
        this.complete();
    }
    
    private void changeFile( File file ) throws IOException
    {
        PrintWriter printWriter  = new PrintWriter( new FileWriter( file ) );
        printWriter.println( "I just changed the content of this file!" );
        printWriter.close();
        
        
    }
    
    
}
