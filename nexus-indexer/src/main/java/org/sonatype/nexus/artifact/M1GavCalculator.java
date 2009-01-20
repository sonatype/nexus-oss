/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.artifact;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The M1 GAV Calculator.
 * 
 * @author Jason van Zyl
 * @author Tamas Cservenak
 * @plexus.component role-hint="maven1"
 */
public class M1GavCalculator
    extends AbstractGavCalculator
{

    private static final Pattern pat1 = Pattern.compile( "^([^0-9]+)-([0-9].+)\\.([^0-9]+)(\\.md5|\\.sha1){0,1}$" );

    private static final Pattern pat2 = Pattern.compile( "^([a-z0-9-_]+)-([0-9-].+)\\.([^0-9]+)(\\.md5|\\.sha1){0,1}$" );

    public Gav pathToGav( String str )
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
            Gav.HashType checksumType = null;
            if ( s.endsWith( ".md5" ) )
            {
                checksum = true;
                checksumType = Gav.HashType.md5;
                s = s.substring( 0, s.length() - 4 );
            }
            else if ( s.endsWith( ".sha1" ) )
            {
                checksum = true;
                checksumType = Gav.HashType.sha1;
                s = s.substring( 0, s.length() - 5 );
            }

            if ( s.endsWith( "maven-metadata.xml" ) )
            {
                return null;
            }

            String ext = s.substring( s.lastIndexOf( '.' ) + 1 );

            Matcher m = pat1.matcher( n );
            if ( m.matches() )
            {
                String a = m.group( 1 );
                String version = m.group( 2 );
                if ( classifier != null )
                {
                    version = version.substring( 0, version.length() - ( classifier.length() + 1 ) );
                }

                return new Gav(
                    g,
                    a,
                    version,
                    classifier,
                    ext,
                    null,
                    null,
                    n,
                    snapshot,
                    checksum,
                    checksumType,
                    false,
                    null );
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

                    return new Gav(
                        g,
                        a,
                        version,
                        classifier,
                        ext,
                        null,
                        null,
                        n,
                        snapshot,
                        checksum,
                        checksumType,
                        false,
                        null );
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

    /**
     * // XXX this is not accurate, m1 is using packaging as an artifact folder name.
     *  
     * @see org.apache.maven.artifact.repository.layout.LegacyRepositoryLayout#pathOf(org.apache.maven.artifact.Artifact)
     * @see org.apache.maven.artifact.handler.DefaultArtifactHandler#getDirectory()
     */
    public String gavToPath( Gav gav )
    {
        StringBuffer path = new StringBuffer( "/" );

        path.append( gav.getGroupId() );

        path.append( "/" );

        if ( gav.getClassifier() == null )
        {
            path.append( gav.getExtension() );

            path.append( "s" );
        }
        else
        {
            if ( gav.getClassifier().startsWith( "source" ) )
            {
                path.append( "java-source" );
            }
            else
            {
                path.append( gav.getClassifier() );
            }
            path.append( "s" );
        }

        path.append( "/" );

        path.append( gav.getArtifactId() );

        path.append( "-" );

        path.append( gav.getVersion() );

        if ( gav.getClassifier() != null )
        {
            path.append( "-" );

            path.append( gav.getClassifier() );
        }

        path.append( "." );

        path.append( gav.getExtension() );

        if ( gav.isHash() )
        {
            path.append( "." );

            path.append( gav.getHashType().toString() );
        }

        return path.toString();
    }

}
