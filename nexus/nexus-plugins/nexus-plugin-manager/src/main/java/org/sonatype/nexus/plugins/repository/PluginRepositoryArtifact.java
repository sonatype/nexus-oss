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

import java.io.File;

import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * Represents a resolved artifact from a {@link NexusPluginRepository}.
 */
public final class PluginRepositoryArtifact
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private GAVCoordinate gav;

    private File file;

    private NexusPluginRepository repo;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PluginRepositoryArtifact()
    {
        // legacy constructor
    }

    PluginRepositoryArtifact( final GAVCoordinate gav, final File file, final NexusPluginRepository repo )
    {
        this.gav = gav;
        this.file = file;
        this.repo = repo;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void setCoordinate( final GAVCoordinate gav )
    {
        this.gav = gav;
    }

    public GAVCoordinate getCoordinate()
    {
        return gav;
    }

    public void setFile( final File file )
    {
        this.file = file;
    }

    public File getFile()
    {
        return file;
    }

    public void setNexusPluginRepository( final NexusPluginRepository repo )
    {
        this.repo = repo;
    }

    public NexusPluginRepository getNexusPluginRepository()
    {
        return repo;
    }

    public PluginMetadata getPluginMetadata()
        throws NoSuchPluginRepositoryArtifactException
    {
        return repo.getPluginMetadata( gav );
    }
}