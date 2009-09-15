/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.rest.users;

import java.util.Set;

import org.restlet.data.Request;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.rest.model.PlexusUserListResourceResponse;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public abstract class AbstractUserSearchPlexusResource
    extends AbstractSecurityPlexusResource
{
    public static final String USER_SOURCE_KEY = "userSource";

    protected String getUserSource( Request request )
    {
        String source = request.getAttributes().get( USER_SOURCE_KEY ).toString();
        if ( "all".equalsIgnoreCase( source ) )
        {
            return null;
        }
        return source;
    }

    protected PlexusUserListResourceResponse search( UserSearchCriteria criteria )
    {
        PlexusUserListResourceResponse result = new PlexusUserListResourceResponse();
        
        Set<User> users = this.getSecuritySystem().searchUsers( criteria );
        
        for ( User user : users )
        {
            PlexusUserResource res = securityToRestModel( user );

            if ( res != null )
            {
                result.addData( res );
            }
        }

        return result;
    }

}
