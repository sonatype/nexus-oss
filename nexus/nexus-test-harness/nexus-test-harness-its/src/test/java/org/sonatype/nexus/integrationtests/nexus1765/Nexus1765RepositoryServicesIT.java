package org.sonatype.nexus.integrationtests.nexus1765;

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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus1765RepositoryServicesIT
    extends AbstractPrivilegeTest
{
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }
    
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

        Assert.assertEquals( response.getStatus().getCode(), 403, "Status: " + response.getStatus() );
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

        Assert.assertEquals( response.getStatus().getCode(), 403, "Status: " + response.getStatus() );
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

        Assert.assertEquals( response.getStatus().getCode(), 403, "Status: " + response.getStatus() );
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

        // NOTE this will succeed, as you don't need view priv to retrieve content from repo
        Assert.assertTrue( response.getStatus().isSuccess(), "Status: " + response.getStatus() );
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

        // NOTE this will succeed, as you don't need view priv to retrieve index content from repo
        Assert.assertTrue( response.getStatus().isSuccess(), "Status: " + response.getStatus() );
    }
    
}
