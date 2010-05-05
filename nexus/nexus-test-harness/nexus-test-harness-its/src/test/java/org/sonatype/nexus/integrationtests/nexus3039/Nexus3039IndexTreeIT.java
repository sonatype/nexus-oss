package org.sonatype.nexus.integrationtests.nexus3039;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class Nexus3039IndexTreeIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void testIndexTree()
        throws Exception
    {
        String repoId = this.getTestRepositoryId();

        // get the index tree
        Response response = RequestFacade.doGetRequest( RequestFacade.SERVICE_LOCAL + "repositories/" + repoId + "/index_content/" );
        Assert.assertEquals( 200, response.getStatus().getCode() );
        
        RepositoryMessageUtil repoUtil = new RepositoryMessageUtil(
            this, this.getXMLXStream(),
            MediaType.APPLICATION_XML,
            this.getRepositoryTypeRegistry() );

        RepositoryResource resource = (RepositoryResource) repoUtil.getRepository( repoId );
        resource.setIndexable( false );
        repoUtil.updateRepo( resource );

        // get the index tree
        response = RequestFacade.doGetRequest( RequestFacade.SERVICE_LOCAL + "repositories/" + repoId + "/index_content/" );
        Assert.assertEquals( 404, response.getStatus().getCode() );

    }
    
    
    @Test
    public void testGroupIndexTree()
        throws Exception
    {
        String repoId = "public";

        // get the index tree
        Response response = RequestFacade.doGetRequest( RequestFacade.SERVICE_LOCAL + "repo_groups/" + repoId + "/index_content/" );
        Assert.assertEquals( 200, response.getStatus().getCode() );
    }
    

}
