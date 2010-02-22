/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;

/** http://issues.sonatype.org/browse/NEXUS-3233 */
public class Nexus3233NexusIndexerTest
    extends AbstractNexusIndexerTest
{
    protected File repo = new File( getBasedir(), "src/test/nexus-3233" );

    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context =
            nexusIndexer.addIndexingContext( "nexus-3233", "nexus-3233", repo, indexDir, null, null, FULL_CREATORS );
        nexusIndexer.scan( context );
    }

    public void testIdentifyPomPackagingArtifacts()
        throws Exception
    {
        // POM1
        ArtifactInfo ai = nexusIndexer.identify( ArtifactInfo.SHA1, "741ea3998e6db3ce202d8b88aa53889543f050cc" );

        assertNotNull( ai );

        assertEquals( "cisco.infra.dft", ai.groupId );

        assertEquals( "dma.maven.plugins", ai.artifactId );

        assertEquals( "1.0-SNAPSHOT", ai.version );

        // POM2
        ai = nexusIndexer.identify( ArtifactInfo.SHA1, "efb52d4ef65452b4e575fc2e7709595915775857" );

        assertNotNull( ai );

        assertEquals( "cisco.infra.dft", ai.groupId );

        assertEquals( "parent.pom", ai.artifactId );

        assertEquals( "1.0-SNAPSHOT", ai.version );
    }
}
