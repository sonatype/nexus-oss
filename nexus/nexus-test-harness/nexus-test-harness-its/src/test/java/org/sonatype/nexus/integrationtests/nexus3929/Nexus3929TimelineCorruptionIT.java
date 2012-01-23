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
package org.sonatype.nexus.integrationtests.nexus3929;

import static org.hamcrest.MatcherAssert.*;
import static org.sonatype.nexus.test.utils.StatusMatchers.*;
import static org.testng.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
        assertThat( UserCreationUtil.login(), isSuccess() );
    }

    @Test
    public void status()
        throws Exception
    {
        assertEquals( getNexusStatusUtil().getNexusStatus().getData().getState(), "STARTED" );
    }
}
