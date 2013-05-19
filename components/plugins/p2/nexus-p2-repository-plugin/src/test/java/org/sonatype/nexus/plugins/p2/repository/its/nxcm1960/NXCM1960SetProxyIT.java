/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1960;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.isDirectory;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.readable;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
//import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

@Ignore("FIXME: Need to update http proxy api usage")
public class NXCM1960SetProxyIT
    extends AbstractNexusProxyP2IT
{

    public NXCM1960SetProxyIT()
    {
        super( "nxcm1960" );
    }

    @Test
    public void test()
        throws Exception
    {
        setupProxyConfig();

        installAndVerifyP2Feature();
    }

    private void setupProxyConfig()
        throws IOException
    {
        final GlobalConfigurationResource resource = SettingsMessageUtil.getCurrentSettings();

        // FIXME: Update api
        //RemoteHttpProxySettings proxy = resource.getRemoteProxySettings();
        //
        //if ( proxy == null )
        //{
        //    proxy = new RemoteHttpProxySettings();
        //    resource.setGlobalHttpProxySettings( proxy );
        //}
        //
        //proxy.setProxyHostname( "http://somejunkproxyurl" );
        //proxy.setProxyPort( 555 );
        //proxy.getNonProxyHosts().clear();
        //proxy.addNonProxyHost( "localhost" );

        final Status status = SettingsMessageUtil.save( resource );

        assertThat( status.isSuccess(), is( true ) );
    }

}
