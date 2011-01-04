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
