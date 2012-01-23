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
