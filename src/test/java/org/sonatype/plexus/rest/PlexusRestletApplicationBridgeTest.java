/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.plexus.rest;

import org.codehaus.plexus.PlexusTestCase;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class PlexusRestletApplicationBridgeTest
    extends PlexusTestCase
{
    public void testRest()
        throws Exception
    {
        Component component = new Component();

        component.getServers().add( Protocol.HTTP, 8182 );

        TestApplication app = (TestApplication) getContainer().lookup( Application.class, "test" );

        component.getDefaultHost().attach( app );

        component.start();

        TestClient client = new TestClient();

        assertEquals( "tokenA", client.request( "http://localhost:8182/tokenA" ) );

        assertEquals( "tokenB", client.request( "http://localhost:8182/tokenB" ) );

        assertEquals( "manual", client.request( "http://localhost:8182/manual" ) );

        component.stop();
    }
}
