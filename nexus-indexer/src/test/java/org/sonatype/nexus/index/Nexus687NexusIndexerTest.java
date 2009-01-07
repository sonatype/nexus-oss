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
package org.sonatype.nexus.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Query;

/**
 * @author Juven Xu
 *  
 * http://issues.sonatype.org/browse/NEXUS-687
 */
public class Nexus687NexusIndexerTest
    extends AbstractNexusIndexerTest
{
    protected File repo = new File( getBasedir(), "src/test/nexus-687" );

    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context = nexusIndexer.addIndexingContext(
            "nexus-687",
            "nexus-687",
            repo,
            indexDir,
            null,
            null,
            NexusIndexer.DEFAULT_INDEX );
        nexusIndexer.scan( context );
    }

    public void testSearchFlat()
        throws Exception
    {
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "xstream" );

        FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( q ) );

        Collection<ArtifactInfo> r = response.getResults();

        assertEquals( 1, r.size() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( r );

        assertEquals( 1, list.size() );

        ArtifactInfo ai = list.get( 0 );

        assertEquals( "xstream", ai.groupId );

        assertEquals( "xstream", ai.artifactId );

        assertEquals( "1.2.2", ai.version );

        assertNull( ai.packaging );
    }
}
