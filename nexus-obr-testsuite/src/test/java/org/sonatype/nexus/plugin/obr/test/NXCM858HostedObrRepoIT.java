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
package org.sonatype.nexus.plugin.obr.test;

import org.junit.Test;
import org.sonatype.nexus.plugin.obr.test.ObrITSupport;

public class NXCM858HostedObrRepoIT
    extends ObrITSupport
{

    public NXCM858HostedObrRepoIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void downloadFromHosted()
        throws Exception
    {
        createObrHostedRepository( "obr-hosted" );

        upload( "obr-hosted", FELIX_WEBCONSOLE );
        upload( "obr-hosted", OSGI_COMPENDIUM );
        upload( "obr-hosted", GERONIMO_SERVLET );
        upload( "obr-hosted", PORTLET_API );

        deployUsingObrIntoFelix( "obr-hosted" );
    }

}
