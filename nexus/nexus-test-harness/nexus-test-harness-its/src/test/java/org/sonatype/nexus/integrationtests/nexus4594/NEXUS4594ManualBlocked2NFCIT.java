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
package org.sonatype.nexus.integrationtests.nexus4594;

import org.testng.annotations.Test;

public class NEXUS4594ManualBlocked2NFCIT
    extends Blocked2NFCITSupport
{

    @Test
    public void whileNexusIsManualBlockedItDoesNotAddPathsToNFC()
        throws Exception
    {
        // manual block Nexus
        manualBlockNexus();

        // make a request to an arbitrary artifact and verify that Nexus did not went remote (repository is blocked)
        // Nexus should not add it to NFC, but that will see later while re-requesting the artifact with Nexus unblocked
        downloadArtifact( "foo", "bar", "5.0" );
        verifyNexusDidNotWentRemote();

        // unblock Nexus so we can request again the arbitrary artifact
        manualUnblockNexus();

        // make a request and check that Nexus went remote (so is not in NFC)
        downloadArtifact( "foo", "bar", "5.0" );
        verifyNexusWentRemote();
    }

}
