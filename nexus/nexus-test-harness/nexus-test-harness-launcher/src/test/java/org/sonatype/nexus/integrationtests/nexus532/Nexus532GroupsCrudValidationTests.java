package org.sonatype.nexus.integrationtests.nexus532;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.GroupMessageUtil;

import com.thoughtworks.xstream.XStream;

public class Nexus532GroupsCrudValidationTests
    extends AbstractNexusIntegrationTest
{

    protected GroupMessageUtil messageUtil;

    public Nexus532GroupsCrudValidationTests()
    {
        this.messageUtil =
            new GroupMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML );
    }

    @Test
    public void noIdTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

//        resource.setId( "noIdTest" );
        resource.setName( "noIdTest" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertFalse( "Group should not have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
    }
    
    @Test
    public void emptyIdTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "" );
        resource.setName( "emptyIdTest" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertFalse( "Group should not have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
    }
    
    @Test
    public void noNameTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "noNameTest" );
//        resource.setName( "noNameTest" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertTrue( "Group should have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        
        // check if the created group Name == the id
        Assert.assertEquals( "Group Name did not default to the Id", resource.getId(), this.messageUtil.getGroup( resource.getId() ).getName() );
    }
    
    @Test
    public void emptyNameTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "emptyNameTest" );
        resource.setName( "" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertTrue( "Group should have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        
        // check if the created group Name == the id
        Assert.assertEquals( "Group Name did not default to the Id", resource.getId(), this.messageUtil.getGroup( resource.getId() ).getName() );
    }
    
    @Test
    public void maven1GroupWithMaven2RepoTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "maven1GroupWithMaven2RepoTest" );
        resource.setName( "maven1GroupWithMaven2RepoTest" );
        resource.setFormat( "maven1" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();
        // should fail
        Assert.assertFalse( "Group should not have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
    }
    
    @Test
    public void noRepos()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "noRepos" );
        resource.setName( "noRepos" );
        resource.setFormat( "maven2" );

//        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
//        member.setId( "nexus-test-harness-repo" );
//        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertFalse( "Group should not have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
    }
    
    @Test
    public void invalidRepoId()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "invalidRepoId" );
        resource.setName( "invalidRepoId" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "really-invalid-repo-name" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertFalse( "Group should not have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
    }

}
