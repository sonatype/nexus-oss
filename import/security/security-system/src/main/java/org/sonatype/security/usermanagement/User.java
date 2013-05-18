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

/**
 * A user contains attributes, userId, name, email address, roles, etc.
 * 
 * @author Brian Demers
 */
public interface User
{
    /**
     * @return The users Id.
     */
    public String getUserId();

    /**
     * Set the user Id.
     * 
     * @param userId
     */
    public void setUserId( String userId );

    /**
     * @return the Users name.
     * @deprecated use getFirstName, and getLastName
     */
    @Deprecated
    public String getName();

    /**
     * Sets the users name.
     * 
     * @param name
     * @deprecated use setFirstName, and setLastName
     */
    @Deprecated
    public void setName( String name );

    /**
     * Gets the users first name.
     * 
     * @return
     */
    public String getFirstName();

    /**
     * Sets the users first name.
     * 
     * @param firstName
     */
    public void setFirstName( String firstName );

    /**
     * Gets the users last name.
     * 
     * @return
     */
    public String getLastName();

    /**
     * Sets the users last name.
     * 
     * @param lastName
     */
    public void setLastName( String lastName );

    /**
     * @return the users email address.
     */
    public String getEmailAddress();

    /**
     * Set the users email address.
     * 
     * @param emailAddress
     */
    public void setEmailAddress( String emailAddress );

    /**
     * @return the users source Id.
     */
    public String getSource();

    /**
     * Set the users source.
     * 
     * @param source
     */
    public void setSource( String source );

    /**
     * Adds a role Identifier to the user.
     * 
     * @param roleIdentifier
     */
    public void addRole( RoleIdentifier roleIdentifier );

    /**
     * Remove a Role Identifier from the user.
     * 
     * @param roleIdentifier
     * @return
     */
    public boolean removeRole( RoleIdentifier roleIdentifier );

    /**
     * Adds a set of RoleIdentifier to the user.
     * 
     * @param roleIdentifiers
     */
    public void addAllRoles( Set<RoleIdentifier> roleIdentifiers );

    /**
     * @return returns all the users roles.
     */
    public Set<RoleIdentifier> getRoles();

    /**
     * Sets the users roles.
     * 
     * @param roles
     */
    public void setRoles( Set<RoleIdentifier> roles );

    /**
     * @return the users status.
     */
    public UserStatus getStatus();

    /**
     * Sets the users status.
     * 
     * @param status
     */
    public void setStatus( UserStatus status );

    @Deprecated
    public boolean isReadOnly();

    @Deprecated
    public void setReadOnly( boolean readOnly );

}
