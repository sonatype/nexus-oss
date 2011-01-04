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
package org.sonatype.nexus.integrationtests.nxcm2124;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionContaining;
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

       MatcherAssert.assertThat( getPluginsNames( pluginInfos ),
                                    IsCollectionContaining.hasItem( "Nexus : Core Plugins : Plugin Console" ) );

        PluginInfoDTO pluginConsolePlugin =
            this.getPluginInfoByName( pluginInfos, "Nexus : Core Plugins : Plugin Console" );
        Assert.assertNotNull( pluginConsolePlugin.getDocumentation() );
        Assert.assertFalse( pluginConsolePlugin.getDocumentation().isEmpty() );

        String url = pluginConsolePlugin.getDocumentation().get( 0 ).getUrl();
        Response r = RequestFacade.sendMessage( new URL( url ), Method.GET, null );
        Assert.assertEquals( r.getStatus().getCode(), Status.SUCCESS_OK.getCode(), "Should be able to get the DOCOs" );
    }
}
