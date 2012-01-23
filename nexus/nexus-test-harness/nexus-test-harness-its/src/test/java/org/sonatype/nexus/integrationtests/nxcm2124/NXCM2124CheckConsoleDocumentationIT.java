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
package org.sonatype.nexus.integrationtests.nxcm2124;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.sonatype.nexus.test.utils.StatusMatchers.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.plugin.nexus2810.AbstractPluginConsoleIT;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NXCM2124CheckConsoleDocumentationIT
    extends AbstractPluginConsoleIT
{
    @Test
    public void checkDoc()
        throws IOException
    {
        List<PluginInfoDTO> pluginInfos = pluginConsoleMsgUtil.listPluginInfos();

        assertThat( getPluginsNames( pluginInfos ), hasItem( "Nexus : Core Plugins : Plugin Console" ) );

        PluginInfoDTO pluginConsolePlugin =
            this.getPluginInfoByName( pluginInfos, "Nexus : Core Plugins : Plugin Console" );
        Assert.assertNotNull( pluginConsolePlugin.getDocumentation() );
        Assert.assertFalse( pluginConsolePlugin.getDocumentation().isEmpty() );

        String url = pluginConsolePlugin.getDocumentation().get( 0 ).getUrl();
        Response r = null;

        try
        {
            r = RequestFacade.sendMessage( new URL( url ), Method.GET, null );
            Status status = r.getStatus();
            assertThat( status, isSuccess() );
        }
        finally
        {
            RequestFacade.releaseResponse( r );
        }
    }
}
