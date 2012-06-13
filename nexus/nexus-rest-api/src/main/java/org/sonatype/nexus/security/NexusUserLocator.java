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
package org.sonatype.nexus.security;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.subject.PrincipalCollection;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;

/**
 * Attempts to locate users (both internal and external) based on their principals.
 */
@Named
@Singleton
public class NexusUserLocator
{
    @Inject
    private List<UserManager> userManagers;

    /**
     * Searches all known {@link UserManager}s for a user with matching principals.
     * 
     * @param principals Identifying principals
     * @return User record
     */
    public User findUser( final PrincipalCollection principals )
    {
        final Set<String> realmNames = principals.getRealmNames();
        final String userId = principals.getPrimaryPrincipal().toString();
        for ( final UserManager userManager : userManagers )
        {
            // only search managers that could have contributed to the principals
            if ( realmNames.contains( userManager.getAuthenticationRealmName() ) )
            {
                try
                {
                    final User user = userManager.getUser( userId );
                    if ( user != null )
                    {
                        return user;
                    }
                }
                catch ( final UserNotFoundException e )
                {
                    // try next UserManager...
                }
            }
        }
        return null;
    }
}
