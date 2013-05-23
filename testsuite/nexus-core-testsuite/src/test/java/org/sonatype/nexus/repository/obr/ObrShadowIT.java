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
package org.sonatype.nexus.repository.obr;

import org.junit.Test;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.repository.obr.client.ObrVirtualRepository;

public class ObrShadowIT
    extends ObrITSupport
{

    public ObrShadowIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void downloadFromShadow()
        throws Exception
    {
        final String mavenRId = repositoryIdForTest() + "-maven";
        final String sRId = repositoryIdForTest() + "-shadow";

        repositories().create( MavenHostedRepository.class, mavenRId ).save();

        upload( mavenRId, FELIX_WEBCONSOLE );
        upload( mavenRId, OSGI_COMPENDIUM );
        upload( mavenRId, GERONIMO_SERVLET );
        upload( mavenRId, PORTLET_API );

        repositories().create( ObrVirtualRepository.class, sRId ).ofRepository( mavenRId ).save();
        deployUsingObrIntoFelix( sRId );
    }

}
