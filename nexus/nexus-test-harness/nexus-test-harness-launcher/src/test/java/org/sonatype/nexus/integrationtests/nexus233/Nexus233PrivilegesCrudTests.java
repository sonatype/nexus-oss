package org.sonatype.nexus.integrationtests.nexus233;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class Nexus233PrivilegesCrudTests
    extends AbstractNexusIntegrationTest
{

    protected PrivilegesMessageUtil messageUtil;

    public Nexus233PrivilegesCrudTests()
    {
        this.messageUtil =
            new PrivilegesMessageUtil(
                                   XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) ),
                                   MediaType.APPLICATION_JSON, this.getBaseNexusUrl() );
    }

    
    @SuppressWarnings( "unchecked" )
    @Test
    public void createTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "testpriv" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create privilege: " + response.getStatus() );
        }

        // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.getResourceListFromResponse( response );
        
        Assert.assertTrue( statusResources.size() == 1 );
        
       // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );
        
        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "testpriv" );
        Assert.assertEquals( statusResources.get( 0 ).getType(), "repositoryTarget" );
        Assert.assertEquals( ( ( PrivilegeTargetStatusResource ) statusResources.get( 0 ) ).getRepositoryTargetId(), "testTarget" );
    }

    
//    @Test
    public void listTest()
    {
        
    }
    
}
