/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.test.utils;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.UserChangePasswordRequest;
import org.sonatype.nexus.rest.model.UserChangePasswordResource;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class ChangePasswordUtils
{

    private static XStream xstream;

    static
    {
        xstream = XStreamFactory.getXmlXStream();
    }

    public static Status changePassword( String username, String oldPassword, String newPassword )
        throws Exception
    {
        String serviceURI = "service/local/users_changepw";

        UserChangePasswordResource resource = new UserChangePasswordResource();
        resource.setUserId( username );
        resource.setOldPassword( oldPassword );
        resource.setNewPassword( newPassword );

        UserChangePasswordRequest request = new UserChangePasswordRequest();
        request.setData( resource );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        representation.setPayload( request );

        Response response = RequestFacade.sendMessage( serviceURI, Method.POST, representation );
        return response.getStatus();

    }

    public static Status changePassword( String username, String newPassword )
        throws Exception
    {
        String serviceURI = "service/local/users_setpw";

        UserChangePasswordResource resource = new UserChangePasswordResource();
        resource.setUserId( username );
        resource.setNewPassword( newPassword );

        UserChangePasswordRequest request = new UserChangePasswordRequest();
        request.setData( resource );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        representation.setPayload( request );

        Response response = RequestFacade.sendMessage( serviceURI, Method.POST, representation );
        return response.getStatus();

    }

}
