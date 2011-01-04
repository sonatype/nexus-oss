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