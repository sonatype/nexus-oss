/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.maven.gav;

import junit.framework.TestCase;

public class MavenArtifactRecognizerTest
    extends TestCase
{

    public void testIsPom()
    {
        assertEquals( true, M2ArtifactRecognizer.isPom( "aaa.pom" ) );
        assertEquals( true, M2ArtifactRecognizer.isPom( "zxc-1-2-3.pom" ) );
        assertEquals( false, M2ArtifactRecognizer.isPom( "aaa.jar" ) );
        assertEquals( false, M2ArtifactRecognizer.isPom( "aaa.pom-a" ) );
    }

    public void testIsSnapshot1()
    {
        // NEXUS-3148
        assertEquals( true, M2ArtifactRecognizer.isSnapshot( "/org/somewhere/aid/1.0SNAPSHOT/aid-1.0SNAPSHOT.jar" ) );

        assertEquals( true, M2ArtifactRecognizer.isSnapshot( "/org/somewhere/aid/1.0-SNAPSHOT/aid-1.0-SNAPSHOT.jar" ) );
        assertEquals( true, M2ArtifactRecognizer.isSnapshot( "/org/somewhere/aid/1.0-SNAPSHOT/aid-1.0-SNAPSHOT.pom" ) );
        assertEquals( true, M2ArtifactRecognizer.isSnapshot( "/org/somewhere/aid/1.0-SNAPSHOT/aid-1.2.3-.pom" ) );
        assertEquals( false, M2ArtifactRecognizer.isSnapshot( "/org/somewhere/aid/1.0/xsd-SNAPsHOT.jar" ) );
        assertEquals( false, M2ArtifactRecognizer.isSnapshot( "/org/somewhere/aid/1.0/xsd-SNAPHOT.pom" ) );
        assertEquals( false, M2ArtifactRecognizer.isSnapshot( "/org/somewhere/aid/1.0/a/b/c/xsd-1.2.3NAPSHOT.pom" ) );
        assertEquals( false, M2ArtifactRecognizer.isSnapshot( "/javax/mail/mail/1.4/mail-1.4.jar" ) );
    }

    public void testIsSnapshot2()
    {
        assertEquals(
            true,
            M2ArtifactRecognizer.isSnapshot( "/org/somewhere/appassembler-maven-plugin/1.0-SNAPSHOT/appassembler-maven-plugin-1.0-20060714.142547-1.pom" ) );
        assertEquals(
            false,
            M2ArtifactRecognizer.isSnapshot( "/org/somewhere/appassembler-maven-plugin/1.0/appassembler-maven-plugin-1.0-20060714.142547-1.pom" ) );
    }

    public void testIsMetadata()
    {
        assertEquals( true, M2ArtifactRecognizer.isMetadata( "maven-metadata.xml" ) );
        assertEquals( false, M2ArtifactRecognizer.isMetadata( "aven-metadata.xml" ) );
        assertEquals( false, M2ArtifactRecognizer.isMetadata( "/javax/mail/mail/1.4/mail-1.4.jar" ) );
    }

}
