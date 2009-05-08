package org.sonatype.nexus.restlight.core;

import java.util.ArrayList;
import java.util.List;

public class Role
{
    private String resourceURI;

    private String id;

    private String name;

    private String description;

    private int sessionTimeout;

    private boolean userManaged = true;

    private List<String> roles = new ArrayList<String>();

    private List<String> privileges = new ArrayList<String>();

    public String getResourceURI()
    {
        return resourceURI;
    }

    public void setResourceURI( String resourceURI )
    {
        this.resourceURI = resourceURI;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public int getSessionTimeout()
    {
        return sessionTimeout;
    }

    public void setSessionTimeout( int sessionTimeout )
    {
        this.sessionTimeout = sessionTimeout;
    }

    public boolean isUserManaged()
    {
        return userManaged;
    }

    public void setUserManaged( boolean userManaged )
    {
        this.userManaged = userManaged;
    }

    public List<String> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges( List<String> privileges )
    {
        this.privileges = privileges;
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
