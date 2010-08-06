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

import java.util.Map;

import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * Provides a simple {@link GAVCoordinate} based plugin repository.
 */
public interface NexusPluginRepository
{
    /**
     * @return Unique repository ID
     */
    String getId();

    /**
     * @return Repository priority; natural ordering, smaller before bigger
     */
    int getPriority();

    /**
     * Queries which plugins are available in this repository.
     * 
     * @return Map of available plugins and their metadata
     */
    Map<GAVCoordinate, PluginMetadata> findAvailablePlugins();

    /**
     * Resolves the plugin artifact identified by the given {@link GAVCoordinate}.
     * 
     * @param gav The plugin coordinates
     * @return Resolved plugin artifact
     */
    PluginRepositoryArtifact resolveArtifact( GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException;

    /**
     * Resolves the dependency artifact identified by the given {@link GAVCoordinate}.
     * 
     * @param plugin The plugin artifact
     * @param gav The dependency coordinates
     * @return Resolved dependency artifact
     */
    PluginRepositoryArtifact resolveDependencyArtifact( PluginRepositoryArtifact plugin, GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException;

    /**
     * Resolves the plugin artifact and returns the metadata from {@code plugin.xml}.
     * 
     * @param gav The plugin coordinates
     * @return Plugin metadata
     */
    PluginMetadata getPluginMetadata( GAVCoordinate gav )
        throws NoSuchPluginRepositoryArtifactException;
}
