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
package org.sonatype.plexus.rest.xstream.xml;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusTestCase;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.plexus.rest.TestApplication;

public class InvalidXMLTest
    extends PlexusTestCase
{

    private static final String URL = "http://localhost:8182/XStreamPlexusResource";

    private static final String VALID = "<simple><data>something</data></simple>";

    private static final String INVALID = "<simple><invalid/><data>something</data></simple>";

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration configuration )
    {
        super.customizeContainerConfiguration( configuration );
        configuration.setAutoWiring( true );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_CACHE );
    }

    public void testXML()
        throws Exception
    {
        Component component = new Component();

        component.getServers().add( Protocol.HTTP, 8182 );

        TestApplication app = (TestApplication) getContainer().lookup( Application.class, "test" );

        component.getDefaultHost().attach( app );

        component.start();

        Status status = post( URL, VALID ).getStatus();
        assertTrue( status.toString(), status.isSuccess() );
        status = post( URL, INVALID ).getStatus();
        assertEquals( status.toString(), 400, status.getCode() );

        component.stop();
    }

    private Response post( String url, String content )
    {
        Request request = new Request();
        request.setResourceRef( url );
        request.setMethod( Method.POST );
        request.setEntity( content, MediaType.APPLICATION_XML );
        Context ctx = new Context();

        Client client = new Client( ctx, Protocol.HTTP );

        return client.handle( request );
    }
}
