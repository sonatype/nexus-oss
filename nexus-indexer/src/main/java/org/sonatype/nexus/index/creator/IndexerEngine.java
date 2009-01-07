/**
 * Copyright © 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 * Eugene Kuleshov (Sonatype)
 * Tamas Cservenak (Sonatype)
 * Brian Fox (Sonatype)
 * Jason Van Zyl (Sonatype)
 */
package org.sonatype.nexus.index.creator;

import java.io.IOException;

import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.context.IndexingContext;

public interface IndexerEngine
{

    void beginIndexing( IndexingContext context )
        throws IOException;

    void index( IndexingContext context, ArtifactContext ac )
        throws IOException;

    void remove( IndexingContext context, ArtifactContext ac )
        throws IOException;

    void endIndexing( IndexingContext context )
        throws IOException;

    void update( IndexingContext context, ArtifactContext ac )
        throws IOException;

}
