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

package org.sonatype.nexus.user;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.usermanagement.InvalidCredentialsException;
import org.sonatype.security.usermanagement.NoSuchUserManagerException;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserNotFoundException;

@Component( role = UserAccountManager.class )
public class DefaultUserAccountManager
    extends AbstractLogEnabled
    implements UserAccountManager
{
    @Requirement
    private SecuritySystem securitySystem;

    public User readAccount( String userId )
        throws UserNotFoundException, AccessDeniedException
    {
        checkPermission( userId );

        return securitySystem.getUser( userId );
    }

    public User updateAccount( User user )
        throws InvalidConfigurationException, UserNotFoundException, NoSuchUserManagerException, AccessDeniedException
    {
        checkPermission( user.getUserId() );

        return securitySystem.updateUser( user );
    }

    public User updateAccount( User user, String oldPassword, String newPassword )
        throws InvalidConfigurationException, UserNotFoundException, InvalidCredentialsException,
        NoSuchUserManagerException, AccessDeniedException
    {
        checkPermission( user.getUserId() );

        securitySystem.changePassword( user.getUserId(), oldPassword, newPassword );

        return securitySystem.updateUser( user );
    }

    protected void checkPermission( String userId )
        throws AccessDeniedException
    {
        if ( !securitySystem.isSecurityEnabled() )
        {
            return;
        }

        if ( !securitySystem.getSubject().getPrincipal().equals( userId ) )
        {
            throw new AccessDeniedException( "Only '" + userId + "' is allowed to perform this operation." );
        }
    }

}
