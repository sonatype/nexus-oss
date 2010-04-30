package org.sonatype.nexus.integrationtests.nexus1765;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;

public class Nexus1765RepositoryFilterIT
    extends AbstractPrivilegeTest
{

    @Test
    public void getRepositoriesListNoAccessTest()
        throws Exception
    {
        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        List<RepositoryListResource> repoList = repoUtil.getList();
        Assert.assertEquals( 0, repoList.size() );
    }

    @Test
    public void getRepositoriesListWithAccessTest()
        throws Exception
    {
        // give the user view access to
        String repoId = this.getTestRepositoryId();
        String viewPriv = "repository-" + repoId;
        this.addPrivilege( TEST_USER_NAME, viewPriv );

        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        List<RepositoryListResource> repoList = repoUtil.getList();
        Assert.assertEquals( 1, repoList.size() );
        Assert.assertEquals( repoId, repoList.get( 0 ).getId() );
    }

    @Test
    public void getRepositoryNoAccessTest()
        throws Exception
    {
        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        String repoId = this.getTestRepositoryId();
        Response response = RequestFacade.doGetRequest( "service/local/repositories/" + repoId );

        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }

    @Test
    public void updateRepositoryNoAccessTest()
        throws Exception
    {

        String repoId = this.getTestRepositoryId();

        RepositoryBaseResource repo = this.repoUtil.getRepository( repoId );
        repo.setName( "new name" );

        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Response response = this.repoUtil.sendMessage( Method.PUT, repo );
        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }

    @Test
    public void createRepositoryNoAccessTest()
        throws Exception
    {

        String repoId = "test-repo";

        RepositoryBaseResource repo = this.repoUtil.getRepository( this.getTestRepositoryId() );
        repo.setId( repoId );

        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Response response = this.repoUtil.sendMessage( Method.POST, repo, repoId );
        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }

    @Test
    public void deleteRepositoryNoAccessTest()
        throws Exception
    {

        String repoId = this.getTestRepositoryId();

        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Response response = RequestFacade.sendMessage( "service/local/repositories/" + repoId, Method.DELETE );
        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }

    @Test
    public void getGroupListNoAccessTest()
        throws Exception
    {
        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        List<RepositoryGroupListResource> groupList = groupUtil.getList();
        Assert.assertEquals( 0, groupList.size() );
    }

    @Test
    public void getGroupListWithAccessTest()
        throws Exception
    {
        // give the user view access to
        String repoId = "public";
        String viewPriv = "repository-" + repoId;
        this.addPrivilege( TEST_USER_NAME, viewPriv );

        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        List<RepositoryGroupListResource> groupList = groupUtil.getList();
        Assert.assertEquals( 1, groupList.size() );
        Assert.assertEquals( repoId, groupList.get( 0 ).getId() );
    }
    
    @Test
    public void getGroupNoAccessTest()
        throws Exception
    {
        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        String repoId = "public";
        Response response = RequestFacade.doGetRequest( GroupMessageUtil.SERVICE_PART +"/" + repoId );
        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }
    
    @Test
    public void updateGroupNoAccessTest()
        throws Exception
    {

        String repoId = "public";

        RepositoryGroupResource repo = this.groupUtil.getGroup( repoId );
        repo.setName( "new name" );

        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Response response = this.groupUtil.sendMessage( Method.PUT, repo );
        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }

    @Test
    public void createGroupNoAccessTest()
        throws Exception
    {

        String repoId = "test-group";

        RepositoryGroupResource repo = this.groupUtil.getGroup( "public" );
        repo.setId( repoId );

        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Response response = this.groupUtil.sendMessage( Method.POST, repo, repoId );
        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }

    @Test
    public void deleteGroupNoAccessTest()
        throws Exception
    {

        String repoId = "public";

        // use test user
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Response response = RequestFacade.sendMessage( GroupMessageUtil.SERVICE_PART +"/" + repoId, Method.DELETE );
        Assert.assertEquals( "Status: " + response.getStatus(), 403, response.getStatus().getCode() );
    }

}
