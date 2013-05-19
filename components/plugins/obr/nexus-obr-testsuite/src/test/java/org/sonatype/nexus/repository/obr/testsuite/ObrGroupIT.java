/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.obr.testsuite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

import org.junit.Test;
import org.sonatype.nexus.repository.obr.client.ObrGroupRepository;
import org.sonatype.nexus.repository.obr.client.ObrHostedRepository;

public class ObrGroupIT
    extends ObrITSupport
{

    public ObrGroupIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void obrGroupOfGroups()
        throws Exception
    {
        final String h1RId = repositoryIdForTest() + "-hosted-1";
        final String h2RId = repositoryIdForTest() + "-hosted-2";
        final String h3RId = repositoryIdForTest() + "-hosted-3";
        final String h4RId = repositoryIdForTest() + "-hosted-4";

        final String g1RId = repositoryIdForTest() + "-group-1";
        final String g2RId = repositoryIdForTest() + "-group-2";
        final String g3RId = repositoryIdForTest() + "-group-3";
        final String g4RId = repositoryIdForTest() + "-group-4";

        repositories().create( ObrHostedRepository.class, h1RId ).save();
        upload( h1RId, FELIX_WEBCONSOLE );

        repositories().create( ObrHostedRepository.class, h2RId ).save();
        upload( h2RId, OSGI_COMPENDIUM );

        repositories().create( ObrHostedRepository.class, h3RId ).save();
        upload( h3RId, GERONIMO_SERVLET );

        repositories().create( ObrHostedRepository.class, h4RId ).save();
        upload( h4RId, PORTLET_API );

        repositories().create( ObrGroupRepository.class, g4RId ).ofRepositories( h2RId, h4RId ).save();
        repositories().create( ObrGroupRepository.class, g3RId ).ofRepositories( g4RId, h3RId ).save();
        repositories().create( ObrGroupRepository.class, g2RId ).ofRepositories( g3RId, g4RId ).save();
        repositories().create( ObrGroupRepository.class, g1RId ).ofRepositories( h1RId, g2RId ).save();

        deployUsingObrIntoFelix( g1RId );
    }

    /**
     * NXCM-2795:
     * Verifies that bundles are found via an OBR group when groups contains multiple OBR hosted repositories.
     *
     * @throws Exception unexpected
     */
    @Test
    public void fourHostedInAGroup()
        throws Exception
    {
        final String h1RId = repositoryIdForTest() + "-hosted-1";
        final String h2RId = repositoryIdForTest() + "-hosted-2";
        final String h3RId = repositoryIdForTest() + "-hosted-3";
        final String h4RId = repositoryIdForTest() + "-hosted-4";

        final String gRId = repositoryIdForTest() + "-group";

        repositories().create( ObrHostedRepository.class, h1RId ).save();
        upload( h1RId, FELIX_WEBCONSOLE );

        repositories().create( ObrHostedRepository.class, h2RId ).save();
        upload( h2RId, OSGI_COMPENDIUM );

        repositories().create( ObrHostedRepository.class, h3RId ).save();
        upload( h3RId, GERONIMO_SERVLET );

        repositories().create( ObrHostedRepository.class, h4RId ).save();
        upload( h4RId, PORTLET_API );

        repositories().create( ObrGroupRepository.class, gRId ).ofRepositories( h1RId, h2RId, h3RId, h4RId ).save();

        // verify that group level merged OBR is valid
        deployUsingObrIntoFelix( gRId );

        // verify that each deployed file can be downloaded
        assertThat( download( gRId, FELIX_WEBCONSOLE ), exists() );
        assertThat( download( gRId, OSGI_COMPENDIUM ), exists() );
        assertThat( download( gRId, GERONIMO_SERVLET ), exists() );
        assertThat( download( gRId, PORTLET_API ), exists() );
    }

}
