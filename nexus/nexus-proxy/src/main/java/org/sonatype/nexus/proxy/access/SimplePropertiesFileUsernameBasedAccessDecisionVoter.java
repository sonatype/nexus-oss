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
package org.sonatype.nexus.proxy.access;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.security.User;

/**
 * <p>
 * Simple voter that allows/denies the repository access based on property file.
 * <p>
 * A property file looks like this:
 * 
 * <pre>
 * repoId1 = user1,user2,user3
 * repoId2 = user2,user3
 * </pre>
 * 
 * <p>
 * If the user is found on line for given repoId, the user has granted access, otherwise he is rejected.
 * <p>
 * This implementation does not takes permissions in account.
 * 
 * @author cstamas
 */
public class SimplePropertiesFileUsernameBasedAccessDecisionVoter
    implements AccessDecisionVoter
{

    /** The auth properties. */
    private Properties authProperties;

    /**
     * Gets the auth properties.
     * 
     * @return the auth properties
     */
    public Properties getAuthProperties()
    {
        return authProperties;
    }

    /**
     * Sets the auth properties.
     * 
     * @param properties the new auth properties
     */
    public void setAuthProperties( Properties properties )
    {
        this.authProperties = properties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.access.AccessDecisionVoter#vote(org.sonatype.nexus.ProximityRequest,
     *      org.sonatype.nexus.Repository, org.sonatype.nexus.access.RepositoryPermission)
     */
    public int vote( ResourceStoreRequest request, Repository repository, RepositoryPermission permission )
    {
        if ( request.getRequestContext().containsKey( REQUEST_USER ) )
        {
            String allowedUsers = getAuthProperties().getProperty( repository.getId() );
            String username = ( (User) request.getRequestContext().get( REQUEST_USER ) ).getUsername();

            List<String> usersList = Arrays.asList( allowedUsers.split( "," ) );
            
            if ( usersList.contains( username ) )
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
