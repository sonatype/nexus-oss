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

public class M1ArtifactRecognizer {
	/**
	 * Is this item M1 Checksum?
	 */
	public static boolean isChecksum(String path) {
		return path.endsWith(".sha1") || path.endsWith(".md5");
	}

	/**
	 * Is this item M1 POM?
	 */
	public static boolean isPom(String path) {
		return path.endsWith(".pom") || path.endsWith(".pom.sha1") || path.endsWith(".pom.md5");
	}

	/**
	 * Is this item M1 snapshot?
	 */
	public static boolean isSnapshot(String path) {
		return path.indexOf("SNAPSHOT") != -1;
	}

	/**
	 * Is this item M1 metadata?
	 * There is no such!
	 */
	public static boolean isMetadata(String path) {
		return path.endsWith("maven-metadata.xml") || path.endsWith("maven-metadata.xml.sha1")
				|| path.endsWith("maven-metadata.xml.md5");
	}

}
