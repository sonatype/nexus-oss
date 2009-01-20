/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.HashSet;
import java.util.Set;

public class ArtifactoryUser
{
    private String username;
    
    /**
     * Read from Artifactory's security.xml, encrypted with MD5.
     */
    private String password;

    private String email = "changeme@yourcompany.com";

    private boolean isAdmin = false;

    private Set<ArtifactoryGroup> groups = new HashSet<ArtifactoryGroup>();

    public ArtifactoryUser( String username, String password )
    {
        this.username = username;
        
        this.password = password;
    }

    public ArtifactoryUser( String username, String password, String email )
    {
        this.username = username;
        
        this.password = password;

        this.email = email;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }

    public void setAdmin( boolean isAdmin )
    {
        this.isAdmin = isAdmin;
    }

    public Set<ArtifactoryGroup> getGroups()
    {
        return groups;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( !( obj instanceof ArtifactoryUser ) )
        {
            return false;
        }

        ArtifactoryUser user = (ArtifactoryUser) obj;

        return this.username.equals( user.username ) && this.password.equals( user.password )
            && this.email.equals( user.email ) && this.isAdmin == user.isAdmin && groups.equals( user.groups );
    }
}
