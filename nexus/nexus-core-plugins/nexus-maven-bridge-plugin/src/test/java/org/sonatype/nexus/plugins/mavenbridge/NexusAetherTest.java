/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.mavenbridge;

import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.nexus.AbstractMavenRepoContentTests;

public class NexusAetherTest
    extends AbstractMavenRepoContentTests
{
    protected NexusAether nexusAether;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusAether = lookup( NexusAether.class );

        // repositoryRegistry = lookup( RepositoryRegistry.class );
    }

    @Test
    public void testDependency()
    {
        Gav gav = new Gav( "org.apache.maven", "apache-maven", "3.0-beta-1" );

        Dependency dep = Utils.createDependencyFromGav( gav, "compile" );

        Assert.assertEquals( dep.getArtifact().getGroupId(), gav.getGroupId() );
        Assert.assertEquals( dep.getArtifact().getArtifactId(), gav.getArtifactId() );
        Assert.assertEquals( dep.getArtifact().getVersion(), gav.getVersion() );
        Assert.assertEquals( "compile", dep.getScope() );
    }
}
