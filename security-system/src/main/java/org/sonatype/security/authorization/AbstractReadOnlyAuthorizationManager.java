package org.sonatype.security.authorization;

import org.sonatype.configuration.validation.InvalidConfigurationException;

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
        throws NoSuchPrivilegeException,
            InvalidConfigurationException
    {
        this.throwException();
        return null;
    }

    public Role updateRole( Role role )
        throws NoSuchRoleException,
            InvalidConfigurationException
    {
        this.throwException();
        return null;
    }

    private void throwException()
    {
        throw new IllegalStateException( "AuthorizationManager: '" + this.getSource() + "' does not support write operations." );
    }

}
