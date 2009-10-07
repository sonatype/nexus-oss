package org.sonatype.nexus.restlight.core;

import java.util.ArrayList;
import java.util.List;

public class PlexusUser
{
    private String userId;

    private String name;

    private String source;

    private String email;

    private List<PlexusRole> plexusRoles = new ArrayList<PlexusRole>();

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

    public String getSource()
    {
        return source;
    }

    public void setSource( String source )
    {
        this.source = source;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public List<PlexusRole> getPlexusRoles()
    {
        return plexusRoles;
    }

    public void setPlexusRoles( List<PlexusRole> plexusRoles )
    {
        this.plexusRoles = plexusRoles;
    }

}
