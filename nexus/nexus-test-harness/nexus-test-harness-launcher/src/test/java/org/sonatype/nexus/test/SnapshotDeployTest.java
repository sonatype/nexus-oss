package org.sonatype.nexus.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class SnapshotDeployTest
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_SNAPSHOT_REPO = "nexus-test-harness-snapshot-repo";

    public SnapshotDeployTest()
    {
        super( REPOSITORY_RELATIVE_URL + TEST_SNAPSHOT_REPO +"/" );
    }

  

    @Test
    public void deployUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( "org.sonatype.nexus.test." + this.getClass().getName(), "uploadWithGav", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, true, null );

        // file to deploy
        File fileToDeploy =
            FileTestingUtils.getTestFile( this.getClass(), gav.getArtifactId() + "." + gav.getExtension() );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        int status = DeployUtils.deployUsingGavWithRest( uploadURL, TEST_SNAPSHOT_REPO, gav, fileToDeploy );

        if ( status != HttpStatus.SC_BAD_REQUEST )
        {
            Assert.fail( "Snapshot repositories do not allow manual file upload: " + status );
        }

        boolean fileWasUploaded = true;
        try
        {
          // download it
          downloadArtifact( gav, "./target/downloaded-jars" );
        }
        catch(FileNotFoundException e)
        {
            fileWasUploaded = false;
        }
        
        Assert.assertFalse( "The file was uploaded and it should not have been.", fileWasUploaded );


        this.complete();
    }
    
    @Test
    public void deploywithPomUsingRest()
        throws HttpException, IOException
    {

        Gav gav =
            new Gav( "org.sonatype.nexus.test." + this.getClass().getName(), "uploadWithGav", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, false, null );

        // file to deploy
        File fileToDeploy =
            FileTestingUtils.getTestFile( this.getClass(), gav.getArtifactId() + "." + gav.getExtension() );
        
        File pomFile =
            FileTestingUtils.getTestFile( this.getClass(), "pom.xml" );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";
            
        int status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_SNAPSHOT_REPO, gav, fileToDeploy, pomFile );

        if ( status != HttpStatus.SC_BAD_REQUEST )
        {
            Assert.fail( "Snapshot repositories do not allow manual file upload: " + status );
        }
        
        boolean fileWasUploaded = true;
        try
        {
          // download it
          downloadArtifact( gav, "./target/downloaded-jars" );
        }
        catch(FileNotFoundException e)
        {
            fileWasUploaded = false;
        }
        
        Assert.assertFalse( "The file was uploaded and it should not have been.", fileWasUploaded );


        this.complete();
    }
}
