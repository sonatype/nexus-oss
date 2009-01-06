package org.sonatype.jsecurity.realms.simple;

import java.util.HashSet;
import java.util.Set;

public class SimpleUser
{

    private String name;

    private String userId;

    private String email;

    private String password;

    private Set<String> roles = new HashSet<String>();

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId( String userId )
    {
        this.userId = userId;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public Set<String> getRoles()
    {
        return roles;
    }

    public void addRole( String role )
    {
        this.roles.add( role );
    }
}
