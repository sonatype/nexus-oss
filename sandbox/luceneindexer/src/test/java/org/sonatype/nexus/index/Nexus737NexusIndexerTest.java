/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

/** http://issues.sonatype.org/browse/NEXUS-737 */
public class Nexus737NexusIndexerTest
    extends AbstractNexusIndexerTest
{
    protected File repo = new File( getBasedir(), "src/test/nexus-658" );

    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context = nexusIndexer.addIndexingContext(
            "nexus-658",
            "nexus-658",
            repo,
            indexDir,
            null,
            null,
            DEFAULT_CREATORS );
        nexusIndexer.scan( context );
    }
    
    public void testValidateUINFOs()
        throws Exception
    {
        IndexReader reader = context.getIndexReader();
        
        int foundCount = 0;
        
        for (int i = 0; i < reader.numDocs(); i++) 
        {
            Document document = reader.document( i );
            
            String uinfo = document.get( ArtifactInfo.UINFO );
            
            if ( "org.sonatype.nexus|nexus-webapp|1.0.0-SNAPSHOT|NA".equals( uinfo )
                || "org.sonatype.nexus|nexus-webapp|1.0.0-SNAPSHOT|bundle|zip".equals( uinfo )
                || "org.sonatype.nexus|nexus-webapp|1.0.0-SNAPSHOT|bundle|tar.gz".equals( uinfo ) )
            {
                foundCount++;
            }
        }
        
        assertEquals( foundCount, 3 );
    }
}
