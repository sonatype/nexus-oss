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
package org.sonatype.nexus.plugin.obr.test.nxcm858virtual;

import org.junit.Test;
import org.sonatype.nexus.plugin.obr.test.ObrITSupport;

public class NXCM858ShadowObrRepoIT
    extends ObrITSupport
{

    public NXCM858ShadowObrRepoIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void downloadFromShadow()
        throws Exception
    {
        upload(
            "releases", "org/apache/felix/org.apache.felix.webconsole/3.0.0/org.apache.felix.webconsole-3.0.0.jar"
        );
        upload(
            "releases", "org/apache/felix/org.osgi.compendium/1.4.0/org.osgi.compendium-1.4.0.jar"
        );
        upload(
            "releases", "org/apache/geronimo/specs/geronimo-servlet_3.0_spec/1.0/geronimo-servlet_3.0_spec-1.0.jar"
        );
        upload(
            "releases", "org/apache/portals/portlet-api_2.0_spec/1.0/portlet-api_2.0_spec-1.0.jar"
        );
        createObrShadowRepository( "obr-shadow", "releases" );
        deployUsingObrIntoFelix( "obr-shadow" );
    }

}
