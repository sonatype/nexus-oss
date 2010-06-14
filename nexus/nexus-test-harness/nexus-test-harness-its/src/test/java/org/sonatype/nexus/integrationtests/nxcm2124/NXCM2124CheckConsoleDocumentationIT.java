package org.sonatype.nexus.integrationtests.nxcm2124;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.plugin.nexus2810.AbstractPluginConsoleIT;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;

public class NXCM2124CheckConsoleDocumentationIT
    extends AbstractPluginConsoleIT
{

    @Test
    public void checkDoc()
        throws IOException
    {
        List<PluginInfoDTO> pluginInfos = pluginConsoleMsgUtil.listPluginInfos();

        Assert.assertThat( getPluginsNames( pluginInfos ),
                           IsCollectionContaining.hasItem( "Nexus Plugin Console Plugin" ) );

        PluginInfoDTO pluginConsolePlugin = this.getPluginInfoByName( pluginInfos, "Nexus Plugin Console Plugin" );
        Assert.assertNotNull( pluginConsolePlugin.getDocumentation() );
        Assert.assertFalse( pluginConsolePlugin.getDocumentation().isEmpty() );

        Response r = RequestFacade.doGetRequest( pluginConsolePlugin.getDocumentation().get( 0 ) + "/docs/index.html" );
        Assert.assertTrue( r.getStatus().isSuccess() );

    }

}
