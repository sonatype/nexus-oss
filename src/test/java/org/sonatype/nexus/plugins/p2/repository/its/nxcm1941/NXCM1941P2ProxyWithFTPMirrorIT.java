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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1941;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.isDirectory;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.readable;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.Test;

public class NXCM1941P2ProxyWithFTPMirrorIT
    extends AbstractNexusProxyP2IT
{

    public NXCM1941P2ProxyWithFTPMirrorIT()
    {
        super( "nxcm1941" );
    }

    @Override
    protected void copyTestResources()
        throws IOException
    {
        super.copyTestResources();

        final String proxyRepoBaseUrl = TestProperties.getString( "proxy.repo.base.url" );
        assertThat( proxyRepoBaseUrl, startsWith( "http://" ) );

        replaceInFile( "target/nexus/proxy-repo/nxcm1941/artifacts.xml", "${proxy-repo-base-url}", proxyRepoBaseUrl );
        replaceInFile( "target/nexus/proxy-repo/nxcm1941/mirrors.xml", "${proxy-repo-base-url}", proxyRepoBaseUrl );
        replaceInFile( "target/nexus/proxy-repo/nxcm1941/mirrors.xml", "${ftp-proxy-repo-base-url}", "ftp"
            + proxyRepoBaseUrl.substring( 4 ) );
    }

    @Test
    public void test()
        throws Exception
    {
        installAndVerifyP2Feature();
    }

}
