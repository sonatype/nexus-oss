package org.sonatype.nexus.integrationtests.nexus3709;

import java.net.URL;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus3709FileTypeValidationIT
    extends AbstractNexusProxyIntegrationTest
{

    public Nexus3709FileTypeValidationIT()
    {
        super("nexus3709");
    }
    
    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();
        
        // enable file type validation
        RepositoryMessageUtil repoUtil = new RepositoryMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML, getRepositoryTypeRegistry() );
        
        RepositoryProxyResource resource = (RepositoryProxyResource) repoUtil.getRepository( this.getTestRepositoryId() );
        // this should be false to start with
        Assert.assertFalse( resource.isFileTypeValidation(), "Expected fileTypeValidation to be false after startup." );
        resource.setFileTypeValidation( true );
        
        // update it
        repoUtil.updateRepo( resource );
    }
    
    
    @Test
    public void testGoodZip() throws Exception
    {
        String relativePath = this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "goodzip", "1.0.0", null, "zip", null, null, null, false, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );
        
        Assert.assertEquals( 200, response.getStatus().getCode() );
    }
    
    @Test
    public void testBadZip() throws Exception
    {
        String relativePath = this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "badzip", "1.0.0", null, "zip", null, null, null, false, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );
        
        Assert.assertEquals( 404, response.getStatus().getCode() );
    }

    @Test
    public void testGoodJar() throws Exception
    {
        String relativePath = this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "goodjar", "1.0.0", null, "jar", null, null, null, false, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );
        
        Assert.assertEquals( 200, response.getStatus().getCode() );
    }
    
    @Test
    public void testBadJar() throws Exception
    {
        String relativePath = this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "badjar", "1.0.0", null, "jar", null, null, null, false, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );
        
        Assert.assertEquals( 404, response.getStatus().getCode() );
    }
    
    @Test
    public void testGoodPom() throws Exception
    {
        String relativePath = this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "goodpom", "1.0.0", null, "pom", null, null, null, false, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );
        
        Assert.assertEquals( 200, response.getStatus().getCode() );
    }
    
    @Test
    public void testBadPom() throws Exception
    {
        String relativePath = this.getRelitiveArtifactPath( new Gav( "nexus3709.foo.bar", "badpom", "1.0.0", null, "pom", null, null, null, false, false, null, false, null ) );
        String url = this.getRepositoryUrl( this.getTestRepositoryId() ) + relativePath;
        Response response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );
        
        Assert.assertEquals( 404, response.getStatus().getCode() );
    }
    
}
