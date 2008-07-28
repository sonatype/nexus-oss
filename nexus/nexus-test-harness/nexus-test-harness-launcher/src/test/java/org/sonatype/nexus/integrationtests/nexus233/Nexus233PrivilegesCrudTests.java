package org.sonatype.nexus.integrationtests.nexus233;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
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
        resource.setName( "createTest" );
        resource.setType( "repositoryTarget" );
        resource.addMethod( "create" );
//        resource.setRepositoryId( "" );
        resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {

            String reponseText = response.getEntity().getText();
            Assert.fail( "Could not create Privilege: " + response.getStatus() + "reponse:\n"+ reponseText);
        }
//        String reponseText = response.getEntity().getText();
//        System.out.println( response.getStatus() + "reponse:\n"+ reponseText );

        // get the Resource object
//        PrivilegeTargetResource responseResource = 
            Object obj = this.messageUtil.getResourceFromResponse( response );

            System.out.println( "obj: "+ obj );
            
        // make sure the id != null
//        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );
//
//        Assert.assertEquals( resource.getContentClass(), responseResource.getContentClass() );
//        Assert.assertEquals( resource.getName(), responseResource.getName() );
//        Assert.assertEquals( resource.getPatterns(), responseResource.getPatterns() );

//        this.messageUtil.verifyTargetsConfig( responseResource );
    }

    
//    @Test
    public void listTest()
    {
        
    }
    
}
