/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 * Eugene Kuleshov (Sonatype)
 * Tamas Cservenak (Sonatype)
 * Brian Fox (Sonatype)
 * Jason Van Zyl (Sonatype)
 */
package org.sonatype.nexus.artifact;

/**
 * A simple class for some very basic "detection" of the kind of path.
 */
public class M2ArtifactRecognizer
{
    /**
     * Is this item M2 Checksum?
     */
    public static boolean isChecksum( String path )
    {
        return path.endsWith( ".sha1" ) || path.endsWith( ".md5" );
    }

    /**
     * Is this item M2 POM?
     */
    public static boolean isPom( String path )
    {
        return path.endsWith( ".pom" ) || path.endsWith( ".pom.sha1" ) || path.endsWith( ".pom.md5" );
    }

    /**
     * Is this item M2 snapshot?
     */
    public static boolean isSnapshot( String path )
    {
        return path.indexOf( "SNAPSHOT" ) != -1;
    }

    /**
     * Is this item M2 metadata?
     */
    public static boolean isMetadata( String path )
    {
        return path.endsWith( "maven-metadata.xml" ) || path.endsWith( "maven-metadata.xml.sha1" )
            || path.endsWith( "maven-metadata.xml.md5" );
    }

    public static boolean isSignature( String path )
    {
        return path.endsWith( ".asc" );
    }

}
