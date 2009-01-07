/**
 * Copyright © 2007-2008 Sonatype, Inc. All rights reserved.
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
