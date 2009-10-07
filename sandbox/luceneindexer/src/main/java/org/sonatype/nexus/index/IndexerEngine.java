/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.IOException;

import org.sonatype.nexus.index.context.IndexingContext;

/**
 * An indexer engine used to index, update and remote artifacts to the indexing context.
 */
public interface IndexerEngine
{
    /**
     * Add new artifact to the index
     */
    void index( IndexingContext context, ArtifactContext ac )
        throws IOException;

    /**
     * Replace data for a previously indexed artifact
     */
    void update( IndexingContext context, ArtifactContext ac )
        throws IOException;

    /**
     * Remove artifact to the index
     */
    void remove( IndexingContext context, ArtifactContext ac )
        throws IOException;

}
