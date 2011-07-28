/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
