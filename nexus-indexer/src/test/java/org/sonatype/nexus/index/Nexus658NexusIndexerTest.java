/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Query;

/** http://issues.sonatype.org/browse/NEXUS-13 */
public class Nexus658NexusIndexerTest
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

    public void testSearchFlat()
        throws Exception
    {
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "org.sonatype.nexus" );
        FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( q ) );
        Collection<ArtifactInfo> r = response.getResults(); 
        assertEquals( r.toString(), 4, r.size() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( r );

        // g a v p c #1
        ArtifactInfo ai1 = list.get( 0 );
        assertEquals( "org.sonatype.nexus", ai1.groupId );
        assertEquals( "nexus-webapp", ai1.artifactId );
        assertEquals( "1.0.0-SNAPSHOT", ai1.version );
        assertEquals( "jar", ai1.packaging );
        assertEquals( null, ai1.classifier );
        assertEquals( ArtifactAvailablility.PRESENT, ai1.sourcesExists );
        assertEquals( "nexus-658", ai1.repository );

        // g a v p c #2
        ArtifactInfo ai2 = list.get( 1 );
        assertEquals( "org.sonatype.nexus", ai2.groupId );
        assertEquals( "nexus-webapp", ai2.artifactId );
        assertEquals( "1.0.0-SNAPSHOT", ai2.version );
        assertEquals( "tar.gz", ai2.packaging );
        assertEquals( "bundle", ai2.classifier );
        assertEquals( ArtifactAvailablility.NOT_AVAILABLE, ai2.sourcesExists );
        assertEquals( "nexus-658", ai2.repository );

        // g a v p c #3
        ArtifactInfo ai3 = list.get( 2 );
        assertEquals( "org.sonatype.nexus", ai3.groupId );
        assertEquals( "nexus-webapp", ai3.artifactId );
        assertEquals( "1.0.0-SNAPSHOT", ai3.version );
        assertEquals( "zip", ai3.packaging );
        assertEquals( "bundle", ai3.classifier );
        assertEquals( ArtifactAvailablility.NOT_AVAILABLE, ai3.sourcesExists );
        assertEquals( "nexus-658", ai3.repository );
        
        // g a v p c #3
        ArtifactInfo ai4 = list.get( 3 );
        assertEquals( "org.sonatype.nexus", ai4.groupId );
        assertEquals( "nexus-webapp", ai4.artifactId );
        assertEquals( "1.0.0-SNAPSHOT", ai4.version );
        assertEquals( "jar", ai4.packaging );
        assertEquals( "sources", ai4.classifier );
        assertEquals( ArtifactAvailablility.NOT_AVAILABLE, ai4.sourcesExists );
        assertEquals( "nexus-658", ai4.repository );
    }

}
