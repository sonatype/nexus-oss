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

import junit.framework.TestCase;

public class MavenArtifactRecognizerTest extends TestCase {

	public void testIsPom() {
		assertEquals(true, M2ArtifactRecognizer.isPom("aaa.pom"));
		assertEquals(true, M2ArtifactRecognizer.isPom("zxc-1-2-3.pom"));
		assertEquals(false, M2ArtifactRecognizer.isPom("aaa.jar"));
		assertEquals(false, M2ArtifactRecognizer.isPom("aaa.pom-a"));
	}

	public void testIsSnapshot1() {
		assertEquals(true, M2ArtifactRecognizer.isSnapshot("/org/somewhere/aid/1.0-SNAPSHOT/xsd-SNAPSHOT.jar"));
		assertEquals(true, M2ArtifactRecognizer.isSnapshot("/org/somewhere/aid/1.0-SNAPSHOT/xsd-SNAPSHOT.pom"));
		assertEquals(true, M2ArtifactRecognizer.isSnapshot("/org/somewhere/aid/1.0-SNAPSHOT/a/b/c/xsd-1.2.3-.pom"));
		assertEquals(false, M2ArtifactRecognizer.isSnapshot("/org/somewhere/aid/1.0/xsd-SNAPsHOT.jar"));
		assertEquals(false, M2ArtifactRecognizer.isSnapshot("/org/somewhere/aid/1.0/xsd-SNAPHOT.pom"));
		assertEquals(false, M2ArtifactRecognizer.isSnapshot("/org/somewhere/aid/1.0/a/b/c/xsd-1.2.3NAPSHOT.pom"));
        assertEquals(false, M2ArtifactRecognizer.isSnapshot("/javax/mail/mail/1.4/mail-1.4.jar"));
	}

	public void testIsSnapshot2() {
		assertEquals(true, M2ArtifactRecognizer
				.isSnapshot("/org/somewhere/aid/1.0-SNAPSHOT/appassembler-maven-plugin-1.0-20060714.142547-1.pom"));
		assertEquals(false, M2ArtifactRecognizer
				.isSnapshot("/org/somewhere/aid/1.0/appassembler-maven-plugin-1.0-20060714.142547-1.pom"));
	}

	public void testIsMetadata() {
		assertEquals(true, M2ArtifactRecognizer.isMetadata("maven-metadata.xml"));
		assertEquals(false, M2ArtifactRecognizer.isMetadata("aven-metadata.xml"));
        assertEquals(false, M2ArtifactRecognizer.isMetadata("/javax/mail/mail/1.4/mail-1.4.jar"));
	}

}
