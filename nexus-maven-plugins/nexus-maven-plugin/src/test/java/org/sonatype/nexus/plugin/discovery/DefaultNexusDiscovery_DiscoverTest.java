/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.discovery;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.junit.Test;

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

        discovery.discover( settings, project, "blah", true );
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

        discovery.discover( settings, project, "blah", true );
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

        discovery.discover( settings, project, "blah", true );
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

        discovery.discover( settings, project, "blah", true );
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

        prompter.addExpectation( "Use this connection?", "y" );
        discovery.discover( settings, project, "blah", false );
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

        prompter.addExpectation( "Use this connection?", "y" );
        discovery.discover( settings, project, "blah", false );
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

        discovery.discover( settings, project, "blah", true );
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

        discovery.discover( settings, project, "blah", true );
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

        discovery.discover( settings, project, "blah", true );
    }

    @Test
    public void promptWithOnePotentialLocationFromMirrors()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";
        String user = "user";
        String password = "password";

        testClientManager.testUrl = url;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

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

        prompter.addExpectation( "1", "http://nexus.somewhere.com/", "Selection:" );
        prompter.addExpectation( "Enter Username", user );
        prompter.addExpectation( "Enter Password", password );

        NexusConnectionInfo info = discovery.discover( settings, project, "blah", false );
        assertNotNull( info );
        assertEquals( url, info.getNexusUrl() );
        assertEquals( user, info.getUser() );
        assertEquals( password, info.getPassword() );
    }

    @Test
    public void promptWithTwoPotentialLocationsFromMirrors()
        throws NexusDiscoveryException
    {
        Settings settings = new Settings();

        String url = "http://nexus.somewhere.com/";
        String user = "user";
        String password = "password";

        testClientManager.testUrl = url;
        testClientManager.testUser = user;
        testClientManager.testPassword = password;

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

        prompter.addExpectation( "1", "http://nexus.somewhere.com/", "Selection:" );
        prompter.addExpectation( "Enter Username", user );
        prompter.addExpectation( "Enter Password", password );

        NexusConnectionInfo info = discovery.discover( settings, project, "blah", false );
        assertNotNull( info );
        assertEquals( url, info.getNexusUrl() );
        assertEquals( user, info.getUser() );
        assertEquals( password, info.getPassword() );
    }

}
