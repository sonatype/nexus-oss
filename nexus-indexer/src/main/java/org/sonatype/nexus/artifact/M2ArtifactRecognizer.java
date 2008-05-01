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

public class M2ArtifactRecognizer {
	/**
	 * Is this item M2 Checksum?
	 */
	public static boolean isChecksum(String path) {
		return path.endsWith(".sha1") || path.endsWith(".md5");
	}

	/**
	 * Is this item M2 POM?
	 */
	public static boolean isPom(String path) {
		return path.endsWith(".pom") || path.endsWith(".pom.sha1") || path.endsWith(".pom.md5");
	}

	/**
	 * Is this item M2 snapshot?
	 */
	public static boolean isSnapshot(String path) {
		return path.indexOf("SNAPSHOT") != -1;
	}

	/**
	 * Is this item M2 metadata?
	 */
	public static boolean isMetadata(String path) {
		return path.endsWith("maven-metadata.xml") || path.endsWith("maven-metadata.xml.sha1")
				|| path.endsWith("maven-metadata.xml.md5");
	}

}
