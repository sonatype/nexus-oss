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
package org.sonatype.nexus.integrationtests.proxy.nexus1089;

import static org.sonatype.nexus.integrationtests.ITGroups.PROXY;

import java.io.File;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus1089SecureProxyIT
    extends AbstractNexusProxyIntegrationTest
{

    @Override
    @BeforeMethod(alwaysRun = true)
    public void startProxy()
        throws Exception
    {
        ServletServer server = lookup( ServletServer.class, "secure" );
        server.start();
    }

    @Override
    @AfterMethod(alwaysRun = true)
    public void stopProxy()
        throws Exception
    {
        ServletServer server = lookup( ServletServer.class, "secure" );
        server.stop();
    }

    @Test(groups = PROXY)
    public void downloadArtifact()
        throws Exception
    {
        File localFile = this.getLocalFile( "release-proxy-repo-1", "nexus1089", "artifact", "1.0", "jar" );

        File artifact = this.downloadArtifact( "nexus1089", "artifact", "1.0", "jar", null, "target/downloads" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( artifact, localFile ) );

    }
}
