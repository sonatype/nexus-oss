/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.creator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexCreator;

/**
 * @author juven
 */
public class MinimalArtifactInfoIndexCreatorTest
    extends PlexusTestCase
{
    protected IndexCreator indexCreator;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        indexCreator = this.lookup( IndexCreator.class, "min" );
    }

    public void testMavenPluginInfo()
        throws Exception
    {
        File artifact = new File(
            getBasedir(),
            "src/test/repo-creator/org/apache/maven/plugins/maven-dependency-plugin/2.0/maven-dependency-plugin-2.0.jar" );

        File pom = new File(
            getBasedir(),
            "src/test/repo-creator/org/apache/maven/plugins/maven-dependency-plugin/2.0/maven-dependency-plugin-2.0.pom" );

        ArtifactInfo artifactInfo = new ArtifactInfo(
            "test",
            "org.apache.maven.plugins",
            "maven-dependency-plugin",
            "2.0",
            null );

        ArtifactContext artifactContext = new ArtifactContext( pom, artifact, null, artifactInfo, null );

        indexCreator.populateArtifactInfo( artifactContext );

        assertEquals( "dependency", artifactContext.getArtifactInfo().prefix );

        List<String> goals = new ArrayList<String>( 16 );
        goals.add( "analyze-dep-mgt" );
        goals.add( "analyze" );
        goals.add( "analyze-only" );
        goals.add( "analyze-report" );
        goals.add( "build-classpath" );
        goals.add( "copy-dependencies" );
        goals.add( "copy" );
        goals.add( "unpack" );
        goals.add( "list" );
        goals.add( "purge-local-repository" );
        goals.add( "go-offline" );
        goals.add( "resolve" );
        goals.add( "sources" );
        goals.add( "resolve-plugins" );
        goals.add( "tree" );
        goals.add( "unpack-dependencies" );

        assertEquals( goals, artifactContext.getArtifactInfo().goals );

    }
}
