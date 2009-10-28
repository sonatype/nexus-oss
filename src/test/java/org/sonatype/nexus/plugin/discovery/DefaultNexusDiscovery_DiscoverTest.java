package org.sonatype.nexus.plugin.discovery;

import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

public class DefaultNexusDiscovery_DiscoverTest
    extends AbstractNexusDiscoveryTest
{

    @Test
    public void autoDiscoverWithOneCompleteLocationFromMirrors()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";

        testClientManager.testUrl = url;
        testClientManager.testUser = "user";
        testClientManager.testPassword = "password";

        Mirror mirror = new Mirror();
        mirror.setId( "some-mirror" );
        mirror.setName( "A Mirror" );
        mirror.setUrl( url );

        settings.addMirror( mirror );

        Server server = new Server();
        server.setId( "some-mirror" );
        server.setUsername( "user" );
        server.setPassword( "password" );

        settings.addServer( server );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        String script = "X\n" + "http://nowhere.com/\n" + "X\n" + "bad-user\n" + "bad-password\n";
        // discovery.setUserOutput( System.out );
        discovery.setUserOutput( dummyOutput );
        discovery.setUserInput( new BufferedReader( new StringReader( script ) ) );
        discovery.discover( settings, project, true );
    }

    @Test
    public void autoDiscoverWithOneCompleteLocationFromMirrorWithEncryptedPassword()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";

        testClientManager.testUrl = url;
        testClientManager.testUser = "user";
        testClientManager.testPassword = "password";

        Mirror mirror = new Mirror();
        mirror.setId( "some-mirror" );
        mirror.setName( "A Mirror" );
        mirror.setUrl( url );

        settings.addMirror( mirror );

        Server server = new Server();
        server.setId( "some-mirror" );
        server.setUsername( "user" );
        server.setPassword( encryptedPassword );

        settings.addServer( server );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        String script = "X\n" + "http://nowhere.com/\n" + "X\n" + "bad-user\n" + "bad-password\n";
        // discovery.setUserOutput( System.out );
        discovery.setUserOutput( dummyOutput );
        discovery.setUserInput( new BufferedReader( new StringReader( script ) ) );
        discovery.discover( settings, project, true );
    }

    @Test
    public void autoDiscoverWithOneCompleteLocationFromMirrorWithEncryptedPasswordContainingComment()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";

        testClientManager.testUrl = url;
        testClientManager.testUser = "user";
        testClientManager.testPassword = "password";

        Mirror mirror = new Mirror();
        mirror.setId( "some-mirror" );
        mirror.setName( "A Mirror" );
        mirror.setUrl( url );

        settings.addMirror( mirror );

        Server server = new Server();
        server.setId( "some-mirror" );
        server.setUsername( "user" );
        server.setPassword( "this is a comment " + encryptedPassword );

        settings.addServer( server );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        String script = "X\n" + "http://nowhere.com/\n" + "X\n" + "bad-user\n" + "bad-password\n";
        // discovery.setUserOutput( System.out );
        discovery.setUserOutput( dummyOutput );
        discovery.setUserInput( new BufferedReader( new StringReader( script ) ) );
        discovery.discover( settings, project, true );
    }

    @Test
    public void autoDiscoverWithOneCompleteLocationFromSettingsProfileRepo()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";
        String id = "some-mirror";
        String user = "user";
        String password = "password";

        testClientManager.testUrl = url;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

        Server server = new Server();
        server.setId( id );
        server.setUsername( user );
        server.setPassword( password );

        settings.addServer( server );

        org.apache.maven.settings.Repository repo = new org.apache.maven.settings.Repository();
        repo.setId( id );
        repo.setUrl( url );

        org.apache.maven.settings.Profile profile = new org.apache.maven.settings.Profile();
        profile.addRepository( repo );

        settings.addProfile( profile );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        String script = "X\n" + "http://nowhere.com/\n" + "X\n" + "bad-user\n" + "bad-password\n";
        // discovery.setUserOutput( System.out );
        discovery.setUserOutput( dummyOutput );
        discovery.setUserInput( new BufferedReader( new StringReader( script ) ) );
        discovery.discover( settings, project, true );
    }

    @Test
    public void autoDiscoverWithOneCompleteLocationFromSettingsProfileRepoWithConfirmation()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";
        String id = "some-mirror";
        String user = "user";
        String password = "password";

        testClientManager.testUrl = url;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

        Server server = new Server();
        server.setId( id );
        server.setUsername( user );
        server.setPassword( password );

        settings.addServer( server );

        org.apache.maven.settings.Repository repo = new org.apache.maven.settings.Repository();
        repo.setId( id );
        repo.setUrl( url );
        repo.setName( "Profile Repository" );

        org.apache.maven.settings.Profile profile = new org.apache.maven.settings.Profile();
        profile.addRepository( repo );

        settings.addProfile( profile );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        String script = "y\n";
        // discovery.setUserOutput( System.out );
        discovery.setUserOutput( dummyOutput );
        discovery.setUserInput( new BufferedReader( new StringReader( script ) ) );
        discovery.discover( settings, project, false );
    }

    @Test
    public void autoDiscoverWithContentUrlInSettingsProfileRepo()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com";
        String id = "some-mirror";
        String user = "user";
        String password = "password";

        testClientManager.testUrl = url;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

        Server server = new Server();
        server.setId( id );
        server.setUsername( user );
        server.setPassword( password );

        settings.addServer( server );

        org.apache.maven.settings.Repository repo = new org.apache.maven.settings.Repository();
        repo.setId( id );
        repo.setUrl( url + "/content/groups/public/" );
        repo.setName( "Profile Repository" );

        org.apache.maven.settings.Profile profile = new org.apache.maven.settings.Profile();
        profile.addRepository( repo );

        settings.addProfile( profile );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        String script = "y\n";
        // discovery.setUserOutput( System.out );
        discovery.setUserOutput( dummyOutput );
        discovery.setUserInput( new BufferedReader( new StringReader( script ) ) );
        discovery.discover( settings, project, false );
    }

    @Test
    public void autoDiscoverWithOneCompleteLocationFromPOMRepo()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";
        String id = "some-mirror";
        String user = "user";
        String password = "password";

        testClientManager.testUrl = url;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

        Server server = new Server();
        server.setId( id );
        server.setUsername( user );
        server.setPassword( password );

        settings.addServer( server );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        Repository repo = new Repository();
        repo.setId( id );
        repo.setUrl( url );

        model.addRepository( repo );

        MavenProject project = new MavenProject( model );

        String script = "X\n" + "http://nowhere.com/\n" + "X\n" + "bad-user\n" + "bad-password\n";
        // discovery.setUserOutput( System.out );
        discovery.setUserOutput( dummyOutput );
        discovery.setUserInput( new BufferedReader( new StringReader( script ) ) );
        discovery.discover( settings, project, true );
    }

    @Test
    public void autoDiscoverWithOneCompleteLocationFromPOMDistMgmt()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";
        String id = "some-mirror";
        String user = "user";
        String password = "password";

        testClientManager.testUrl = url;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

        Server server = new Server();
        server.setId( id );
        server.setUsername( user );
        server.setPassword( password );

        settings.addServer( server );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        DistributionManagement dm = new DistributionManagement();

        DeploymentRepository repo = new DeploymentRepository();
        repo.setId( id );
        repo.setUrl( url );

        dm.setRepository( repo );

        model.setDistributionManagement( dm );

        MavenProject project = new MavenProject( model );

        project.setArtifact( factory.create( project ) );

        String script = "X\n" + "http://nowhere.com/\n" + "X\n" + "bad-user\n" + "bad-password\n";
        // discovery.setUserOutput( System.out );
        discovery.setUserOutput( dummyOutput );
        discovery.setUserInput( new BufferedReader( new StringReader( script ) ) );
        discovery.discover( settings, project, true );
    }

    @Test
    public void autoDiscoverWithOneCompleteLocationFromSnapshotPOMDistMgmt()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";
        String id = "some-mirror";
        String user = "user";
        String password = "password";

        testClientManager.testUrl = url;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

        Server server = new Server();
        server.setId( id );
        server.setUsername( user );
        server.setPassword( password );

        settings.addServer( server );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1-SNAPSHOT" );

        DistributionManagement dm = new DistributionManagement();

        DeploymentRepository repo = new DeploymentRepository();
        repo.setId( id );
        repo.setUrl( url );

        dm.setSnapshotRepository( repo );

        model.setDistributionManagement( dm );

        MavenProject project = new MavenProject( model );

        project.setArtifact( factory.create( project ) );

        String script = "X\n" + "http://nowhere.com/\n" + "X\n" + "bad-user\n" + "bad-password\n";
        // discovery.setUserOutput( System.out );
        discovery.setUserOutput( dummyOutput );
        discovery.setUserInput( new BufferedReader( new StringReader( script ) ) );
        discovery.discover( settings, project, true );
    }

    @Test
    public void promptWithOnePotentialLocationFromMirrors()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";

        testClientManager.testUrl = url;

        Mirror mirror = new Mirror();
        mirror.setId( "some-mirror" );
        mirror.setName( "A Mirror" );
        mirror.setUrl( url );

        settings.addMirror( mirror );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        String script = "1\n" + "X\n" + "user\n" + "password\n";
        discovery.setUserOutput( dummyOutput );
        discovery.setUserInput( new BufferedReader( new StringReader( script ) ) );
        discovery.discover( settings, project, true );
    }

    @Test
    public void promptWithTwoPotentialLocationsFromMirrors()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";

        testClientManager.testUrl = url;

        Mirror mirror = new Mirror();
        mirror.setId( "some-mirror" );
        mirror.setName( "A Mirror" );
        mirror.setUrl( url );

        settings.addMirror( mirror );

        Mirror mirror2 = new Mirror();
        mirror2.setId( "some-other-mirror" );
        mirror2.setName( "Another Mirror" );
        mirror2.setUrl( "http://nexus.somewhere-else.com/" );

        settings.addMirror( mirror2 );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( "group.id" );
        model.setArtifactId( "artifact-id" );
        model.setVersion( "1" );

        MavenProject project = new MavenProject( model );

        String script = "1\n" + "X\n" + "user\n" + "password\n";
        discovery.setUserOutput( dummyOutput );
        // discovery.setUserOutput( System.out );
        discovery.setUserInput( new BufferedReader( new StringReader( script ) ) );
        discovery.discover( settings, project, true );
    }

}
