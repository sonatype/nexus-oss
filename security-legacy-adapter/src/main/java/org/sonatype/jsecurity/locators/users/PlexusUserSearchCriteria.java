package org.sonatype.jsecurity.locators.users;

import java.util.HashSet;
import java.util.Set;

public class PlexusUserSearchCriteria
{

    private String userId;

    private Set<String> oneOfRoleIds = new HashSet<String>();

    public PlexusUserSearchCriteria()
    {

    }

    public PlexusUserSearchCriteria( String userId )
    {
        this.userId = userId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId( String userId )
    {
        this.userId = userId;
    }

    public Set<String> getOneOfRoleIds()
    {
        return oneOfRoleIds;
    }

    public void setOneOfRoleIds( Set<String> oneOfRoleIds )
    {
        this.oneOfRoleIds = oneOfRoleIds;
    }

}
