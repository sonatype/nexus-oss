/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.access;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Quasi complex voter that allows/denies the repository access based on property files. The property files are searched
 * on the propertiesBase and are loaded with Classloader (for demo purposes).
 * <p>
 * The name of the property file to be looked up is formed as:
 * 
 * <pre>
 * propertiesBase + &lt;username&gt;-&lt;repositoryId&gt;.properties
 * </pre>
 * 
 * <p>
 * A property file looks like this:
 * 
 * <pre>
 * path1 = perm1,perm2
 * path2 = perm3,perm4
 * </pre>
 * 
 * <p>
 * If the property file is not found for given repoId and username, the user has access as defined in default rights
 * properties.
 * 
 * @author cstamas
 * @plexus.component instantiation-strategy="per-lookup" role-hint="complex-props"
 */
public class ComplexPropertiesFileUsernameBasedAccessDecisionVoter
    extends AbstractPropertiesFileBasedAccessDecisionVoter
{

    /** The default rights. */
    private Properties defaultRights;

    /** The properties base. */
    private String propertiesBase;

    /**
     * Gets the properties base.
     * 
     * @return the properties base
     */
    public String getPropertiesBase()
    {
        if ( this.propertiesBase == null )
        {
            return "/";
        }
        else
        {
            return propertiesBase;
        }
    }

    /**
     * Sets the properties base.
     * 
     * @param propertiesBase the new properties base
     */
    public void setPropertiesBase( String propertiesBase )
    {
        this.propertiesBase = propertiesBase;
        if ( !this.propertiesBase.endsWith( "/" ) )
        {
            this.propertiesBase = this.propertiesBase + "/";
        }
    }

    /**
     * Gets the default rights.
     * 
     * @return the default rights
     */
    public Properties getDefaultRights()
    {
        if ( defaultRights == null )
        {
            this.defaultRights = new Properties();
        }
        return defaultRights;
    }

    /**
     * Sets the default rights.
     * 
     * @param defaultRights the new default rights
     */
    public void setDefaultRights( Properties defaultRights )
    {
        this.defaultRights = defaultRights;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.access.AccessDecisionVoter#vote(org.sonatype.nexus.ProximityRequest,
     *      org.sonatype.nexus.Repository, org.sonatype.nexus.access.RepositoryPermission)
     */
    public int vote( ResourceStoreRequest request, Repository repository, RepositoryPermission permission )
    {
        Properties props = getProperties( (String) request.getRequestContext().get( REQUEST_USER ), repository.getId() );

        return authorizeTree( props, request.getRequestPath(), permission );
    }

    /**
     * Authorize tree.
     * 
     * @param path2rights the path2rights
     * @param path the path
     * @param permission the permission
     * @return the int
     */
    private int authorizeTree( Properties path2rights, String path, RepositoryPermission permission )
    {
        String pathRights = path2rights.getProperty( path );

        if ( pathRights != null )
        {
            List<String> userList = Arrays.asList( pathRights.split( "," ) );

            if ( userList.contains( permission.getId() ) )
            {
                return ACCESS_APPROVED;
            }
        }

        String parent = ( new File( path ) ).getParent();
        if ( parent == null )
        {
            return ACCESS_DENIED;
        }

        return authorizeTree( path2rights, parent, permission );
    }

    /**
     * Gets the properties.
     * 
     * @param username the username
     * @param repositoryId the repository id
     * @return the properties
     */
    protected Properties getProperties( String username, String repositoryId )
    {
        Properties props = new Properties( getDefaultRights() );

        if ( username != null && repositoryId != null )
        {
            String propsName = getPropertiesBase() + username + "-" + repositoryId + ".properties";

            try
            {
                props.putAll( loadProperties( propsName ) );
            }
            catch ( IOException e )
            {
                getLogger().info( "There is no properties file with name " + propsName + ". Using defaults." );
            }
        }
        else
        {
            getLogger().info( "There is no security information in request. Using defaults." );
        }
        return props;
    }

}
