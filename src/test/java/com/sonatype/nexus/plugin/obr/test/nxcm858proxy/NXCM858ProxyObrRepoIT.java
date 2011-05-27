/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.plugin.obr.test.nxcm858proxy;

import java.io.File;

import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sonatype.nexus.plugin.obr.test.AbstractObrDownloadIT;

public class NXCM858ProxyObrRepoIT
    extends AbstractObrDownloadIT
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
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

    private void assertExists( String filename )
    {
        File f = new File( nexusWorkDir, "storage/obr-proxy/" + filename );
        Assert.assertTrue( f.exists(), "File not found: " + filename );
    }
}
