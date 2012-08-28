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
package org.sonatype.nexus.plugin.obr.test.nxcm858proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.plugin.obr.test.ObrITSupport;

public class NXCM858ProxyObrRepoIT
    extends ObrITSupport
{

    public NXCM858ProxyObrRepoIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void downloadFromProxy()
        throws Exception
    {
        createObrHostedRepository( "obr-hosted" );

        upload( "obr-hosted", FELIX_WEBCONSOLE );
        upload( "obr-hosted", OSGI_COMPENDIUM );
        upload( "obr-hosted", GERONIMO_SERVLET );
        upload( "obr-hosted", PORTLET_API );

        createObrProxyRepository(
            "obr-proxy", nexus().getUrl().toExternalForm() + "content/repositories/obr-hosted/.meta/obr.xml"
        );

        deployUsingObrIntoFelix( "obr-proxy" );

        verifyExistenceInStorage( FELIX_WEBCONSOLE );
        verifyExistenceInStorage( OSGI_COMPENDIUM );
        verifyExistenceInStorage( GERONIMO_SERVLET );
        verifyExistenceInStorage( PORTLET_API );
    }

    private void verifyExistenceInStorage( final String filename )
    {
        assertThat( new File( nexus().getWorkDirectory(), "storage/obr-proxy/" + filename ), exists() );
    }

}
