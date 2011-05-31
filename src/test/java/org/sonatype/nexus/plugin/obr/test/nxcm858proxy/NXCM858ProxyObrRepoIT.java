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
package org.sonatype.nexus.plugin.obr.test.nxcm858proxy;

import java.io.File;

import org.sonatype.nexus.plugin.obr.test.AbstractObrDownloadIT;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NXCM858ProxyObrRepoIT
    extends AbstractObrDownloadIT
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        final ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
        prop.setValue( "obr-hosted" );

        TaskScheduleUtil.runTask( "PublishObrDescriptorTask", prop );
    }

    @Test
    public void downloadFromShadow()
        throws Exception
    {
        downloadApacheFelixWebManagement( "obr-proxy" );

        assertExists( "org/apache/felix/org.apache.felix.webconsole/3.0.0/org.apache.felix.webconsole-3.0.0.jar" );
        assertExists( "org/apache/felix/org.osgi.compendium/1.4.0/org.osgi.compendium-1.4.0.jar" );
        assertExists( "org/apache/geronimo/specs/geronimo-servlet_3.0_spec/1.0/geronimo-servlet_3.0_spec-1.0.jar" );
        assertExists( "org/apache/portals/portlet-api_2.0_spec/1.0/portlet-api_2.0_spec-1.0.jar" );

    }

    private void assertExists( final String filename )
    {
        final File f = new File( nexusWorkDir, "storage/obr-proxy/" + filename );
        Assert.assertTrue( f.exists(), "File not found: " + filename );
    }
}
