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
package org.sonatype.nexus.integrationtests.nexus3929;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.UserCreationUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus3929TimelineCorruptionIT
    extends AbstractNexusIntegrationTest
{

    @Override
    @BeforeMethod( alwaysRun = true )
    public void oncePerClassSetUp()
        throws Exception
    {
        synchronized ( AbstractNexusIntegrationTest.class )
        {
            if ( NEEDS_INIT )
            {
                super.oncePerClassSetUp();

                stopNexus();

                File tl = new File( nexusWorkDir, "timeline/index" );
                while ( FileUtils.listFiles( tl, new String[] { "cfs" }, false ).size() < 7 )
                {
                    startNexus();
                    stopNexus();
                }

                @SuppressWarnings( "unchecked" )
                List<File> cfs = new ArrayList<File>( FileUtils.listFiles( tl, new String[] { "cfs" }, false ) );
                FileUtils.forceDelete( cfs.get( 0 ) );
                FileUtils.forceDelete( cfs.get( 2 ) );
                FileUtils.forceDelete( cfs.get( 5 ) );

                startNexus();
            }
        }
    }

    @Test
    public void login()
        throws Exception
    {
        Status s = UserCreationUtil.login();
        assertTrue( s.isSuccess() );
    }

    @Test
    public void status()
        throws Exception
    {
        assertEquals( getNexusStatusUtil().getNexusStatus().getData().getState(), "STARTED" );
    }
}
