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

import org.testng.annotations.Test;

/**
 * Make sure normal case works, that is, a proxy whose remote is timing out blocks, and then unblocks when the remote is
 * available. It does take around 2 minutes to run.
 */
public class NEXUS4539AutoBlockIT
    extends AutoBlockITSupport
{

    @Test
    public void autoBlock()
        throws Exception
    {
        // initial status, no timing out
        autoUnblockNexus();

        // block Nexus
        autoBlockNexus();

        // it must unblock auto magically
        autoUnblockNexus();

        // let's see if it will block again
        autoBlockNexus();
        // let is sit on ice for 30s
        Thread.sleep( 30 * 1000 );

        // it must auto unblock again
        autoUnblockNexus();
    }

}
