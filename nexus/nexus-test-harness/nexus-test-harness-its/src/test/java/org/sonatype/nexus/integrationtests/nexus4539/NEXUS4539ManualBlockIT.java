/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus4539;

import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * while a proxy is auto-blocked move the proxy repository state to manually blocked. make sure it is still manually
 * blocked after the auto-block thread wakes to recheck the status
 */
public class NEXUS4539ManualBlockIT
    extends AutoBlockITSupport
{

    @BeforeMethod
    public void setTimeout()
    {
        super.sleepTime = 100;
    }

    @Test
    public void manualBlock()
        throws Exception
    {
        // initial status, timing out
        waitFor( RemoteStatus.UNAVAILABLE, ProxyMode.BLOCKED_AUTO );

        // set manual block
        repoUtil.setBlockProxy( REPO, true );
        assertStatus( repoUtil.getStatus( REPO ), RemoteStatus.UNAVAILABLE, ProxyMode.BLOCKED_MANUAL );

        // server back to normal
        super.sleepTime = -1;

        // nexus shall not unblock
        Thread.sleep( 15 * 1000 );
        assertStatus( repoUtil.getStatus( REPO ), RemoteStatus.UNAVAILABLE, ProxyMode.BLOCKED_MANUAL );

        // must still be manual blocked
        shakeNexus();
        assertStatus( repoUtil.getStatus( REPO ), RemoteStatus.UNAVAILABLE, ProxyMode.BLOCKED_MANUAL );
    }


}
