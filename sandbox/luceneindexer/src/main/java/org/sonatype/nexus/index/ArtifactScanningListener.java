/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A listener notified during repository scan process to track progress, collect results, etc.
 * 
 * @author Jason van Zyl
 */
public interface ArtifactScanningListener
    extends ArtifactDiscoveryListener
{
    void scanningStarted( IndexingContext ctx );

    void scanningFinished( IndexingContext ctx, ScanningResult result );
    
    void artifactError( ArtifactContext ac, Exception e );

}
