/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.plugins.p2.repository.its.p2r02;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusP2GeneratorIT;
import org.testng.annotations.Test;

public class P2R0201DeployLegacyJarIT
    extends AbstractNexusP2GeneratorIT
{

    public P2R0201DeployLegacyJarIT()
    {
        super( "p2r02" );
    }

    /**
     * When deploying a legacy jar (non OSGi bundle), p2Artifacts & p2Content are not created.
     */
    @Test
    public void test()
        throws Exception
    {
        createP2MetadataGeneratorCapability();

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        final File p2Artifacts = storageP2ArtifactsFor( "commons-logging", "commons-logging", "1.1.1" );
        assertThat( "p2Artifacts does not exist", p2Artifacts.exists(), is( false ) );

        final File p2Content = storageP2ContentFor( "commons-logging", "commons-logging", "1.1.1" );
        assertThat( "p2Content does not exist", p2Content.exists(), is( false ) );
    }

}
