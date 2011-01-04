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
package org.sonatype.plugin.metadata;

import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.codehaus.plexus.util.StringUtils;

/**
 * Trivial Group:Artifact:Version identifier.
 */
public final class GAVCoordinate
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String classifier;

    private final String type;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public GAVCoordinate( final String groupId, final String artifactId, final String version )
    {
        this( groupId, artifactId, version, null, null );
    }

    public GAVCoordinate( final String groupId, final String artifactId, final String version, final String classifier,
                          final String type )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.type = "jar".equals( type ) ? null : type;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public String getFinalName( final ArtifactPackagingMapper packagingMapper )
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( artifactId ).append( '-' ).append( version );
        if ( StringUtils.isNotEmpty( classifier ) )
        {
            buf.append( '-' ).append( classifier );
        }
        if ( StringUtils.isNotEmpty( type ) )
        {
            buf.append( '.' ).append( packagingMapper.getExtensionForPackaging( type ) );
        }
        else
        {
            buf.append( ".jar" );
        }
        return buf.toString();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( !( rhs instanceof GAVCoordinate ) )
        {
            return false;
        }
        return toString().equals( rhs.toString() );
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( groupId ).append( ':' ).append( artifactId ).append( ':' ).append( version );
        final boolean haveType = StringUtils.isNotEmpty( type );
        if ( StringUtils.isNotEmpty( classifier ) )
        {
            buf.append( ':' ).append( classifier );
        }
        else if ( haveType )
        {
            buf.append( ':' );
        }
        if ( haveType )
        {
            buf.append( ':' ).append( type );
        }
        return buf.toString();
    }
}
