package org.sonatype.nexus.plugin.discovery;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

public class DefaultNexusDiscovery_FillAuthTest
    extends AbstractNexusDiscoveryTest
{

    @Test
    public void unmatchedNexusUrl()
        throws NexusDiscoveryException
    {
        String nexusUrl = "http://www.somewhere.com/";
        String user = "user";
        String password = "password";

        testClientManager.testUrl = nexusUrl;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

        Settings settings = new Settings();

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        discovery.setUserInput( new BufferedReader( new StringReader( "y\n" + user + "\n" + password + "\n" ) ) );
        discovery.setUserOutput( dummyOutput );

        NexusConnectionInfo info = discovery.fillAuth( nexusUrl, settings, project, false );
        assertNotNull( info );
        assertEquals( user, info.getUser() );
        assertEquals( password, info.getPassword() );
    }

    @Test
    public void unmatchedNexusUrlWithServerIdAvailable()
        throws NexusDiscoveryException
    {
        String nexusUrl = "http://www.somewhere.com/";
        String user = "user";
        String password = "password";
        String serverId = "test-server";

        testClientManager.testUrl = nexusUrl;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

        Settings settings = new Settings();

        Server server = new Server();
        server.setId( serverId );
        server.setUsername( user );
        server.setPassword( password );

        settings.addServer( server );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        discovery.setUserInput( new BufferedReader( new StringReader( "y\n1\n" ) ) );
        discovery.setUserOutput( dummyOutput );

        NexusConnectionInfo info = discovery.fillAuth( nexusUrl, settings, project, false );
        assertNotNull( info );
        assertEquals( user, info.getUser() );
        assertEquals( password, info.getPassword() );
    }

    @Test
    public void unmatchedNexusUrlWithServerIdAvailableIsIgnored()
        throws NexusDiscoveryException
    {
        String nexusUrl = "http://www.somewhere.com/";
        String user = "user";
        String password = "password";
        String serverId = "test-server";

        testClientManager.testUrl = nexusUrl;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

        Settings settings = new Settings();

        Server server = new Server();
        server.setId( serverId );
        server.setUsername( "foo" );
        server.setPassword( "bar" );

        settings.addServer( server );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        discovery.setUserInput( new BufferedReader( new StringReader( "y\nX\n" + user + "\n" + password + "\n" ) ) );
        discovery.setUserOutput( dummyOutput );

        NexusConnectionInfo info = discovery.fillAuth( nexusUrl, settings, project, false );
        assertNotNull( info );
        assertEquals( user, info.getUser() );
        assertEquals( password, info.getPassword() );
    }

    @Test
    public void nexusUrlWithServerIdMatchedToMirror()
        throws NexusDiscoveryException
    {
        String nexusUrl = "http://www.somewhere.com/";
        String user = "user";
        String password = "password";
        String serverId = "test-server";

        testClientManager.testUrl = nexusUrl;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

        Settings settings = new Settings();

        Server server = new Server();
        server.setId( serverId );
        server.setUsername( user );
        server.setPassword( password );

        settings.addServer( server );

        Mirror mirror = new Mirror();
        mirror.setId( serverId );
        mirror.setUrl( nexusUrl );

        settings.addMirror( mirror );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        NexusConnectionInfo info = discovery.fillAuth( nexusUrl, settings, project, true );
        assertNotNull( info );
        assertEquals( user, info.getUser() );
        assertEquals( password, info.getPassword() );
    }

}
