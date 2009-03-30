/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.artifact;

import java.util.StringTokenizer;

/**
 * Default implementation of artifact versioning.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * 
 */
public class DefaultArtifactVersion
    implements ArtifactVersion
{
    private Integer majorVersion;

    private Integer minorVersion;

    private Integer incrementalVersion;

    private Integer buildNumber;

    private String qualifier;

    public DefaultArtifactVersion( String version )
    {
        parseVersion( version );
    }

    @Override
    public int hashCode()
    {
        return 11 + toString().hashCode();
    }

    @Override
    public boolean equals( Object other )
    {
        if ( other == null )
        {
            return false;
        }

        if ( !( other instanceof DefaultArtifactVersion ) )
        {
            return false;
        }

        return compareTo( (DefaultArtifactVersion) other ) == 0;
    }

    public int compareTo( ArtifactVersion otherVersion )
    {
        int result = compareIntegers( majorVersion, otherVersion.getMajorVersion() );
        if ( result == 0 )
        {
            result = compareIntegers( minorVersion, otherVersion.getMinorVersion() );
        }
        if ( result == 0 )
        {
            result = compareIntegers( incrementalVersion, otherVersion.getIncrementalVersion() );
        }
        if ( result == 0 )
        {
            if ( ( getBuildNumber() != 0 ) || ( otherVersion.getBuildNumber() != 0 ) )
            {
                result = compareIntegers( getBuildNumber(), otherVersion.getBuildNumber() );
            }
            else if ( qualifier != null )
            {
                if ( otherVersion.getQualifier() != null )
                {
                    if ( ( qualifier.length() > otherVersion.getQualifier().length() ) &&
                        qualifier.startsWith( otherVersion.getQualifier() ) )
                    {
                        // here, the longer one that otherwise match is considered older
                        result = -1;
                    }
                    else if ( ( qualifier.length() < otherVersion.getQualifier().length() ) &&
                        otherVersion.getQualifier().startsWith( qualifier ) )
                    {
                        // here, the longer one that otherwise match is considered older
                        result = 1;
                    }
                    else
                    {
                        result = qualifier.compareTo( otherVersion.getQualifier() );
                    }
                }
                else
                {
                    // otherVersion has no qualifier but we do - that's newer
                    result = -1;
                }
            }
            else if ( otherVersion.getQualifier() != null )
            {
                // otherVersion has a qualifier but we don't, we're newer
                result = 1;
            }
        }
        return result;
    }

    private int compareIntegers( Integer i1,
                                 Integer i2 )
    {
        // treat null as 0 in comparison
        if ( i1 == null ? i2 == null : i1.equals( i2 ) )
        {
            return 0;
        }
        else if ( i1 == null )
        {
            return -i2.intValue();
        }
        else if ( i2 == null )
        {
            return i1.intValue();
        }
        else
        {
            return i1.intValue() - i2.intValue();
        }
    }

    public int getMajorVersion()
    {
        return majorVersion != null ? majorVersion.intValue() : 0;
    }

    public int getMinorVersion()
    {
        return minorVersion != null ? minorVersion.intValue() : 0;
    }

    public int getIncrementalVersion()
    {
        return incrementalVersion != null ? incrementalVersion.intValue() : 0;
    }

    public int getBuildNumber()
    {
        return buildNumber != null ? buildNumber.intValue() : 0;
    }

    public String getQualifier()
    {
        return qualifier;
    }

    public final void parseVersion( String version )
    {
        int index = version.indexOf( "-" );

        String part1;
        String part2 = null;

        if ( index < 0 )
        {
            part1 = version;
        }
        else
        {
            part1 = version.substring( 0, index );
            part2 = version.substring( index + 1 );
        }

        if ( part2 != null )
        {
            try
            {
                if ( ( part2.length() == 1 ) || !part2.startsWith( "0" ) )
                {
                    buildNumber = Integer.valueOf( part2 );
                }
                else
                {
                    qualifier = part2;
                }
            }
            catch ( NumberFormatException e )
            {
                qualifier = part2;
            }
        }

        if ( ( part1.indexOf( "." ) < 0 ) && !part1.startsWith( "0" ) )
        {
            try
            {
                majorVersion = Integer.valueOf( part1 );
            }
            catch ( NumberFormatException e )
            {
                // qualifier is the whole version, including "-"
                qualifier = version;
                buildNumber = null;
            }
        }
        else
        {
            boolean fallback = false;
            StringTokenizer tok = new StringTokenizer( part1, "." );
            try
            {
                majorVersion = getNextIntegerToken( tok );
                if ( tok.hasMoreTokens() )
                {
                    minorVersion = getNextIntegerToken( tok );
                }
                if ( tok.hasMoreTokens() )
                {
                    incrementalVersion = getNextIntegerToken( tok );
                }
                if ( tok.hasMoreTokens() )
                {
                    fallback = true;
                }
            }
            catch ( NumberFormatException e )
            {
                fallback = true;
            }

            if ( fallback )
            {
                // qualifier is the whole version, including "-"
                qualifier = version;
                majorVersion = null;
                minorVersion = null;
                incrementalVersion = null;
                buildNumber = null;
            }
        }
    }

    private static Integer getNextIntegerToken( StringTokenizer tok )
    {
        String s = tok.nextToken();
        if ( ( s.length() > 1 ) && s.startsWith( "0" ) )
        {
            throw new NumberFormatException( "Number part has a leading 0: '" + s + "'" );
        }
        return Integer.valueOf( s );
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        if ( majorVersion != null )
        {
            buf.append( majorVersion );
        }
        if ( minorVersion != null )
        {
            buf.append( "." );
            buf.append( minorVersion );
        }
        if ( incrementalVersion != null )
        {
            buf.append( "." );
            buf.append( incrementalVersion );
        }
        if ( buildNumber != null )
        {
            buf.append( "-" );
            buf.append( buildNumber );
        }
        else if ( qualifier != null )
        {
            if ( buf.length() > 0 )
            {
                buf.append( "-" );
            }
            buf.append( qualifier );
        }
        return buf.toString();
    }

}
