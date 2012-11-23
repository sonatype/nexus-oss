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
package org.sonatype.plexus.rest.jaxrs;

import javax.ws.rs.core.Application;

import org.codehaus.plexus.PlexusTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.JaxRsApplication;

@Ignore
public class JaxRsTest
    extends PlexusTestCase
{
    public Application getApplication()
        throws Exception
    {
        return lookup( Application.class );
    }

    public PlexusObjectFactory getPlexusObjectFactory()
        throws Exception
    {
        return lookup( PlexusObjectFactory.class );
    }

    @Test
    public void testSimple()
        throws Exception
    {
        Component comp = new Component();
        Server server = comp.getServers().add( Protocol.HTTP, 8182 );

        // create JAX-RS runtime environment
        JaxRsApplication application = new JaxRsApplication( comp.getContext() );

        // plexus goes here
        application.setObjectFactory( getPlexusObjectFactory() );

        // attach ApplicationConfig
        application.add( getApplication() );

        // Attach the application to the component and start it
        comp.getDefaultHost().attach( application );
        comp.start();

        System.out.println( "Server started on port " + server.getPort() );
        System.out.println( "Press key to stop server" );
        System.in.read();
        System.out.println( "Stopping server" );
        comp.stop();
        System.out.println( "Server stopped" );

    }

}
