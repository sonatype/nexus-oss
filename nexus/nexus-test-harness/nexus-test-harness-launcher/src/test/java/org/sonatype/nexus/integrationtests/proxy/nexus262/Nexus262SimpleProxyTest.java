package org.sonatype.nexus.integrationtests.proxy.nexus262;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;


public class Nexus262SimpleProxyTest extends AbstractNexusProxyIntegrationTest
{

    public Nexus262SimpleProxyTest()
    {
        super( "release-proxy-repo-1" );
    }
    
    @Test
    public void downloadFromProxy() throws IOException
    {
        File localFile = this.getLocalFile( "release-proxy-repo-1", "simple.artifact", "simpleXMLArtifact", "1.0.0", "xml" );
                                                                                              
        log.debug( "localFile: "+ localFile.getAbsolutePath() );
        
        File artifact = this.downloadArtifact( "simple.artifact", "simpleXMLArtifact", "1.0.0", "xml", "target/downloads" );
        
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( artifact, localFile ) );
    }

}
