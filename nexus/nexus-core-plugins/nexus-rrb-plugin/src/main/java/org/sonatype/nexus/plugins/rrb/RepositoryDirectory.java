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
package org.sonatype.nexus.plugins.rrb;

import javax.xml.bind.annotation.XmlType;

@XmlType( name = "node" )
public class RepositoryDirectory
{
    @Override
    public String toString()
    {
        return "RepositoryDirectory [lastModified=" + lastModified + ", leaf=" + leaf + ", relativePath="
            + relativePath + ", resourceURI=" + resourceURI + ", sizeOnDisk=" + sizeOnDisk + ", text=" + text + "]";
    }

    private String resourceURI = "";

    private String relativePath = "";

    private String text = "";

    private boolean leaf;

    private String lastModified = "";

    private int sizeOnDisk = -1;

    public String getResourceURI()
    {
        return resourceURI;
    }

    public void setResourceURI( String baseUrl )
    {
        this.resourceURI = baseUrl;
    }

    public String getText()
    {
        return text;
    }

    public void setText( String name )
    {
        this.text = name;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( resourceURI == null ) ? 0 : resourceURI.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final RepositoryDirectory other = (RepositoryDirectory) obj;
        if ( resourceURI == null )
        {
            if ( other.resourceURI != null )
            {
                return false;
            }
        }
        else if ( !resourceURI.equals( other.resourceURI ) )
        {
            return false;
        }
        return true;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public void setRelativePath( String relativePath )
    {
        this.relativePath = relativePath;
    }

    public boolean isLeaf()
    {
        return leaf;
    }

    public void setLeaf( boolean leaf )
    {
        this.leaf = leaf;
    }

    public String getLastModified()
    {
        return lastModified;
    }

    public void setLastModified( String lastModified )
    {
        this.lastModified = lastModified;
    }

    public int getSizeOnDisk()
    {
        return sizeOnDisk;
    }

    public void setSizeOnDisk( int sizeOnDisk )
    {
        this.sizeOnDisk = sizeOnDisk;
    }
}