/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
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
