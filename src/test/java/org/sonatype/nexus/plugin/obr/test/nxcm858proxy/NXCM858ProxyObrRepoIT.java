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
