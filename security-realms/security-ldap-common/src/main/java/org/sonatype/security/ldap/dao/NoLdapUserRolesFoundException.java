package org.sonatype.security.ldap.dao;

public class NoLdapUserRolesFoundException
    extends Exception
{

    private final String username;

    public NoLdapUserRolesFoundException( String username )
    {
        super( "No roles found for user: " + username );
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

}
