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
package org.sonatype.nexus.restlight.testharness;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

public class PUTFixtureTest
    extends AbstractRESTTest
{

    private final PUTFixture fixture = new PUTFixture( getExpectedUser(), getExpectedPassword() );

    @Override
    protected RESTTestFixture getTestFixture()
    {
        return fixture;
    }

    @Test
    public void testPUT()
        throws Exception
    {
        Document doc = new Document().setRootElement( new Element( "root" ) );

        fixture.setRequestDocument( doc );

        String url = "http://localhost:" + fixture.getPort();

        HttpClient client = new HttpClient();
        setupAuthentication( client );

        PutMethod put = new PutMethod( url );

        XMLOutputter outputter = new XMLOutputter( Format.getCompactFormat() );

        put.setRequestEntity( new StringRequestEntity( outputter.outputString( doc ), "application/xml", "UTF-8" ) );

        client.executeMethod( put );

        String statusText = put.getStatusText();

        System.out.println( statusText );

        put.releaseConnection();

        assertEquals( HttpServletResponse.SC_OK, put.getStatusCode() );
    }

}
