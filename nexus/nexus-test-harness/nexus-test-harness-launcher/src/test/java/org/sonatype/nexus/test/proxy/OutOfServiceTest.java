package org.sonatype.nexus.test.proxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.test.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class OutOfServiceTest
    extends AbstractNexusIntegrationTest
{

    public static final String TEST_RELEASE_REPO = "release-proxy-repo-1";

    public OutOfServiceTest()
    {
        super( REPOSITORY_RELATIVE_URL + TEST_RELEASE_REPO + "/" );
    }

    @Test
    public void outOfServiceTest()
        throws IOException
    {

        ProxyRepo proxyRepo = ProxyRepo.getInstance();

        // get an artifact
        Gav gav =
            new Gav( this.getClass().getName(), "out-of-service", "0.1.8-four-beta18", null, "jar", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null );

        // download an artifact
        File originalFile = this.downloadArtifact( gav, "target/downloads/original" );

        // put proxy out of service
        proxyRepo.setOutOfServiceProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, true );
        
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
        proxyRepo.setOutOfServiceProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, false );
        
        // redownload artifact
        File newFile = this.downloadArtifact( gav, "target/downloads/original" );
        
        // compare the files just for kicks
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, newFile ) );

        this.complete();
    }

}
