package org.sonatype.nexus.restlight.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.ConversationalFixture;
import org.sonatype.nexus.restlight.testharness.DELETEFixture;
import org.sonatype.nexus.restlight.testharness.GETFixture;
import org.sonatype.nexus.restlight.testharness.POSTFixture;
import org.sonatype.nexus.restlight.testharness.PUTFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;

public class CoreClientTest
    extends AbstractRESTTest
{

    private final ConversationalFixture fixture = new ConversationalFixture();

    @BeforeClass
    public static void setUpClass()
        throws Exception
    {
        System.getProperties().put( TEST_NX_API_VERSION_SYSPROP, "1.4" );
    }

    @Override
    protected RESTTestFixture getTestFixture()
    {
        return fixture;
    }

    @Test
    public void listUser()
        throws Exception
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();
        GETFixture userListGetFixture = new GETFixture();
        userListGetFixture.setExactURI( CoreClient.USER_PATH );
        userListGetFixture.setResponseDocument( readTestDocumentResource( "user-list.xml" ) );
        conversation.add( getVersionCheckFixture() );
        conversation.add( userListGetFixture );
        fixture.setConversation( conversation );

        CoreClient client = new CoreClient( getBaseUrl(), "testuser", "unused" );

        List<User> users = client.listUser();

        List<RESTTestFixture> unused = fixture.verifyConversationWasFinished();
        if ( unused != null && !unused.isEmpty() )
        {
            System.out.println( unused );
            fail( "Conversation was not finished. Didn't traverse:\n" + unused );
        }

        assertNotNull( users );

        assertEquals( 4, users.size() );
        assertEquals( "http://localhost:8081/nexus/service/local/users/admin", users.get( 0 ).getResourceURI() );
        assertEquals( "admin", users.get( 0 ).getUserId() );
        assertEquals( "Administrator", users.get( 0 ).getName() );
        assertEquals( "active", users.get( 0 ).getStatus() );
        assertEquals( "changeme@yourcompany.com", users.get( 0 ).getEmail() );
        assertEquals( true, users.get( 0 ).isUserManaged() );
    }

    @Test
    public void getUser()
        throws Exception
    {
        final String userId = "deployment";

        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();
        GETFixture userListGetFixture = new GETFixture();
        userListGetFixture.setExactURI( CoreClient.USER_PATH + "/" + userId );
        userListGetFixture.setResponseDocument( readTestDocumentResource( "user-get.xml" ) );
        conversation.add( getVersionCheckFixture() );
        conversation.add( userListGetFixture );
        fixture.setConversation( conversation );

        CoreClient client = new CoreClient( getBaseUrl(), "testuser", "unused" );

        User user = client.getUser( userId );

        assertNotNull( user );

        assertEquals( "http://localhost:8081/nexus/service/local/users/deployment", user.getResourceURI() );
        assertEquals( "deployment", user.getUserId() );
        assertEquals( "Deployment User", user.getName() );
        assertEquals( "active", user.getStatus() );
        assertEquals( "changeme1@yourcompany.com", user.getEmail() );
        assertEquals( true, user.isUserManaged() );
        List<String> roles = new ArrayList<String>( 2 );
        roles.add( "deployment" );
        roles.add( "repo-all-full" );
        assertEquals( roles, user.getRoles() );
    }

    @Test
    public void postUser()
        throws Exception
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();
        POSTFixture userPostFixture = new POSTFixture();
        userPostFixture.setExactURI( CoreClient.USER_PATH );
        userPostFixture.setRequestDocument( readTestDocumentResource( "user-post-req.xml" ) );
        userPostFixture.setResponseDocument( readTestDocumentResource( "user-post-resp.xml" ) );
        conversation.add( getVersionCheckFixture() );
        conversation.add( userPostFixture );
        fixture.setConversation( conversation );

        User user = new User();
        user.setUserId( "bbb" );
        user.setName( "bbb" );
        user.setStatus( "active" );
        user.setEmail( "b@b.b" );
        user.setUserManaged( true );
        user.getRoles().add( "admin" );

        CoreClient client = new CoreClient( getBaseUrl(), "testuser", "unused" );

        User userResp = client.postUser( user );

        assertEquals( "http://localhost:8081/nexus/service/local/users/bbb", userResp.getResourceURI() );
        assertEquals( "bbb", userResp.getUserId() );
        assertEquals( "bbb", userResp.getName() );
        assertEquals( "active", userResp.getStatus() );
        assertEquals( "b@b.b", userResp.getEmail() );
        assertEquals( true, user.isUserManaged() );
        List<String> roles = new ArrayList<String>( 1 );
        roles.add( "admin" );
        assertEquals( roles, user.getRoles() );
    }

    @Test
    public void putUser()
        throws Exception
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();
        PUTFixture userPutFixture = new PUTFixture();
        userPutFixture.setExactURI( CoreClient.USER_PATH + "/bbb" );
        userPutFixture.setResponseDocument( readTestDocumentResource( "user-put.xml" ) );
        conversation.add( getVersionCheckFixture() );
        conversation.add( userPutFixture );
        fixture.setConversation( conversation );

        User user = new User();
        user.setUserId( "bbb" );
        user.setName( "bbb" );
        user.setStatus( "active" );
        user.setEmail( "b@b.b" );
        user.setUserManaged( true );
        user.getRoles().add( "admin" );

        CoreClient client = new CoreClient( getBaseUrl(), "testuser", "unused" );

        User userResp = client.putUser( user );

        assertEquals( "http://localhost:8081/nexus/service/local/users/bbb", userResp.getResourceURI() );
        assertEquals( "bbb", userResp.getUserId() );
        assertEquals( "bbb", userResp.getName() );
        assertEquals( "active", userResp.getStatus() );
        assertEquals( "b@b.b", userResp.getEmail() );
        assertEquals( true, user.isUserManaged() );
        List<String> roles = new ArrayList<String>( 1 );
        roles.add( "admin" );
        assertEquals( roles, user.getRoles() );
    }

    @Test
    public void deleteUser()
        throws Exception
    {
        String userId = "user-test";

        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();
        DELETEFixture userDeleteFixture = new DELETEFixture();
        userDeleteFixture.setExactURI( CoreClient.USER_PATH + "/" + userId );
        conversation.add( getVersionCheckFixture() );
        conversation.add( userDeleteFixture );
        fixture.setConversation( conversation );

        CoreClient client = new CoreClient( getBaseUrl(), "testuser", "unused" );

        client.deleteUser( userId );
    }

    @Test
    public void listRole()
        throws Exception
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();
        GETFixture userListGetFixture = new GETFixture();
        userListGetFixture.setExactURI( CoreClient.ROLE_PATH );
        userListGetFixture.setResponseDocument( readTestDocumentResource( "role-list.xml" ) );
        conversation.add( getVersionCheckFixture() );
        conversation.add( userListGetFixture );
        fixture.setConversation( conversation );

        CoreClient client = new CoreClient( getBaseUrl(), "testuser", "unused" );

        List<Role> roles = client.listRole();

        assertNotNull( roles );
        assertEquals( 4, roles.size() );
        assertEquals( "http://localhost:8080/nexus/service/local/roles/anonymous", roles.get( 3 ).getResourceURI() );
        assertEquals( "anonymous", roles.get( 3 ).getId() );
        assertEquals( "Nexus Anonymous Role", roles.get( 3 ).getName() );
        assertEquals( "Anonymous role for Nexus", roles.get( 3 ).getDescription() );
        assertEquals( 60, roles.get( 3 ).getSessionTimeout() );
        assertEquals( false, roles.get( 3 ).isUserManaged() );
        List<String> subRoles = new ArrayList<String>( 3 );
        subRoles.add( "ui-repo-browser" );
        subRoles.add( "ui-search" );
        subRoles.add( "ui-system-feeds" );
        assertEquals( subRoles, roles.get( 3 ).getRoles() );
        List<String> privileges = new ArrayList<String>( 6 );
        privileges.add( "1" );
        privileges.add( "54" );
        privileges.add( "57" );
        privileges.add( "58" );
        privileges.add( "70" );
        privileges.add( "74" );
        assertEquals( privileges, roles.get( 3 ).getPrivileges() );
    }

    @Test
    public void postRole()
        throws Exception
    {
        List<RESTTestFixture> conversation = new ArrayList<RESTTestFixture>();
        POSTFixture postFixture = new POSTFixture();
        postFixture.setExactURI( CoreClient.ROLE_PATH );
        postFixture.setRequestDocument( readTestDocumentResource( "role-post-req.xml" ) );
        postFixture.setResponseDocument( readTestDocumentResource( "role-post-resp.xml" ) );
        conversation.add( getVersionCheckFixture() );
        conversation.add( postFixture );
        fixture.setConversation( conversation );

        Role role = new Role();
        role.setId( "a1" );
        role.setName( "a11" );
        role.setDescription( "a111" );
        role.setSessionTimeout( 100 );
        role.getRoles().add( "anonymous" );
        role.getPrivileges().add( "18" );

        CoreClient client = new CoreClient( getBaseUrl(), "testuser", "unused" );

        Role roleResp = client.postRole( role );

        assertNotNull( roleResp );
        assertEquals( "http://localhost:8080/nexus/service/local/roles/a1", roleResp.getResourceURI() );
        assertEquals( "a1", roleResp.getId() );
        assertEquals( "a11", roleResp.getName() );
        assertEquals( "a111", roleResp.getDescription() );
        assertEquals( 100, roleResp.getSessionTimeout() );
        assertEquals( true, roleResp.isUserManaged() );
        assertEquals( 1, roleResp.getRoles().size() );
        assertEquals( "anonymous", roleResp.getRoles().get( 0 ) );
        assertEquals( 1, roleResp.getPrivileges().size() );
        assertEquals( "18", roleResp.getPrivileges().get( 0 ) );
    }
}
