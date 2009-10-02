package com.sonatype.nexus.proxy.maven.site.nxcm1148;

import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.RequestFacade;

public class NXCM1148SiteDownloadIT
    extends AbstractMavenNexusIT
{
    @Test
    public void testCssMimeType() throws Exception
    {
        this.setTestRepositoryId( "nxcm1148site" );
        
        Response response = RequestFacade.doGetRequest( RequestFacade.SERVICE_LOCAL + "repositories/"+ this.getTestRepositoryId() +"/content/project/css/site.css" );
        Assert.assertTrue( response.getStatus().isSuccess() );
        
        Assert.assertEquals(MediaType.TEXT_CSS, response.getEntity().getMediaType() );
    }
    
    
    @Test
    public void testDirectoryListing() throws Exception
    {
        this.setTestRepositoryId( "nxcm1148site" );
        Response response = RequestFacade.doGetRequest( RequestFacade.SERVICE_LOCAL + "repositories/"+ this.getTestRepositoryId() +"/content/project/" );
        Assert.assertTrue( response.getStatus().isSuccess() );
        Assert.assertEquals(MediaType.APPLICATION_XML, response.getEntity().getMediaType() );
        Assert.assertTrue( response.getEntity().getText().contains( "<content-item>" ) );
        
        response = RequestFacade.sendMessage( new URL( this.getBaseNexusUrl() + "content/sites/"+ this.getTestRepositoryId() + "/project/"), Method.GET, null );
        Assert.assertTrue( response.getStatus().isSuccess() );
        Assert.assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType() );
        Assert.assertTrue( response.getEntity().getText().contains( "<html" ) );
        
    }
}
