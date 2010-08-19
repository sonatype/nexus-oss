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
                                    IsCollectionContaining.hasItem( "Nexus : Core Plugins : Plugin Console Plugin" ) );

        PluginInfoDTO pluginConsolePlugin =
            this.getPluginInfoByName( pluginInfos, "Nexus : Core Plugins : Plugin Console Plugin" );
        Assert.assertNotNull( pluginConsolePlugin.getDocumentation() );
        Assert.assertFalse( pluginConsolePlugin.getDocumentation().isEmpty() );

        String url = pluginConsolePlugin.getDocumentation().get( 0 ).getUrl();
        Response r = RequestFacade.sendMessage( new URL( url ), Method.GET, null );
        Assert.assertEquals( r.getStatus().getCode(), Status.SUCCESS_OK.getCode(), "Should be able to get the DOCOs" );
    }
}
