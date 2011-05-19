/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.nexus.plugins.mavenbridge;

import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelSource;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.nexus.proxy.maven.MavenRepository;

public interface NexusMavenBridge
{
    Model buildModel( ModelSource pom, List<MavenRepository> repositories )
        throws ModelBuildingException;

    DependencyNode collectDependencies( Dependency node, List<MavenRepository> repositories )
        throws DependencyCollectionException, ArtifactResolutionException;

    DependencyNode resolveDependencies( Dependency node, List<MavenRepository> repositories )
        throws DependencyCollectionException, ArtifactResolutionException;

    DependencyNode collectDependencies( Model model, List<MavenRepository> repositories )
        throws DependencyCollectionException, ArtifactResolutionException;

    DependencyNode resolveDependencies( Model model, List<MavenRepository> repositories )
        throws DependencyCollectionException, ArtifactResolutionException;
}
