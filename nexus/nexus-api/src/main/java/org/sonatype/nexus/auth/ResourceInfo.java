/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.auth;

import org.sonatype.nexus.proxy.access.Action;

/**
 * Resource info collects the description of HOW and WHAT has been accessed.
 * 
 * @author cstamas
 */
public class ResourceInfo
{
    private final String accessProtocol;

    private final String accessMethod;

    private final Action action;

    private final String accessedUri;

    public ResourceInfo( final String accessProtocol, final String accessMethod, final Action action,
                         final String accessedUri )
    {
        this.accessProtocol = accessProtocol;
        this.accessMethod = accessMethod;
        this.action = action;
        this.accessedUri = accessedUri;
    }

    public String getAccessProtocol()
    {
        return accessProtocol;
    }

    public String getAccessMethod()
    {
        return accessMethod;
    }

    public Action getAction()
    {
        return action;
    }

    public String getAccessedUri()
    {
        return accessedUri;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( accessMethod == null ) ? 0 : accessMethod.hashCode() );
        result = prime * result + ( ( accessProtocol == null ) ? 0 : accessProtocol.hashCode() );
        result = prime * result + ( ( accessedUri == null ) ? 0 : accessedUri.hashCode() );
        result = prime * result + ( ( action == null ) ? 0 : action.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ResourceInfo other = (ResourceInfo) obj;
        if ( accessMethod == null )
        {
            if ( other.accessMethod != null )
                return false;
        }
        else if ( !accessMethod.equals( other.accessMethod ) )
            return false;
        if ( accessProtocol == null )
        {
            if ( other.accessProtocol != null )
                return false;
        }
        else if ( !accessProtocol.equals( other.accessProtocol ) )
            return false;
        if ( accessedUri == null )
        {
            if ( other.accessedUri != null )
                return false;
        }
        else if ( !accessedUri.equals( other.accessedUri ) )
            return false;
        if ( action != other.action )
            return false;
        return true;
    }
}
