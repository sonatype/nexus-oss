/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.scan;

import org.sonatype.nexus.index.ArtifactScanningListener;
import org.sonatype.nexus.index.context.IndexingContext;

/** @author Jason van Zyl */
public class DefaultScanningRequest
    implements ScanningRequest
{
    private IndexingContext context;

    private ArtifactScanningListener artifactScanningListener;

    public DefaultScanningRequest( IndexingContext context, 
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
