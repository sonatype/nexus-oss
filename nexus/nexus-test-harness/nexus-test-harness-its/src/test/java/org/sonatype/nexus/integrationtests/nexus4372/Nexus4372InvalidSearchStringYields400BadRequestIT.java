/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus4372;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.sonatype.nexus.test.utils.ResponseMatchers.respondsWithStatusCode;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.testng.annotations.Test;

public class Nexus4372InvalidSearchStringYields400BadRequestIT
    extends AbstractNexusIntegrationTest
{

    private static String[][] toTest = {
        {"!", "!*"},
        {"]]", "]]*"},
        {"<!", "<!*"}, // html special chars will be rendered by javascript, payload is expected to be represented as 'plain text'
    };

    @Test
    public void testInvalidSearch()
        throws IOException
    {
        for ( int i = 0; i < toTest.length; i++ )
        {
            String[] strings = toTest[i];
            String query = strings[0];
            String expected = strings[1];

            test( query, expected);
        }
    }

    private void test( String query, String expected )
        throws IOException
    {

        String serviceURIpart = "service/local/lucene/search?q=" + URLEncoder.encode( query, "UTF-8" );
        log.debug( "Testing query {}: {}", query, serviceURIpart );
        String errorPayload = RequestFacade.doGetForText( serviceURIpart,
                                                          respondsWithStatusCode( 400 ) );
        log.debug( "Received 'Bad Request' error: " + errorPayload );
        MediaType type = MediaType.APPLICATION_XML;
        XStreamRepresentation representation = new XStreamRepresentation( getXMLXStream(), errorPayload, type);

        ErrorResponse payload = (ErrorResponse) representation.getPayload( new ErrorResponse() );

        List errors = payload.getErrors();
        assertThat( (Collection<?>)errors, hasSize( 1 ) );
        ErrorMessage error = (ErrorMessage) errors.get( 0 );
        String msg = error.getMsg();

        msg = msg.replaceAll(
            "Cannot parse '([^']*)':.*",
            "$1" );

        log.debug( "msg: " + msg );

        assertThat( msg, equalTo( expected ) );
    }
}
