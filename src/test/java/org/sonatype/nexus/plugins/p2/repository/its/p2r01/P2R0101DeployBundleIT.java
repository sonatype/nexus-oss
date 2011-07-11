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
package org.sonatype.nexus.plugins.p2.repository.its.p2r01;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusP2GeneratorIT;

public class P2R0101DeployBundleIT
    extends AbstractNexusP2GeneratorIT
{

    public P2R0101DeployBundleIT()
    {
        super( "p2r01" );
    }

    /**
     * When an OSGi bundle is deployed pArtifacts && p2Content are created.
     */
    @Test
    public void test()
        throws Exception
    {
        createP2MetadataGeneratorCapability();

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        final File p2Artifacts = downloadP2ArtifactsFor( "org.ops4j.base", "ops4j-base-lang", "1.2.3" );
        assertThat( "p2Artifacts has been downloaded", p2Artifacts, is( notNullValue() ) );
        assertThat( "p2Artifacts exists", p2Artifacts.exists(), is( true ) );
        // TODO compare downloaded file with an expected one

        final File p2Content = downloadP2ContentFor( "org.ops4j.base", "ops4j-base-lang", "1.2.3" );
        assertThat( "p2Content has been downloaded", p2Content, is( notNullValue() ) );
        assertThat( "p2Content exists", p2Content.exists(), is( true ) );
        // TODO compare downloaded file with an expected one
    }

}
