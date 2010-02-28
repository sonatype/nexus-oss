/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.plugin.metadata;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.maven.ArtifactPackagingMapper;

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
