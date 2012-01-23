/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
