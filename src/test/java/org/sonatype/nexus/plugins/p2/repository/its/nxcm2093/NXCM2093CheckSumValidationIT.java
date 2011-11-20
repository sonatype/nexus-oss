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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm2093;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.contains;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NXCM2093CheckSumValidationIT
    extends AbstractNexusProxyP2IT
{

    public NXCM2093CheckSumValidationIT()
    {
        super( "nxcm2093-bad-checksum" );
    }

    @Test
    public void test()
        throws Exception
    {
        final File installDir = new File( "target/eclipse/nxcm2093" );

        // the must work one
        installUsingP2(
            getNexusTestRepoUrl( "nxcm2093-ok-checksum" ),
            "org.mortbay.jetty.util",
            installDir.getCanonicalPath()
        );

        try
        {
            final Map<String, String> env = new HashMap<String, String>();
            env.put( "eclipse.p2.MD5Check", "false" );

            installUsingP2(
                getNexusTestRepoUrl(),
                "com.sonatype.nexus.p2.its.feature.feature.group",
                installDir.getCanonicalPath(), env
            );
            Assert.fail();
        }
        catch ( final Exception e )
        {
            assertThat(
                getNexusLogFile(),
                contains(
                    "Proxied item nxcm2093-bad-checksum:/plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar evaluated as INVALID"
                )
            );
        }

        final RepositoryMessageUtil repoUtil = new RepositoryMessageUtil(
            this, getXMLXStream(), MediaType.APPLICATION_XML
        );
        final RepositoryStatusResource repoStatusResource = repoUtil.getStatus( getTestRepositoryId() );

        assertThat( repoStatusResource.getProxyMode(), is( equalTo( ProxyMode.ALLOW.name() ) ) );
        assertThat( repoStatusResource.getLocalStatus(), is( equalTo( LocalStatus.IN_SERVICE.name() ) ) );
    }

}
