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
package org.sonatype.nexus.bundle.launcher;

import org.sonatype.nexus.bundle.launcher.internal.PortType;
import org.sonatype.nexus.bundle.launcher.support.resolver.ResolvedArtifact;

import java.io.File;

/**
 * A Nexus bundle that is being managed by a {@link NexusBundleLauncher}
 * <p/>
 * We only try to expose the minimum details needed to interact with a bundle as a client.
 *
 * @author plynch
 */
public interface NexusBundle {

    /**
     * @return The unique id among all managed Nexus bundles.
     */
    String getId();

    /**
     * @return the resolved artifact for the original bundle file.
     */
    ResolvedArtifact getArtifact();

    /**
     * @param portType the portType to get
     * @return the port value for the specified port type. -1 if not port value assigned for the type.
     */
    int getPort(PortType portType);

    /**
     * @return the http port assigned to this bundle
     */
    int getHttpPort();

    /**
     * @return The host this bundle is configured to run on, usually 'localhost'.
     */
    String getHost();

    /**
     * The context path at which the bundle is configured.
     * <p/>
     * In keeping with {@link javax.servlet.ServletContext#getContextPath()}, if the bundle is configured at the root context, then this value will be "".
     *
     * @return The context path at which the bundle is configured - usually /nexus. It is guaranteed to not be null and either equal to "" or a string starting with forward slash {@code /}
     */
    String getContextPath();

    /**
     * @return the work directory configured for this bundle
     */
    File getNexusWorkDirectory();

    /**
     * @return the runtime directory configured for this bundle
     */
    File getNexusRuntimeDirectory();

}
