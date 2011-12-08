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
package org.sonatype.nexus.plugins.mavenbridge;

import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.nexus.proxy.maven.gav.Gav;

/**
 * Collection of static utility methods to bridge the "gap" between Aether and Nexus.
 * 
 * @author cstamas
 */
public class Utils
{
    private Utils()
    {
    }

    /**
     * A shorthand method to create a Dependency from GAV and scope.
     * 
     * @param gav GAV to make Dependency, may not be {@code null}.
     * @param scope the needed scope, or {@code null}
     * @return
     */
    public static Dependency createDependencyFromGav( final Gav gav, final String scope )
    {
        Dependency dependency =
            new Dependency( new DefaultArtifact( gav.getGroupId(), gav.getArtifactId(), gav.getExtension(),
                gav.getVersion() ), scope );

        return dependency;
    }
}
