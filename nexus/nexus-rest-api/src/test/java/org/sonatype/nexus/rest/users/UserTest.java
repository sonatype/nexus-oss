/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.users;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.AbstractRestTestCase;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.security.rest.model.UserResourceRequest;

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
