/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
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
package org.sonatype.nexus.artifact;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author Jason van Zyl
 * @author cstamas
 */
public class M2GavCalculator
{

    public static Gav calculate( String str )
    {
        try
        {
            String s = str.startsWith( "/" ) ? str.substring( 1 ) : str;

            int vEndPos = s.lastIndexOf( '/' );

            if ( vEndPos == -1 )
            {
                return null;
            }

            int aEndPos = s.lastIndexOf( '/', vEndPos - 1 );

            if ( aEndPos == -1 )
            {
                return null;
            }

            int gEndPos = s.lastIndexOf( '/', aEndPos - 1 );

            if ( gEndPos == -1 )
            {
                return null;
            }

            String g = s.substring( 0, gEndPos ).replace( '/', '.' );
            String a = s.substring( gEndPos + 1, aEndPos );
            String v = s.substring( aEndPos + 1, vEndPos );
            String n = s.substring( vEndPos + 1 );

            boolean checksum = false;
            if ( s.endsWith( ".md5" ) )
            {
                checksum = true;
                s = s.substring( 0, s.length() - 4 );
            }
            else if ( s.endsWith( ".sha1" ) )
            {
                checksum = true;
                s = s.substring( 0, s.length() - 5 );
            }

            if ( s.endsWith( "maven-metadata.xml" ) )
            {
                return null;
            }

            String ext = s.substring( s.lastIndexOf( '.' ) + 1 );

            boolean snapshot = v.contains( "-SNAPSHOT" );

            boolean primary = false;
            String c = null;

            if ( snapshot )
            {
                String snapshotBuildNumber = null;
                Long snapshotTimestamp = null;

                int vSnapshotStart = vEndPos + a.length() + v.length() - 9 + 3;
                String vSnapshot = s.substring( vSnapshotStart, vSnapshotStart + 8 );
                if ( "SNAPSHOT".equals( vSnapshot ) )
                {
                    // primary?
                    primary = n.substring( 0, a.length() + v.length() + ext.length() + 2 ).equals(
                        a + "-" + v + "." + ext );
                    if ( !primary )
                    {
                        c = s.substring( vEndPos + a.length() + v.length() + 3, s.lastIndexOf( '.' ) );
                    }
                }
                else
                {
                    StringBuffer sb = new StringBuffer( vSnapshot );
                    sb.append( s.substring( vSnapshotStart + sb.length(), vSnapshotStart + sb.length() + 8 ) );
                    try
                    {
                        SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd.HHmmss" );
                        snapshotTimestamp = Long.valueOf( df.parse( sb.toString() ).getTime() );
                    }
                    catch ( ParseException e )
                    {
                    }

                    int buildNumberPos = vSnapshotStart + sb.length();
                    while ( s.charAt( buildNumberPos ) >= '0' && s.charAt( buildNumberPos ) <= '9' )
                    {
                        sb.append( s.charAt( buildNumberPos ) );
                        buildNumberPos++;
                    }
                    snapshotBuildNumber = sb.toString();

                    primary = !checksum
                        && n
                            .equals( a + "-" + v.substring( 0, v.length() - 9 ) + "-" + snapshotBuildNumber + "." + ext );
                    if ( !primary )
                    {
                        if ( checksum )
                        {
                            c = s.substring(
                                vEndPos + a.length() + v.length() - 9 + 3 + snapshotBuildNumber.length(),
                                s.lastIndexOf( '.' ) );
                            if ( c.length() == 0 )
                            {
                                c = null;
                            }
                        }
                        else
                        {
                            c = s.substring( s.lastIndexOf( '-' ) + 1, s.lastIndexOf( '.' ) );
                        }
                    }
                }

                return new Gav( g, a, v, c, ext, snapshotBuildNumber, snapshotTimestamp, n, primary, snapshot, checksum );
            }
            else
            {
                if ( n.startsWith( a + "-" + v ) )
                {
                    primary = !checksum && n.equals( a + "-" + v + "." + ext );
                    if ( !primary )
                    {
                        if ( vEndPos + a.length() + v.length() + 3 < s.lastIndexOf( '.' ) )
                        {
                            c = s.substring( vEndPos + a.length() + v.length() + 3, s.lastIndexOf( '.' ) );
                        }
                        else
                        {
                            c = null;
                        }
                    }
                    return new Gav( g, a, v, c, ext, null, null, n, primary, snapshot, checksum );
                }
                else
                {
                    return null;
                }
            }
        }
        catch ( StringIndexOutOfBoundsException e )
        {
            return null;
        }

    }

    public static String calculateRepositoryPath( Gav gav )
    {
        StringBuffer path = new StringBuffer( "/" );

        path.append( gav.getGroupId().replaceAll( "\\.", "/" ) );

        path.append( "/" );

        path.append( gav.getArtifactId() );

        path.append( "/" );

        path.append( gav.getVersion() );

        path.append( "/" );

        path.append( calculateArtifactName( gav ) );

        return path.toString();
    }

    public static String calculateArtifactName( Gav gav )
    {
        StringBuffer path = new StringBuffer( gav.getArtifactId() );

        path.append( "-" );

        if ( !gav.isSnapshot() )
        {
            path.append( gav.getVersion() );

        }
        else
        {
            if ( gav.getSnapshotBuildNumber() != null )
            {
                path.append( gav.getVersion().substring( 0, gav.getVersion().length() - 9 ) );

                path.append( "-" );

                path.append( gav.getSnapshotBuildNumber() );
            }
            else
            {
                path.append( gav.getVersion() );
            }

        }

        path.append( ".pom" );

        return path.toString();
    }
}
