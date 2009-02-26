/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A scanning request provides various input parameters for repository scan
 *  
 * @author Jason van Zyl 
 */
public class ScanningRequest
{
    private IndexingContext context;

    private ArtifactScanningListener artifactScanningListener;

    public ScanningRequest( IndexingContext context, 
        ArtifactScanningListener artifactScanningListener )
    {
        this.context = context;
        this.artifactScanningListener = artifactScanningListener;
    }

    public IndexingContext getIndexingContext()
    {
        return context;
    }

    public ArtifactScanningListener getArtifactScanningListener() {
        return artifactScanningListener;
    }

}
