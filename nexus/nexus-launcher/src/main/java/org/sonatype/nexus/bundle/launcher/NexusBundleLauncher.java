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

import org.sonatype.nexus.bundle.NexusBundleConfiguration;

/**
 * A service interface which launches nexus bundle instances.
 *
 * @author plynch
 */
public interface NexusBundleLauncher {

    /**
     * Start a bundle configured as per the specified config, returning a managed representation.
     * <p>
     * The bundle is grouped into the global group.
     *
     * @param config the bundle configuration to use when configuring the bundle before launch.
     * @return
     */
    ManagedNexusBundle start(NexusBundleConfiguration config);

    /**
     * Start a bundle
     * @param config
     * @param groupName
     * @return a {@link ManagedNexusBundle} providing details about the started bundle.
     */
    ManagedNexusBundle start(NexusBundleConfiguration config, String groupName);

    /**
     * Stop the specified bundle.
     * <p>
     * This operation is synchronous.
     *
     * @param managedNexusbundle
     */
    void stop(ManagedNexusBundle managedNexusbundle);

    /**
     * Stop all bundles in the specified group.
     *
     * @param groupName
     */
    void stopAll(String groupName);

    /**
     * Stop all bundles registered with this service.
     */
    void stopAll();

}
