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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1381;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

import java.io.File;

import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.annotations.Test;

public class NXCM1381UpdateSiteAbsoluteUrlsIT
    extends AbstractNexusProxyP2IT
{

    public NXCM1381UpdateSiteAbsoluteUrlsIT()
    {
        super( "nxcm1381" );
    }

    @Test
    public void test()
        throws Exception
    {
        final File nexusDir = new File( nexusWorkDir, "storage/nxcm1381" );

        TaskScheduleUtil.run( "1" );
        TaskScheduleUtil.waitForAllTasksToStop();

        assertThat( new File( nexusDir, "features/com.sonatype.nexus.p2.its.feature_1.0.0.jar" ), exists() );
        assertThat( new File( nexusDir, "plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar" ), exists() );
    }

}
