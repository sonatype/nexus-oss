/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.plugin.obr.test.nxcm858virtual;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.testng.annotations.Test;

import com.sonatype.nexus.plugin.obr.test.AbstractObrDownloadIT;

public class NXCM858ShadowObrRepoIT
    extends AbstractObrDownloadIT
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        FileUtils.deleteDirectory( new File( FELIX_REPO, ".meta" ) );
    }

    @Test
    public void downloadFromShadow()
        throws Exception
    {
        downloadApacheFelixWebManagement( "obr-shadow" );
    }
}
