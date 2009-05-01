package org.sonatype.security.usermanagement;

import java.util.Set;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.authorization.Role;

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
        throws UserNotFoundException,
            InvalidConfigurationException
    {        
    }

    public User updateUser( User user )
        throws UserNotFoundException,
            InvalidConfigurationException
    {
        this.throwException();
        return null;
    }

    private void throwException()
    {
        throw new IllegalStateException( "UserManager: '" + this.getSource() + "' does not support write operations." );
    }

}
