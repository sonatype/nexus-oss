package org.sonatype.nexus.integrationtests.nexus429;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.UserMessageUtil;

import com.thoughtworks.xstream.XStream;

public class Nexus429AdvanceDeployTest
    extends AbstractNexusIntegrationTest
{
    private static final String TEST_RELEASE_REPO = "nexus-test-harness-release-repo";

    public Nexus429AdvanceDeployTest()
    {
        super( TEST_RELEASE_REPO );
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }


    @Test
    public void onlyDeployPriv()
        throws IOException
    {
        // GAV
        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0", null, "xml", 0, new Date().getTime(), "", false,
                     false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        File pomFile = this.getTestFile( "pom.xml" );

         UserMessageUtil messageUtil = new UserMessageUtil(XStreamInitializer.initialize( new XStream( ) ),
         MediaType.APPLICATION_XML );
        //        
        // get user
         UserResource user = messageUtil.getUser( "all-but-deploy" );
         
        // deploy
        TestContainer.getInstance().getTestContext().setUsername( "all-but-deploy" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        int status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy, pomFile );

        Assert.assertEquals( "Status should have been 401", 401, status );
        

        
        
        // give privs and do it again
        user.addRole( "deployment" );
        messageUtil.updateUser( user );
        
        status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy, pomFile );

        Assert.assertEquals( "Status should have been 201", 201, status );
        
        
        
        
        

        // assign deploy priv
        // deploy
        TestContainer.getInstance().getTestContext().setUsername( "just-deploy" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // url to upload to
        uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        status = DeployUtils.deployUsingPomWithRest( uploadURL, TEST_RELEASE_REPO, gav, fileToDeploy, pomFile );

        Assert.assertEquals( "Status should have been 201", 201, status );
        // deploy

        // remove priv

        // deploy

    }

}
