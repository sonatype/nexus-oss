/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nexus0977;

import java.net.URL;

import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IntegrationIT;


public class NEXUS0977P2GroupOfGroupsIT
    extends AbstractNexusProxyP2IntegrationIT
{

    @Test
    public void groupOfGroups()
        throws Exception
    {
        downloadFile( new URL( getRepositoryUrl( "g1" ) + "/content.xml" ), "target/downloads/nxcm1995/1/content.xml" );
    }
}
