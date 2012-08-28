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
package org.sonatype.nexus.plugin.obr.test.nexus977;

import org.junit.Test;
import org.sonatype.nexus.plugin.obr.test.ObrITSupport;

public class Nexus977ObrGroupOfGroupsIT
    extends ObrITSupport
{

    public Nexus977ObrGroupOfGroupsIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void obrGroupOfGroups()
        throws Exception
    {
        createObrHostedRepository( "obr-hosted-1" );
        upload( "obr-hosted-1", FELIX_WEBCONSOLE );

        createObrHostedRepository( "obr-hosted-2" );
        upload( "obr-hosted-2", OSGI_COMPENDIUM );

        createObrHostedRepository( "obr-hosted-3" );
        upload( "obr-hosted-3", GERONIMO_SERVLET );

        createObrHostedRepository( "obr-hosted-4" );
        upload( "obr-hosted-4", PORTLET_API );

        createObrGroup( "obr-group-4", "obr-hosted-2", "obr-hosted-4" );
        createObrGroup( "obr-group-3", "obr-group-4", "obr-hosted-3" );
        createObrGroup( "obr-group-2", "obr-group-3", "obr-group-4" );
        createObrGroup( "obr-group-1", "obr-hosted-1", "obr-group-2" );

        deployUsingObrIntoFelix( "obr-group-1" );
    }

}
