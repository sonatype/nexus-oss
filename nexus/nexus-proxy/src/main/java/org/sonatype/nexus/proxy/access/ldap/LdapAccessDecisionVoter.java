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
package org.sonatype.nexus.proxy.access.ldap;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessDecisionVoter;
import org.sonatype.nexus.proxy.access.RepositoryPermission;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.security.User;

/**
 * The Class LdapAccessDecisionVoter.
 * 
 * @author cstamas
 * @plexus.component role-hint="ldap"
 */
public class LdapAccessDecisionVoter
    extends LdapConsumer
    implements AccessDecisionVoter
{

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.proxy.access.AccessDecisionVoter#vote(org.sonatype.nexus.proxy.ResourceStoreRequest,
     *      org.sonatype.nexus.proxy.repository.Repository, org.sonatype.nexus.proxy.access.RepositoryPermission)
     */
    public int vote( ResourceStoreRequest request, Repository repository, RepositoryPermission permission )
    {
        if ( request.getRequestContext().containsKey( AccessDecisionVoter.REQUEST_USER ) )
        {
            String username = ( (User) request.getRequestContext().get( AccessDecisionVoter.REQUEST_USER ) )
                .getUsername();

            if ( ldapAuthorize( username, request, repository, permission ) )
            {
                return ACCESS_APPROVED;
            }
            else
            {
                return ACCESS_DENIED;
            }
        }
        else
        {
            return ACCESS_DENIED;
        }
    }
}
