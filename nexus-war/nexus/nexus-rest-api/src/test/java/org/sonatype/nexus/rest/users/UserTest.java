/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.users;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.AbstractRestTestCase;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class UserTest
    extends AbstractRestTestCase
{

    public void testRequest()
        throws Exception
    {
        String jsonString = "{\"data\":{\"userId\":\"myuser\",\"name\":\"johnny test\",\"email\":\"test@email.com\",\"status\":\"active\","
            + "\"roles\":[\"roleId\"]}}}";
        XStreamRepresentation representation = new XStreamRepresentation(
            xstream,
            jsonString,
            MediaType.APPLICATION_JSON );

        UserResourceRequest request = (UserResourceRequest) representation.getPayload( new UserResourceRequest() );

        assert request.getData().getUserId().equals( "myuser" );
        assert request.getData().getName().equals( "johnny test" );
        assert request.getData().getEmail().equals( "test@email.com" );
        assert request.getData().getStatus().equals( "active" );
        assert request.getData().getRoles().size() == 1;
        assert request.getData().getRoles().get( 0 ).equals( "roleId" );
    }
}
