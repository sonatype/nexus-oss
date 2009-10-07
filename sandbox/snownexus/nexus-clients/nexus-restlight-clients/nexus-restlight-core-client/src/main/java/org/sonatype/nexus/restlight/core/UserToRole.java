package org.sonatype.nexus.restlight.core;

import java.util.ArrayList;
import java.util.List;

public class UserToRole
{
    private String userId;

    private String source;

    private List<String> roles = new ArrayList<String>();

    public String getUserId()
    {
        return userId;
    }

    public void setUserId( String userId )
    {
        this.userId = userId;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource( String source )
    {
        this.source = source;
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
