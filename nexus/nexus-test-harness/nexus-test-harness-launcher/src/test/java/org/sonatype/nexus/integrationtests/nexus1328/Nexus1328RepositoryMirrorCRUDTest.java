package org.sonatype.nexus.integrationtests.nexus1328;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.test.utils.MirrorMessageUtils;

public class Nexus1328RepositoryMirrorCRUDTest
    extends AbstractNexusIntegrationTest
{    
    @BeforeClass
    public static void clean()
    {
        try
        {
            cleanWorkDir();
        }
        catch ( IOException e )
        {
            // NVM
        }
    }
    
    protected MirrorMessageUtils messageUtil;
    
    public Nexus1328RepositoryMirrorCRUDTest()
    {
        this.messageUtil = new MirrorMessageUtils( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }
    
    @Test
    public void createMirrorTest()
        throws IOException
    {
        MirrorResource resource = new MirrorResource();
        
        resource.setUrl( "http://createMirrorTest" );

        // this also validates
        this.messageUtil.createMirror( "release-proxy-repo-1", resource );
    }
    
    @Test
    public void readMirrorTest()
        throws IOException
    {   
        MirrorResource resource = new MirrorResource();
    
        resource.setUrl( "http://readMirrorTest" );
    
        // this also validates
        this.messageUtil.createMirror( "release-proxy-repo-1", resource );
    
        Response response = this.messageUtil.sendMessage( Method.GET, "release-proxy-repo-1", resource );
    
        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not GET Repository Mirror: " + response.getStatus() );
        }
    
        // get the Resource object
        MirrorResource responseResource = this.messageUtil.getResourceFromResponse( response );
    
        Assert.assertEquals( resource.getUrl(), responseResource.getUrl() );
    }
    
    @Test
    public void updateMirrorTest()
        throws IOException
    {
        MirrorResource resource = new MirrorResource();

        resource.setUrl( "http://createMirrorTest" );

        resource = this.messageUtil.createMirror( "release-proxy-repo-1", resource );

        // update the mirror
        resource.setUrl( "http://updateMirrorTest" );
        
        // this validates
        this.messageUtil.updateMirror( "release-proxy-repo-1", resource );
    }
    
    @Test
    public void deleteMirrorTest()
        throws IOException
    {
        MirrorResource resource = new MirrorResource();

        resource.setUrl( "http://deleteMirrorTest" );
        
        resource = this.messageUtil.createMirror( "release-proxy-repo-1", resource );

        // use the new ID
        Response response = this.messageUtil.sendMessage( Method.DELETE, "release-proxy-repo-1", resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete Mirror: " + response.getStatus() );
        }
        
        response = this.messageUtil.sendMessage( Method.GET, "release-proxy-repo-1", resource );
        
        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Mirror still exists, but should not" );
        }
    }
}
