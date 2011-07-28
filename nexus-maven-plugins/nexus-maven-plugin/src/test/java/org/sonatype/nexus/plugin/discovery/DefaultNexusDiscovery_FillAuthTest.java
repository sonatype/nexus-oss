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

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.junit.Test;

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

        prompter.addExpectation( "Are you sure you want to use the Nexus URL: http://www.somewhere.com/?", "y" );
        prompter.addExpectation( "Enter Username", user );
        prompter.addExpectation( "Enter Password", password );

        NexusConnectionInfo info = discovery.fillAuth( nexusUrl, settings, project, "blah", false );
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

        prompter.addExpectation( "Are you sure you want to use the Nexus URL: http://www.somewhere.com/?", "y" );
        prompter.addExpectation( "Select a login to use for Nexus connection 'http://www.somewhere.com/'", "1" );

        NexusConnectionInfo info = discovery.fillAuth( nexusUrl, settings, project, "blah", false );
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

        prompter.addExpectation( "Are you sure you want to use the Nexus URL: http://www.somewhere.com/?", "y" );
        prompter.addExpectation( "Select a login to use for Nexus connection 'http://www.somewhere.com/'", "X" );
        prompter.addExpectation( "Enter Username", user );
        prompter.addExpectation( "Enter Password", password );

        NexusConnectionInfo info = discovery.fillAuth( nexusUrl, settings, project, "blah", false );
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

        NexusConnectionInfo info = discovery.fillAuth( nexusUrl, settings, project, "blah", true );
        assertNotNull( info );
        assertEquals( user, info.getUser() );
        assertEquals( password, info.getPassword() );
    }
    
    
    @Test
    public void enctypedPasswordWithServerAuthId()
        throws NexusDiscoveryException
    {
        String nexusUrl = "http://www.somewhere.com/";
        String user = "user";
        String serverId = "test-server";

        testClientManager.testUrl = nexusUrl;
        testClientManager.testUser = user;
        testClientManager.testPassword = clearTextPassword;

        Settings settings = new Settings();

        Server server = new Server();
        server.setId( serverId );
        server.setUsername( user );
        server.setPassword( encryptedPassword );

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

        NexusConnectionInfo info = discovery.fillAuth( nexusUrl, settings, project, "blah", true );
        assertNotNull( info );
        assertEquals( user, info.getUser() );
        assertEquals( clearTextPassword, info.getPassword() );
    }
    

}
