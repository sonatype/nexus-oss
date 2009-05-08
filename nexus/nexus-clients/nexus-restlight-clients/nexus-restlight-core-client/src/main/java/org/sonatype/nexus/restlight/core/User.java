package org.sonatype.nexus.restlight.core;

import java.util.ArrayList;
import java.util.List;

public class User
{
    private String resourceURI;

    private String userId;

    private String name;

    private String email;

    private String status;

    private boolean userManaged;

    private List<String> roles = new ArrayList<String>();

    public String getUserId()
    {
        return userId;
    }

    public void setUserId( String userId )
    {
        this.userId = userId;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public String getResourceURI()
    {
        return resourceURI;
    }

    public void setResourceURI( String resourceURI )
    {
        this.resourceURI = resourceURI;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus( String status )
    {
        this.status = status;
    }

    public boolean isUserManaged()
    {
        return userManaged;
    }

    public void setUserManaged( boolean userManaged )
    {
        this.userManaged = userManaged;
    }

    public List<String> getRoles()
    {
        return roles;
    }

    public void setRoles( List<String> roles )
    {
        this.roles = roles;
    }

}
