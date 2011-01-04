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
package org.sonatype.nexus.integrationtests.nexus3936;

import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.test.utils.NexusIllegalStateException;
import org.sonatype.nexus.test.utils.NexusStatusUtil;
import org.sonatype.security.rest.model.ClientPermission;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Security is already disabled for this Test, we just need to make sure the Status resource returns ALL/15, for all the
 * permission strings.
 */
@Test( groups = { "security", "anonymous", "status" } )
public class Nexus3936DisableSecurityStatusIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void testSecurityDisabledStatus()
        throws NexusIllegalStateException
    {

        NexusStatusUtil statusUtil = new NexusStatusUtil();
        StatusResource statusResource = statusUtil.getNexusStatus().getData();

        List<ClientPermission> permisisons = statusResource.getClientPermissions().getPermissions();

        Assert.assertTrue( permisisons.size() > 0, "Permissions are empty, expected a whole bunch, not zero." );
        for ( ClientPermission clientPermission : permisisons )
        {
            Assert.assertEquals( clientPermission.getValue(), 15, "Permission '"+ clientPermission.getId() +"' should have had a value of '15', the value was" + clientPermission.getValue() );
        }
        // that is it, just checking the values, when security is disabled, access is WIDE open.
    }
}
