/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.roles;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.AbstractRestTestCase;
import org.sonatype.nexus.rest.model.RoleResourceRequest;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class RoleTest
    extends AbstractRestTestCase
{

    public void testRequest()
        throws Exception
    {
        String jsonString = "{\"data\":{\"id\":null,\"name\":\"Test Role\",\"description\":\"This is a test role\",\"sessionTimeout\":50,"
            + "\"roles\":[\"roleid\"],\"privileges\":[\"privid\"]}}}";
        XStreamRepresentation representation = new XStreamRepresentation(
            xstream,
            jsonString,
            MediaType.APPLICATION_JSON );

        RoleResourceRequest request = (RoleResourceRequest) representation.getPayload( new RoleResourceRequest() );

        assert request.getData().getId() == null;
        assert request.getData().getName().equals( "Test Role" );
        assert request.getData().getDescription().equals( "This is a test role" );
        assert request.getData().getSessionTimeout() == 50;
        assert request.getData().getRoles().size() == 1;
        assert ( (String) request.getData().getRoles().get( 0 ) ).equals( "roleid" );
        assert request.getData().getPrivileges().size() == 1;
        assert ( (String) request.getData().getPrivileges().get( 0 ) ).equals( "privid" );
    }
}
