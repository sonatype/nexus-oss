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

import org.junit.Test;
import org.sonatype.nexus.repository.obr.client.ObrHostedRepository;

public class ObrHostedIT
    extends ObrITSupport
{

    public ObrHostedIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void downloadFromHosted()
        throws Exception
    {
        final String hRId = repositoryIdForTest() + "-hosted";

        repositories().create( ObrHostedRepository.class, hRId ).save();

        upload( hRId, FELIX_WEBCONSOLE );
        upload( hRId, OSGI_COMPENDIUM );
        upload( hRId, GERONIMO_SERVLET );
        upload( hRId, PORTLET_API );

        deployUsingObrIntoFelix( hRId );
    }

    @Test
    public void deployToHostedUsingMaven()
        throws Exception
    {
        final String hRId = repositoryIdForTest() + "-hosted";

        repositories().create( ObrHostedRepository.class, hRId ).save();

        deployUsingMaven( "helloworld-hs", hRId );
    }

}
