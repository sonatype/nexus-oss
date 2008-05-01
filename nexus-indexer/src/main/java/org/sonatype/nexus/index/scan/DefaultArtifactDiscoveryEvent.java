/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.index.scan;

import java.io.File;

import org.sonatype.nexus.index.ArtifactScanningListener;
import org.sonatype.nexus.index.context.IndexingContext;

/** @author Jason van Zyl */
public class DefaultArtifactDiscoveryEvent
    implements ArtifactDiscoveryEvent
{
    private IndexingContext indexingContext;

    private File file;
    
    private ArtifactScanningListener artifactScanningListener;

    public DefaultArtifactDiscoveryEvent( IndexingContext context, File file, ArtifactScanningListener artifactScanningListener )
    {
        this.indexingContext = context;
        this.file = file;
        this.artifactScanningListener = artifactScanningListener;
    }

    public IndexingContext getIndexingContext()
    {
        return indexingContext;
    }

    public File getFile()
    {
        return file;
    }
    
    public ArtifactScanningListener getArtifactScanningListener() {
        return artifactScanningListener; 
    }

}
