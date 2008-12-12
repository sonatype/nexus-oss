package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.HashSet;
import java.util.Set;

public class ArtifactoryUser
{
    private String username;

    /** Read form Artifactory's security.xml, so it's encrypted */
    private String password;

    private Set<ArtifactoryRole> roles = new HashSet<ArtifactoryRole>();

    public ArtifactoryUser( String username, String password )
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

    public Set<ArtifactoryRole> getRoles()
    {
        return roles;
    }

    public void addRole( ArtifactoryRole role )
    {
        roles.add( role );
    }

}
