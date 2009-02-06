/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonatype.nexus.index.creator;

import org.apache.lucene.document.Document;
import org.sonatype.nexus.index.ArtifactInfo;

/**
 * A legacy document updater is used to produce legacy Lucene index documents. 
 * 
 * @author Eugene Kuleshov
 */
public interface LegacyDocumentUpdater
{

    /**
     * Update a legacy Lucene <code>Document</code> from the <code>ArtifactInfo</code>. 
     */
    void updateLegacyDocument( ArtifactInfo ai, Document doc );
    
}

