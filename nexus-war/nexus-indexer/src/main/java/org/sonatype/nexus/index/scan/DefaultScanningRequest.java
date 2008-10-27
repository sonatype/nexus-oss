/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
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
