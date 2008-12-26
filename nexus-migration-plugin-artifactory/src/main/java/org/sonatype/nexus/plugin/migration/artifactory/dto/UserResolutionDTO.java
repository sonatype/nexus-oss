package org.sonatype.nexus.plugin.migration.artifactory.dto;

public class UserResolutionDTO
{

    private String id;
    
    private String password;

    private boolean isAdmin;

    private String email;

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }

    public void setAdmin( boolean isAdmin )
    {
        this.isAdmin = isAdmin;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

}
