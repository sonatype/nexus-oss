/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nexus5012;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.*;

import java.io.File;

import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusP2GeneratorIT;
import org.testng.annotations.Test;

public class Nexus5012IT
    extends AbstractNexusP2GeneratorIT
{

    public Nexus5012IT()
    {
        super( "p2r03" );
    }

    /**
     * When Nexus is stopped the p2 capability should still exist
     */
    @Test
    public void test()
        throws Exception
    {
        final File artifactsXML = storageP2RepositoryArtifactsXML();
        final File contentXML = storageP2RepositoryContentXML();

        createP2RepositoryAggregatorCapability();

        assertThat( "P2 artifacts.xml does exist", artifactsXML, exists() );
        assertThat( "P2 content.xml does exist", contentXML, exists() );

        /*
         * Calling stopNexus() will mean that there is an error during 
         * testing when AbstractNexusIntegrationTest.oncePerClassTearDown() 
         * is called, but there is no infrastructure available to avoid this.
         */
        stopNexus();

        assertThat( "P2 artifacts.xml does exist", artifactsXML, exists() );
        assertThat( "P2 content.xml does exist", contentXML, exists() );
    }

    @Override
    protected String getTestId()
    {
        return testRepositoryId;
    }

}
