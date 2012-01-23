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
