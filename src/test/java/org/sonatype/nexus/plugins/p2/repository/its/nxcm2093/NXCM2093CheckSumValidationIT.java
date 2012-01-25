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
            final Map<String, String> sysProps = new HashMap<String, String>();
            sysProps.put( "eclipse.p2.MD5Check", "false" );

            installUsingP2(
                getNexusTestRepoUrl(),
                "com.sonatype.nexus.p2.its.feature.feature.group",
                installDir.getCanonicalPath(), sysProps
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
