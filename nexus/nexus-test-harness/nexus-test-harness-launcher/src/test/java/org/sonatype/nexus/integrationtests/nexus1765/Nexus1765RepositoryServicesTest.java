package org.sonatype.nexus.integrationtests.nexus1765;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class Nexus1765RepositoryServicesTest
    extends AbstractPrivilegeTest
{

    @Test
    public void testGetRepoStatus()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "55" ); //nexus:repostatus:read
        
        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        String repoId = this.getTestRepositoryId();
        Response response = RequestFacade.doGetRequest( RepositoryMessageUtil.SERVICE_PART + "/" + repoId + "/status" );

        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }

    @Test
    public void testSetRepoStatus()
        throws Exception
    {

        this.giveUserPrivilege( TEST_USER_NAME, "55" ); //nexus:repostatus:read
        this.giveUserPrivilege( TEST_USER_NAME, "56" ); //nexus:repostatus:update
        
        String repoId = this.getTestRepositoryId();

        RepositoryStatusResource repoStatus = repoUtil.getStatus( repoId );
        repoStatus.setProxyMode( ProxyMode.BLOCKED_AUTO.name() );

        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        XStreamRepresentation representation = new XStreamRepresentation(
            this.getXMLXStream(),
            "",
            MediaType.APPLICATION_XML );
        RepositoryStatusResourceResponse resourceResponse = new RepositoryStatusResourceResponse();
        resourceResponse.setData( repoStatus );
        representation.setPayload( resourceResponse );
        
        Response response = RequestFacade.sendMessage(
            RepositoryMessageUtil.SERVICE_PART + "/" + repoId + "/status",
            Method.PUT,
            representation );

        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }

    @Test
    public void testGetRepoMeta()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "67" ); //nexus:repometa:read
        
        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        String repoId = this.getTestRepositoryId();
        Response response = RequestFacade.doGetRequest( RepositoryMessageUtil.SERVICE_PART + "/" + repoId + "/meta" );

        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }
    
    @Test
    public void testGetRepoContent()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "T1" ); //read all M2
        
        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        String repoId = this.getTestRepositoryId();
        Response response = RequestFacade.doGetRequest( RepositoryMessageUtil.SERVICE_PART + "/" + repoId + "/content/" );

        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }
    
    @Test
    public void testGetRepoIndexContent()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "T1" ); //read all M2
        
        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        String repoId = this.getTestRepositoryId();
        Response response = RequestFacade.doGetRequest( RepositoryMessageUtil.SERVICE_PART + "/" + repoId + "/index_content/" );

        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }
    
}
