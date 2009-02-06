/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.creator;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;

/**
 * An index creator is responsible for storing and reading data to and from Lucene index.
 * 
 * @author Jason van Zyl
 * @see MinimalArtifactInfoIndexCreator
 * @see JarFileContentsIndexCreator
 */
public interface IndexCreator
{
    /**
     * Populate an <code>ArtifactContext</code> with information about corresponding artifact.
     */
    void populateArtifactInfo( ArtifactContext artifactContext )
        throws IOException;

    /**
     * Update Lucene <code>Document</code> from a given <code>ArtifactInfo</code>.
     */
    void updateDocument( ArtifactInfo artifactInfo, Document document );

    /**
     * Update an <code>ArtifactInfo</code> from given Lucene <code>Document</code>.
     * 
     * @return true is artifact info has been updated
     */
    boolean updateArtifactInfo( Document document, ArtifactInfo artifactInfo );

}
