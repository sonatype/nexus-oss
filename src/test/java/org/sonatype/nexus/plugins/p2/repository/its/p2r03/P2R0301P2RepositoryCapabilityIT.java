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
package org.sonatype.nexus.plugins.p2.repository.its.p2r03;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusP2GeneratorIT;

public class P2R0301P2RepositoryCapabilityIT
    extends AbstractNexusP2GeneratorIT
{

    public P2R0301P2RepositoryCapabilityIT()
    {
        super( "p2r03" );
    }

    /**
     * When p2 repository generator capability is created p2 repository is created. When removed p2 repository gets
     * deleted.
     */
    @Test
    public void test()
        throws Exception
    {
        final File artifactsXML = storageP2RepositoryArtifactsXML();
        final File contentXML = storageP2RepositoryContentXML();

        createP2RepositoryAggregatorCapability();

        assertThat( "P2 artifacts.xml does exist", artifactsXML.exists(), is( true ) );
        assertThat( "P2 content.xml does exist", contentXML.exists(), is( true ) );

        removeP2RepositoryAggregatorCapability();

        assertThat( "P2 artifacts.xml does not exist", artifactsXML.exists(), is( false ) );
        assertThat( "P2 content.xml does not exist", contentXML.exists(), is( false ) );
    }

}
