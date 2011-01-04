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
package org.sonatype.nexus.rest.roles;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.AbstractRestTestCase;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.security.rest.model.RoleResourceRequest;

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
