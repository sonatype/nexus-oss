package org.sonatype.nexus.mock.models;

public class User
{
    public static final User ADMIN = new User( "admin", "admin123" );

    private String username;

    private String password;

    public User( String username, String password )
    {
        this.username = username;
        this.password = password;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }
}
