/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.util.Set;

import org.apache.lucene.search.Query;

/**
 * @author Jason van Zyl
 * @author Eugene Kuleshov
 */
public class MinimalIndexNexusIndexerTest
    extends AbstractRepoNexusIndexerTest
{
    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context = nexusIndexer.addIndexingContext( "test-minimal", "test", repo, indexDir, null, null, MIN_CREATORS );

        nexusIndexer.scan( context );
    }

    public void testNEXUS2712()
        throws Exception
    {
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "com.adobe.flexunit" );

        FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( q ) );

        Set<ArtifactInfo> r = response.getResults();

        assertEquals( 1, r.size() );

        ArtifactInfo ai = r.iterator().next();

        assertEquals( "com.adobe.flexunit", ai.groupId );
        assertEquals( "flexunit", ai.artifactId );
        assertEquals( "0.90", ai.version );
        assertEquals( null, ai.classifier );
        assertEquals( "swc", ai.packaging );

        assertEquals( "swc", ai.fextension );
    }
}
