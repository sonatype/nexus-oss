/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.bundle.launcher.util;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Resolves artifacts from Maven repositories.
 */
public interface ArtifactResolver
{

    /**
     * Resolves an artifact using specified artifact coordinates.
     *
     * @param coordinate The artifact coordinates in the format
     *            {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not be {@code null}.
     * @return immutable resolved artifact, never {@code null}.
     */
    ResolvedArtifact resolveArtifact( String coordinate );

    /**
     * Resolves artifacts using the set of specified artifact coordinates.
     *
     * @param coordinates A Set of artifact coordinates in the format
     *            {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not be {@code null}.
     * @return immutable set of resolved artifacts, never {@code null}.
     */
    List<ResolvedArtifact> resolveArtifacts(Collection<String> coordinates);

}
