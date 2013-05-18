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
package org.sonatype.security.authorization;

import org.sonatype.configuration.validation.InvalidConfigurationException;

/**
 * An abstract AuthorizationManager, that just throws exceptions for all the write methods. Any call to theses methods
 * should be checked by the <code>supportsWrite()</code> method, so this should never be called.
 * 
 * @author Brian Demers
 */
public abstract class AbstractReadOnlyAuthorizationManager
    implements AuthorizationManager
{

    public boolean supportsWrite()
    {
        return false;
    }

    public Privilege addPrivilege( Privilege privilege )
        throws InvalidConfigurationException
    {
        this.throwException();
        return null;
    }

    public Role addRole( Role role )
        throws InvalidConfigurationException
    {
        this.throwException();
        return null;
    }

    public void deletePrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        this.throwException();
    }

    public void deleteRole( String roleId )
        throws NoSuchRoleException
    {
        this.throwException();
    }

    public Privilege updatePrivilege( Privilege privilege )
        throws NoSuchPrivilegeException, InvalidConfigurationException
    {
        this.throwException();
        return null;
    }

    public Role updateRole( Role role )
        throws NoSuchRoleException, InvalidConfigurationException
    {
        this.throwException();
        return null;
    }

    private void throwException()
    {
        throw new IllegalStateException( "AuthorizationManager: '" + this.getSource()
            + "' does not support write operations." );
    }

}
