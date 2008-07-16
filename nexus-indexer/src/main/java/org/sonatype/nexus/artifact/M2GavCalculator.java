/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.artifact;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * The M2 GAV Calculator.
 * 
 * @author Jason van Zyl
 * @author cstamas
 * @plexus.component role-hint="maven2"
 */
public class M2GavCalculator
    extends AbstractGavCalculator
    implements GavCalculator
{
    public Gav pathToGav( String str )
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
            boolean signature = false;
            Gav.HashType checksumType = null;
            Gav.SignatureType signatureType = null;
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

            if ( s.endsWith( ".asc" ) )
            {
                signature = true;
                signatureType = Gav.SignatureType.gpg;
                s = s.substring( 0, s.length() - 4 );
            }

            if ( s.endsWith( "maven-metadata.xml" ) )
            {
                return null;
            }

            String ext = null;

            // TODO: refine this, the version may contain dot too!
            if ( n.contains( "." ) )
            {
                ext = s.substring( s.lastIndexOf( '.' ) + 1 );
            }
            else
            {
                // NX-563: not allowing extensionless paths to be interpreted as artifact
                return null;
            }

            boolean snapshot = v.contains( "-SNAPSHOT" ) || ( !getEnforcer().isStrict() && v.equals( "SNAPSHOT" ) );

            boolean primary = false;
            String c = null;

            if ( snapshot )
            {
                String bv = null;
                Integer snapBuildNr = null;
                Long snapshotTimestamp = null;

                String snapshotBuildNumber = null;

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
                    bv = v;
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
                    StringBuffer bnr = new StringBuffer();
                    while ( s.charAt( buildNumberPos ) >= '0' && s.charAt( buildNumberPos ) <= '9' )
                    {
                        sb.append( s.charAt( buildNumberPos ) );
                        bnr.append( s.charAt( buildNumberPos ) );
                        buildNumberPos++;
                    }
                    snapshotBuildNumber = sb.toString();
                    snapBuildNr = Integer.parseInt( bnr.toString() );

                    if ( getEnforcer().isStrict() )
                    {
                        primary = !checksum
                            && !signature
                            && n.equals( a + "-" + v.substring( 0, v.length() - 9 ) + "-" + snapshotBuildNumber + "."
                                + ext );
                    }
                    else
                    {
                        primary = !checksum
                            && !signature
                            && n.equals( a + "-"
                                + ( ( v.length() > 9 ) ? ( v.substring( 0, v.length() - 9 ) + "-" ) : "" )
                                + snapshotBuildNumber + "." + ext );
                    }
                    
                    if ( !primary )
                    {
                        if ( vEndPos + a.length() + v.length() - "-SNAPSHOT".length() + 3 + snapshotBuildNumber.length()
                            < s.lastIndexOf( "." ) )
                        {
                            c = s.substring( s.lastIndexOf( '-' ) + 1, s.lastIndexOf( '.' ) );   
                        }
                    }

                    v = bv.substring( 0, bv.length() - 8 ) + snapshotBuildNumber;
                }

                return new Gav(
                    g,
                    a,
                    v,
                    c,
                    ext,
                    snapBuildNr,
                    snapshotTimestamp,
                    n,
                    snapshot,
                    checksum,
                    checksumType,
                    signature,
                    signatureType );
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
                    return new Gav(
                        g,
                        a,
                        v,
                        c,
                        ext,
                        null,
                        null,
                        n,
                        snapshot,
                        checksum,
                        checksumType,
                        signature,
                        signatureType );
                }
                else
                {
                    return null;
                }
            }
        }
        catch ( NumberFormatException e )
        {
            return null;
        }
        catch ( StringIndexOutOfBoundsException e )
        {
            return null;
        }
    }

    public String gavToPath( Gav gav )
    {
        StringBuffer path = new StringBuffer( "/" );

        path.append( gav.getGroupId().replaceAll( "\\.", "/" ) );

        path.append( "/" );

        path.append( gav.getArtifactId() );

        path.append( "/" );

        path.append( gav.getBaseVersion() );

        path.append( "/" );

        path.append( calculateArtifactName( gav ) );

        return path.toString();
    }

    public String calculateArtifactName( Gav gav )
    {
        StringBuffer path = new StringBuffer( gav.getArtifactId() );

        path.append( "-" );

        path.append( gav.getVersion() );

        if ( gav.getClassifier() != null )
        {
            path.append( "-" );

            path.append( gav.getClassifier() );
        }

        if ( gav.getExtension() != null )
        {
            path.append( "." );

            path.append( gav.getExtension() );
        }

        if ( gav.isSignature() )
        {
            path.append( "." );

            path.append( gav.getSignatureType().toString() );
        }

        if ( gav.isHash() )
        {
            path.append( "." );

            path.append( gav.getHashType().toString() );
        }

        return path.toString();
    }

}
