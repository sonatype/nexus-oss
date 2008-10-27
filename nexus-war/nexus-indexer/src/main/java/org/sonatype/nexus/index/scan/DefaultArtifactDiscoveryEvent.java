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
