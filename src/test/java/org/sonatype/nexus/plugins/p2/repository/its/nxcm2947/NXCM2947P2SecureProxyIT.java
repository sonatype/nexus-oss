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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm2947;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.testng.annotations.Test;

public class NXCM2947P2SecureProxyIT
    extends AbstractNexusProxyP2IT
{

    public NXCM2947P2SecureProxyIT()
    {
        super( "nxcm2947" );
    }

    @Override
    protected ServletServer lookupProxyServer()
        throws ComponentLookupException
    {
        return (ServletServer) lookup( ServletServer.ROLE, "secure" );
    }

    @Test
    public void test()
        throws Exception
    {
        installAndVerifyP2Feature();
    }

}
