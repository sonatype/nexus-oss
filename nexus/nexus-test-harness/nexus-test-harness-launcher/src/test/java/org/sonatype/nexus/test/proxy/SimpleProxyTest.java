package org.sonatype.nexus.test.proxy;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.test.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;


public class SimpleProxyTest extends AbstractNexusIntegrationTest
{

    public SimpleProxyTest()
    {
        super( "content/repositories/release-proxy-repo-1/" );
    }
    
    @Test
    public void downloadFromProxy() throws IOException
    {
        File localFile = ProxyRepo.getInstance().getLocalFile( "release-proxy-repo-1", "simple.artifact", "simpleXMLArtifact", "1.0.0", "xml" );
                                                                                              
        System.out.println( "localFile: "+ localFile.getAbsolutePath() );
        
        File artifact = this.downloadArtifact( "simple.artifact", "simpleXMLArtifact", "1.0.0", "xml", "target/downloads" );
        
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( artifact, localFile ) );
        this.complete();
    }

}
