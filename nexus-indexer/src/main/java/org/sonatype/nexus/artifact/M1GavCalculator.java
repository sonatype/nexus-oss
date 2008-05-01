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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jason van Zyl
 * @author cstamas
 */
public class M1GavCalculator
{

    private static final Pattern pat1 = Pattern.compile( "^([^0-9]+)-([0-9].+)\\.([^0-9]+)(\\.md5|\\.sha1){0,1}$" );

    private static final Pattern pat2 = Pattern.compile( "^([a-z0-9]+)-([0-9-].+)\\.([^0-9]+)(\\.md5|\\.sha1){0,1}$" );

    public static Gav calculate( String str )
    {
        try
        {
            String s = str.startsWith( "/" ) ? str.substring( 1 ) : str;

            int n1 = s.lastIndexOf( '/' );

            if ( n1 == -1 )
            {
                return null;
            }

            int n2 = s.lastIndexOf( '/', n1 - 1 );

            if ( n2 == -1 )
            {
                return null;
            }

            String g = s.substring( 0, n2 ).replace( '/', '.' );
            String middle = s.substring( n2 + 1, n1 );
            String n = s.substring( n1 + 1 );

            String classifier = null;
            if ( "java-sources".equals( middle ) )
            {
                classifier = "sources";
            }
            else if ( "javadocs".equals( middle ) )
            {
                classifier = "javadoc";
            }

            boolean snapshot = s.contains( "SNAPSHOT" );

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

            boolean primary = classifier == null && !checksum;

            Matcher m = pat1.matcher( n );
            if ( m.matches() )
            {
                String a = m.group( 1 );
                String version = m.group( 2 );
                if ( classifier != null )
                {
                    version = version.substring( 0, version.length() - ( classifier.length() + 1 ) );
                }

                return new Gav( g, a, version, classifier, ext, null, null, n, primary, snapshot, checksum );
            }
            else
            {
                m = pat2.matcher( n );
                if ( m.matches() )
                {
                    String a = m.group( 1 );
                    String version = m.group( 2 );
                    if ( classifier != null )
                    {
                        version = version.substring( 0, version.length() - ( classifier.length() + 1 ) );
                    }

                    return new Gav( g, a, version, classifier, ext, null, null, n, primary, snapshot, checksum );
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
        catch ( IndexOutOfBoundsException e )
        {
            return null;
        }
    }

    public static String calculateRepositoryPath( Gav gav )
    {
        StringBuffer path = new StringBuffer( "/" );

        path.append( gav.getGroupId() );

        path.append( "/" );

        path.append( "poms" );

        path.append( "/" );

        path.append( gav.getArtifactId() );

        path.append( "-" );

        path.append( gav.getVersion() );

        path.append( ".pom" );

        return path.toString();
    }
}
