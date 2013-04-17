/*
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
package org.sonatype.security.realms.kenai;

import java.io.File;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.sonatype.jettytestsuite.ServletInfo;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.jettytestsuite.WebappContext;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.realms.kenai.config.KenaiRealmConfiguration;

import com.google.inject.Binder;
import com.sonatype.security.realms.kenai.config.model.Configuration;

public abstract class AbstractKenaiRealmTest
    extends AbstractSecurityTestCase
{

    protected final String username = "test-user";

    protected final String password = "test-user123";

    private ServletServer server;

    protected final static String DEFAULT_ROLE = "default-url-role";

    protected static final String AUTH_APP_NAME = "auth_app";

    public void configure( final Binder binder )
    {
        super.configure( binder );
        binder.bind( HttpClient.class ).toInstance( new DefaultHttpClient() );
    }

    protected ServletServer getServletServer()
        throws Exception
    {
        ServletServer server = new ServletServer();

        ServerSocket socket = new ServerSocket( 0 );
        int freePort = socket.getLocalPort();
        socket.close();

        server.setPort( freePort );

        WebappContext webapp = new WebappContext();
        server.setWebappContexts( Arrays.asList( webapp ) );

        webapp.setName( "auth_app" );

        ServletInfo servletInfoAuthc = new ServletInfo();
        servletInfoAuthc.setName( "authc" );
        servletInfoAuthc.setMapping( "/api/login/*" );
        servletInfoAuthc.setServletClass( KenaiMockAuthcServlet.class.getName() );
        servletInfoAuthc.setParameters( new Properties() );

        webapp.setServletInfos( Arrays.asList( servletInfoAuthc ) );

        server.initialize();

        return server;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        server = this.getServletServer();
        // start the server
        server.start();

        KenaiRealmConfiguration kenaiRealmConfiguration = lookup( KenaiRealmConfiguration.class );
        Configuration configuration = kenaiRealmConfiguration.getConfiguration();
        configuration.setDefaultRole( DEFAULT_ROLE );
        configuration.setEmailDomain( "sonatype.org" );
        configuration.setBaseUrl( server.getUrl( AUTH_APP_NAME ) + "/" ); // add the '/' to the end
        // kenaiRealmConfiguration.updateConfiguration( configuration );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        server.stop();
        super.tearDown();
    }
}
