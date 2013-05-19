/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.usermanagement;

import java.util.Set;

import org.sonatype.configuration.validation.InvalidConfigurationException;

/**
 * An abstract UserManager, that just throws exceptions for all the write methods. Any call to theses methods should be
 * checked by the <code>supportsWrite()</code> method, so this should never be called.
 * 
 * @author Brian Demers
 */
public abstract class AbstractReadOnlyUserManager
    extends AbstractUserManager
{

    public boolean supportsWrite()
    {
        return false;
    }

    public User addUser( User user, String password )
        throws InvalidConfigurationException
    {
        this.throwException();
        return null;
    }

    public void changePassword( String userId, String newPassword )
        throws UserNotFoundException
    {
        this.throwException();
    }

    public void deleteUser( String userId )
        throws UserNotFoundException
    {
        this.throwException();
    }

    public void setUsersRoles( String userId, Set<RoleIdentifier> roleIdentifiers )
        throws UserNotFoundException, InvalidConfigurationException
    {
    }

    public User updateUser( User user )
        throws UserNotFoundException, InvalidConfigurationException
    {
        this.throwException();
        return null;
    }

    private void throwException()
    {
        throw new IllegalStateException( "UserManager: '" + this.getSource() + "' does not support write operations." );
    }

}
