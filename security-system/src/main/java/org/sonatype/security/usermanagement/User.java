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
