/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.web.testapp;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.model.CUser;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.UserStatus;

public class SampleAppTestDisabled
    extends PlexusTestCase
{
    private static String CONF_DIR = "target/plexus-work/conf";

    private static final int USER_COUNT = 1;

    private static final int REQUEST_COUNT = 10000;

    private ServletServer server;

    public void testMemoryUsage()
        throws Exception
    {
        Response response = this.doGet( "sample/test", "admin", "admin123" );
        String responseText = ( response.getEntity() != null ) ? response.getEntity().getText() : "";
        Assert.assertTrue( "Response: " + response.getStatus() + "\n" + responseText + "\nredirect: "
                               + response.getLocationRef(), response.getStatus().isSuccess() );

        // memory size after one request
        long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        for ( int ii = 0; ii < USER_COUNT; ii++ )
        {
            String userId = "user" + ii;

            for ( int requestCountTmp = 0; requestCountTmp < REQUEST_COUNT; requestCountTmp++ )
            {
                response = this.doGet( "sample/test", userId, "password" );
                responseText = ( response.getEntity() != null ) ? response.getEntity().getText() : "";

                Assert.assertTrue( "Response: " + response.getStatus() + "\n" + responseText + "\nredirect: "
                    + response.getLocationRef(), response.getStatus().isSuccess() );
            }
        }

        long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // System.out.println( "Memory used after 1 request: " + usedMemoryBefore );
        //
        // System.out.println( "Memory used after "+ USER_COUNT +" request: " + usedMemoryAfter );
        long difference = usedMemoryAfter - usedMemoryBefore;
        double differenceInMB = difference / 1024d / 1024d;

        System.out.println( "Difference of: " + difference + " bytes " + ( difference / 1024d ) + "Kb " + difference
            + "MB" );

        Assert.assertTrue( "Expected memory usage is less then 40, but actual was: " + differenceInMB,
                           55d > differenceInMB );

    }

    private Response doGet( String urlPart, String username, String password )
    {
        Client restClient = new Client( new org.restlet.Context(), Protocol.HTTP );

        Request request = new Request();
        request.setResourceRef( server.getUrl( urlPart ) );
        request.setMethod( Method.GET );

        if ( StringUtils.isNotEmpty( username ) )
        {
            ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
            ChallengeResponse authentication = new ChallengeResponse( scheme, username, password );
            request.setChallengeResponse( authentication );
        }

        return restClient.handle( request );
    }

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration configuration )
    {
        configuration.setClassPathScanning( PlexusConstants.SCANNING_CACHE );
    }

    @Override
    protected void customizeContext( org.codehaus.plexus.context.Context context )
    {
        super.customizeContext( context );
        context.put( "security-xml-file", CONF_DIR + "/security.xml" );
        context.put( "application-conf", CONF_DIR );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // copy security.xml in place
        // the test security.xml name will be <package-name>.<test-name>-security.xml
        String baseName = "target/test-classes/" + this.getClass().getName().replaceAll( "\\.", "/" );
        File securityXml = new File( baseName + "-security.xml" );
        FileUtils.copyFile( securityXml, new File( CONF_DIR, "security.xml" ) );

        File securityConfigXml = new File( baseName + "-security-configuration.xml" );
        FileUtils.copyFile( securityConfigXml, new File( CONF_DIR, "security-configuration.xml" ) );

        // create users
        this.createUsers();

        new HackedPlexusContainerContextListener().setPlexusContainer( this.getContainer() );

        this.server = this.lookup( ServletServer.class );
        server.start();
    }

    private void createUsers()
        throws Exception
    {

        // use the ConfigurationManager directly because it is way faster

        ConfigurationManager configManager = this.lookup( ConfigurationManager.class );

        for ( int ii = 0; ii < USER_COUNT; ii++ )
        {
            String userId = "user" + ii;

            CUser user = new CUser();
            user.setEmail( userId + "@invalidDomain.foobar" );
            user.setId( userId );
            user.setFirstName( userId );
            user.setLastName( userId );
            user.setStatus( UserStatus.active.toString() );

            Set<String> roles = new HashSet<String>();
            roles.add( "role1" );

            // add each user
            // System.out.println( "creating user: " + userId );
            configManager.createUser( user, "password", roles );

        }

    }

    @Override
    protected void tearDown()
        throws Exception
    {
        if ( this.server != null )
        {
            this.server.stop();
        }
    }

}
