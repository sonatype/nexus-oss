package org.sonatype.nexus.integrationtests.proxy.nexus177;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class Nexus177OutOfServiceTest
    extends AbstractNexusProxyIntegrationTest
{

    public static final String TEST_RELEASE_REPO = "release-proxy-repo-1";

    public Nexus177OutOfServiceTest()
    {
        super( TEST_RELEASE_REPO );
    }

    @Test
    public void outOfServiceTest()
        throws IOException
    {

        // get an artifact
        Gav gav =
            new Gav( this.getClass().getName(), "out-of-service", "0.1.8-four-beta18", null, "jar", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null, false, null );

        // download an artifact
        File originalFile = this.downloadArtifact( gav, "target/downloads/original" );

        // put proxy out of service
        this.setOutOfServiceProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, true );
        
        // redownload artifact
        boolean fileWasDownloaded = true;
        try
        {
          // download it
          downloadArtifact( gav, "./target/downloaded-jars" );
        }
        catch(FileNotFoundException e)
        {
            fileWasDownloaded = false;
        }
        
        Assert.assertFalse( "Out Of Service Command didn't do anything.", fileWasDownloaded );

        // put proxy back in service
        this.setOutOfServiceProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, false );
        
        // redownload artifact
        File newFile = this.downloadArtifact( gav, "target/downloads/original" );
        
        // compare the files just for kicks
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, newFile ) );

    }

}
