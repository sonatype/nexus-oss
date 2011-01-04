/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.StringUtils;

public class Mirror
{
    private String id;

    private String url;
    
    private String mirrorOfUrl;

    public Mirror( String id, String url )
    {
        setId( id );
        setUrl( url );
    }
    
    public Mirror( String id, String url, String mirrorOfUrl )
    {
        setId( id );
        setUrl( url );
        setMirrorOfUrl( mirrorOfUrl );
    }

    public String getId()
    {
        return id;
    }

    public String getUrl()
    {
        return url;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    // ==

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null || ( o.getClass() != this.getClass() ) )
        {
            return false;
        }

        Mirror other = (Mirror) o;

        return StringUtils.equals( getId(), other.getId() ) && StringUtils.equals( getUrl(), other.getUrl() );
    }

    public int hashCode()
    {
        int result = 7;

        result = 31 * result + ( id == null ? 0 : id.hashCode() );

        result = 31 * result + ( url == null ? 0 : url.hashCode() );

        return result;
    }

    public void setMirrorOfUrl( String mirrorOfUrl )
    {
        this.mirrorOfUrl = mirrorOfUrl;
    }

    public String getMirrorOfUrl()
    {
        return mirrorOfUrl;
    }
}
