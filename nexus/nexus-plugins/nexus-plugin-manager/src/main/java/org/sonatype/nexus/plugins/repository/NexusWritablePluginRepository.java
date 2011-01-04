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
package org.sonatype.nexus.plugins.repository;

import java.io.IOException;
import java.net.URL;

import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * Writable {@link NexusPluginRepository} that supports installation and deletion of Nexus plugins.
 */
public interface NexusWritablePluginRepository
    extends NexusPluginRepository
{
    /**
     * Downloads and installs the given Nexus plugin into the writable repository.
     * 
     * @param bundle The plugin resource bundle
     * @return {@code true} if the plugin installed successfully; otherwise {@code false}
     */
    boolean installPluginBundle( URL bundle )
        throws IOException;

    /**
     * Deletes the given Nexus plugin from the writable repository.
     * 
     * @param gav The plugin coordinates
     * @return {@code true} if the plugin was successfully deleted; otherwise {@code false}
     */
    boolean deletePluginBundle( GAVCoordinate gav )
        throws IOException;
}
