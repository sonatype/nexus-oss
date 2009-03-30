/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.artifact;

/**
 * Describes an artifact version in terms of its components, converts it to/from a string and
 * compares two versions.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public interface ArtifactVersion
    extends Comparable<ArtifactVersion>
{
    int getMajorVersion();

    int getMinorVersion();

    int getIncrementalVersion();

    int getBuildNumber();

    String getQualifier();

    void parseVersion( String version );
}
