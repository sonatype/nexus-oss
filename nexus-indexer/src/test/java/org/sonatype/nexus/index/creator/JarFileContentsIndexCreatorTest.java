/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.creator;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexCreator;

/**
 * @author Alin Dreghiciu
 */
public class JarFileContentsIndexCreatorTest
    extends PlexusTestCase
{
    protected IndexCreator indexCreator;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        indexCreator = this.lookup( IndexCreator.class, "jarContent" );
    }

    public void test_nexus_2318_indexJarWithClasses()
        throws Exception
    {
        File artifact = new File(
            getBasedir(),
            "src/test/nexus-2318/aopalliance/aopalliance/1.0/aopalliance-1.0.jar" );

        File pom = new File(
            getBasedir(),
            "src/test/nexus-2318/aopalliance/aopalliance/1.0/aopalliance-1.0.pom" );

        ArtifactInfo artifactInfo = new ArtifactInfo(
            "test",
            "aopalliance",
            "aopalliance",
            "1.0",
            null );

        ArtifactContext artifactContext = new ArtifactContext( pom, artifact, null, artifactInfo, null );

        indexCreator.populateArtifactInfo( artifactContext );

        assertNotNull( "Classes should not be null", artifactContext.getArtifactInfo().classNames );
    }

    public void test_nexus_2318_indexJarWithSources()
        throws Exception
    {
        File artifact = new File(
            getBasedir(),
            "src/test/nexus-2318/aopalliance/aopalliance/1.0/aopalliance-1.0-sources.jar" );

        File pom = new File(
            getBasedir(),
            "src/test/nexus-2318/aopalliance/aopalliance/1.0/aopalliance-1.0.pom" );

        ArtifactInfo artifactInfo = new ArtifactInfo(
            "test",
            "aopalliance",
            "aopalliance",
            "1.0",
            null );

        ArtifactContext artifactContext = new ArtifactContext( pom, artifact, null, artifactInfo, null );

        indexCreator.populateArtifactInfo( artifactContext );

        assertNull( "Classes should be null", artifactContext.getArtifactInfo().classNames );
    }


}
