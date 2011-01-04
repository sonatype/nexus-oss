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
package org.sonatype.nexus.integrationtests.nexus2996;

import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionContaining;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2996DeleteRepoTargetIT
    extends AbstractNexusIntegrationTest
{

    private PrivilegesMessageUtil privUtil =
        new PrivilegesMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );

    private static final String TARGET_ID = "1c1fd83a2fd9";

    private static final String READ_PRIV_ID = "1c26537599f6";

    private static final String CREATE_PRIV_ID = "1c2652734258";

    private static final String UPDATE_PRIV_ID = "1c2653b9a119";

    private static final String DELETE_PRIV_ID = "1c2653f5a3e2";

    @Test
    public void deleteRepoTarget()
        throws Exception
    {
        RepositoryTargetResource target = TargetMessageUtil.get( TARGET_ID );
        MatcherAssert.assertThat( target.getPatterns(), IsCollectionContaining.hasItem( ".*" ) );

        privUtil.assertExists( READ_PRIV_ID, CREATE_PRIV_ID, UPDATE_PRIV_ID, DELETE_PRIV_ID );

        Assert.assertTrue( TargetMessageUtil.delete( TARGET_ID ).getStatus().isSuccess() );

        privUtil.assertNotExists( READ_PRIV_ID, CREATE_PRIV_ID, UPDATE_PRIV_ID, DELETE_PRIV_ID );
    }
}
