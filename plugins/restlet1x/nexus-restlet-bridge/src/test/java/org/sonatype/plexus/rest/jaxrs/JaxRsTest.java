/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
