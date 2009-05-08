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
    public void getUserList()
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

        List<User> users = client.getUserList();

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
        userPostFixture.setResponseDocument( readTestDocumentResource( "user-post.xml" ) );
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

}
