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

import org.apache.lucene.document.Document;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.ArtifactIndexingContext;
import org.sonatype.nexus.index.context.IndexingContext;

/** @author Jason van Zyl */
public interface IndexCreator
{
    void populateArtifactInfo( ArtifactIndexingContext indexingContext ) 
        throws IOException;
  
    void updateDocument( ArtifactIndexingContext context, Document doc );
    
    /**
     * @return true is artifact info has been updated
     */
    boolean updateArtifactInfo( IndexingContext ctx, Document d, ArtifactInfo artifactInfo );

}
