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

import java.util.Set;

import org.sonatype.nexus.index.ArtifactScanningListener;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;

/** @author Jason van Zyl */
public class DefaultScanningRequest
    implements ScanningRequest
{
    private IndexingContext context;

    private NexusIndexer indexer;
    
    private ArtifactScanningListener artifactScanningListener;

    private final Set<String> infos; 

    public DefaultScanningRequest( IndexingContext context, ArtifactScanningListener artifactScanningListener, NexusIndexer indexer, Set<String> infos )
    {
        this.context = context;
        this.indexer = indexer;
        this.artifactScanningListener = artifactScanningListener;
        this.infos = infos;
    }

    public IndexingContext getIndexingContext()
    {
        return context;
    }

    public NexusIndexer getNexusIndexer()
    {
        return indexer;
    }
    
    public ArtifactScanningListener getArtifactScanningListener() {
        return artifactScanningListener;
    }

    public Set<String> getInfos()
    {
        return infos;
    }
}
