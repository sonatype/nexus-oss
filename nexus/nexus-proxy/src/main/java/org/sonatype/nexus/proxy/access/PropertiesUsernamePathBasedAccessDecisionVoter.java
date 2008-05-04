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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.security.User;

/**
 * Voter that allows/denies the repository access based on property file.
 * <p>
 * A property file looks like this:
 * 
 * <pre>
 * username/some/path1 = perm1,perm2
 * username/ = perm3,perm4
 * ...
 * </pre>
 * 
 * (the usernames are concatenated with path).
 * 
 * @author cstamas
 * @plexus.component role-hint="properties"
 */
public class PropertiesUsernamePathBasedAccessDecisionVoter
    implements AccessDecisionVoter
{

    public static final String PROPERTIES_PATH = "propertiesFile";

    /**
     * The properties path.
     */
    protected String propertiesPath;

    /** The properties base. */
    private Properties properties;

    /**
     * Gets the properties path.
     * 
     * @return the properties path
     */
    public String getPropertiesPath()
    {
        return propertiesPath;
    }

    /**
     * Sets the properties path.
     * 
     * @param propertiesPath the new properties path
     */
    public void setPropertiesPath( String propertiesPath )
    {
        this.propertiesPath = propertiesPath;
    }

    /**
     * Gets the properties.
     * 
     * @return the properties
     */
    public Properties getProperties()
    {
        if ( properties == null )
        {
            try
            {
                loadProperties( getPropertiesPath() );
            }
            catch ( IOException e )
            {
                throw new IllegalArgumentException(
                    "Could not initialize NexusAuthentication, nor auth properties nor propertiesPath parameter is given!",
                    e );
            }
        }
        return properties;
    }

    /**
     * Sets the properties.
     * 
     * @param properties the new properties
     */
    public void setProperties( Properties properties )
    {
        this.properties = properties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.access.AccessDecisionVoter#vote(org.sonatype.nexus.ProximityRequest,
     *      org.sonatype.nexus.Repository, org.sonatype.nexus.access.RepositoryPermission)
     */
    public int vote( ResourceStoreRequest request, Repository repository, RepositoryPermission permission )
    {
        return authorizeTree( getProperties(), (User) request.getRequestContext().get( REQUEST_USER ), request
            .getRequestPath(), permission );
    }

    /**
     * Authorize tree.
     * 
     * @param path2rights the path2rights
     * @param path the path
     * @param permission the permission
     * @return the int
     */
    private int authorizeTree( Properties path2rights, User user, String path, RepositoryPermission permission )
    {
        String pathRights = path2rights.getProperty( user.getUsername() + path );

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

        return authorizeTree( path2rights, user, parent, permission );
    }

    /**
     * Load properties.
     * 
     * @param resource the resource
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void loadProperties( String resource )
        throws IOException
    {
        if ( resource == null )
        {
            throw new IllegalArgumentException( "Authorization source properties file path cannot be 'null'!" );
        }
        if ( this.properties == null )
        {
            this.properties = new Properties();
        }

        // First see if the resource is a valid file
        File resourceFile = new File( resource );
        if ( resourceFile.exists() )
        {
            this.properties.load( new FileInputStream( resourceFile ) );
            return;
        }

        // Otherwise try to load it from the classpath
        InputStream is = getClass().getClassLoader().getResourceAsStream( resource );
        if ( is != null )
        {
            this.properties.load( is );
            return;
        }

        throw new IllegalArgumentException( "Authorization source cannot be loaded because it is not found on "
            + resource );
    }

}
