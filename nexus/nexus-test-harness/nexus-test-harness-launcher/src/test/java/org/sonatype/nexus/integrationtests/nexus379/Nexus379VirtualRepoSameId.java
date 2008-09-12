package org.sonatype.nexus.integrationtests.nexus379;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

import com.thoughtworks.xstream.XStream;


/**
 * Test to make sure a Virtual repo cannot have the same Id as an real repository.
 */
public class Nexus379VirtualRepoSameId
    extends AbstractNexusIntegrationTest
{

    protected RepositoryMessageUtil messageUtil;

    public Nexus379VirtualRepoSameId()
    {
        this.messageUtil =
            new RepositoryMessageUtil(
                                       this.getXMLXStream(),
                                       MediaType.APPLICATION_XML );
    }
    
    @Test
    public void testVirtualRepoWithSameId() throws IOException
    {
        
        // create a repository
        RepositoryResource repo = new RepositoryResource();

        repo.setId( "testVirtualRepoWithSameId" );
        repo.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        repo.setName( "testVirtualRepoWithSameId" );
        repo.setFormat( "maven2" );
        repo.setRepoPolicy( "release" );
        repo.setChecksumPolicy( "ignore" ); // [ignore, warn, strictIfExists, strict]
        repo = this.messageUtil.createRepository( repo );
        
        // now create a virtual one, this should fail
        
        
        // create a repository
        RepositoryShadowResource virtualRepo = new RepositoryShadowResource();

        virtualRepo.setId( "testVirtualRepoWithSameId" );
        virtualRepo.setRepoType( "virtual" ); // [hosted, proxy, virtual]
        virtualRepo.setName( "testVirtualRepoWithSameId" );
        virtualRepo.setFormat( "maven1" );
        virtualRepo.setShadowOf( "testVirtualRepoWithSameId" );
        Response response = this.messageUtil.sendMessage( Method.POST, virtualRepo );
        
        Assert.assertEquals( "Status:" + "\n"+ response.getEntity().getText(), 400, response.getStatus().getCode() );
        
    }
    

}
