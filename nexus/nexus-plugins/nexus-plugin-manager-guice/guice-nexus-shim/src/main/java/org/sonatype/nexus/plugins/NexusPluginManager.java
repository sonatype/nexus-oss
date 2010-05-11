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
package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

import com.google.inject.ImplementedBy;

/**
 * Manages Nexus plugins, including both system and user types.
 */
@ImplementedBy( DefaultNexusPluginManager.class )
public interface NexusPluginManager
{
    /**
     * Queries which plugins are activated at the moment.
     * 
     * @return Map of activated plugins and their descriptors
     */
    Map<GAVCoordinate, PluginDescriptor> getActivatedPlugins();

    /**
     * Queries which plugins are installed at the moment.
     * 
     * @return Map of installed plugins and their metadata
     */
    Map<GAVCoordinate, PluginMetadata> getInstalledPlugins();

    /**
     * Queries for plugin responses to recent actions/requests.
     * 
     * @return Map of plugin responses to recent requests
     */
    Map<GAVCoordinate, PluginResponse> getPluginResponses();

    /**
     * Attempts to activate all installed plugins.
     * 
     * @return Responses to the activation request
     */
    Collection<PluginManagerResponse> activateInstalledPlugins();

    /**
     * Queries to see if the given plugin is activated or not.
     * 
     * @param gav The plugin coordinates
     * @return {@code true} if the plugin is activated; otherwise {@code false}
     */
    boolean isActivatedPlugin( GAVCoordinate gav );

    /**
     * Attempts to activate the given plugin.
     * 
     * @param gav The plugin coordinates
     * @return Response to the activation request
     */
    PluginManagerResponse activatePlugin( GAVCoordinate gav );

    /**
     * Attempts to de-activate the given plugin.
     * 
     * @param gav The plugin coordinates
     * @return Response to the de-activation request
     */
    PluginManagerResponse deactivatePlugin( GAVCoordinate gav );

    /**
     * Downloads and installs the given Nexus plugin into the writable repository.
     * 
     * @param bundle The plugin resource bundle
     * @return {@code true} if the plugin installed successfully; otherwise {@code false}
     */
    boolean installPluginBundle( URL bundle )
        throws IOException;

    /**
     * Uninstalls the given Nexus plugin from the writable repository.
     * 
     * @param gav The plugin coordinates
     * @return {@code true} if the plugin was successfully deleted; otherwise {@code false}
     */
    boolean uninstallPluginBundle( GAVCoordinate gav )
        throws IOException;
}
