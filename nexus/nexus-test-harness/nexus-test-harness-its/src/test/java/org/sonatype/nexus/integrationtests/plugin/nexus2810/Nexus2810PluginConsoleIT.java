package org.sonatype.nexus.integrationtests.plugin.nexus2810;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.it.util.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;

public class Nexus2810PluginConsoleIT
    extends AbstractPluginConsoleIT
{

    @Override
    protected void copyTestResources()
        throws IOException
    {
        super.copyTestResources();

        File source = this.getTestFile( "broken-plugin" );

        File desti = new File( this.getNexusBaseDir(), "runtime/apps/nexus/plugin-repository" );

        FileUtils.copyDirectoryStructure( source, desti );
    }

    @Test
    public void testListPluginInfos()
        throws Exception
    {
        List<PluginInfoDTO> pluginInfos = pluginConsoleMsgUtil.listPluginInfos();

        Assert.assertThat( getPluginsNames( pluginInfos ),
                           IsCollectionContaining.hasItems( "Nexus : Core Plugins : Plugin Console Plugin", "Nexus Broken Plugin" ) );

        PluginInfoDTO pluginConsolePlugin = this.getPluginInfoByName( pluginInfos, "Nexus : Core Plugins : Plugin Console Plugin" );
        assertPropertyValid( "Name", pluginConsolePlugin.getName(), "Nexus : Core Plugins : Plugin Console Plugin" );
        assertPropertyValid( "Version", pluginConsolePlugin.getVersion() );
        assertPropertyValid( "Description", pluginConsolePlugin.getDescription(), "Nexus Core Plugin :: Plugin Console" );
        assertPropertyValid( "Status", pluginConsolePlugin.getStatus(), "ACTIVATED" );
        assertPropertyValid( "SCM Version", pluginConsolePlugin.getScmVersion() );
        assertPropertyValid( "SCM Timestamp", pluginConsolePlugin.getScmTimestamp() );
        assertPropertyValid( "Site", pluginConsolePlugin.getSite() );
        Assert.assertTrue( StringUtils.isEmpty( pluginConsolePlugin.getFailureReason() ) );
        Assert.assertTrue( !pluginConsolePlugin.getRestInfos().isEmpty() );

        PluginInfoDTO pgpPlugin = this.getPluginInfoByName( pluginInfos, "Nexus Broken Plugin" );
        assertPropertyValid( "Name", pgpPlugin.getName() );
        assertPropertyValid( "Version", pgpPlugin.getVersion() );
        assertPropertyValid( "Status", pgpPlugin.getStatus(), "BROKEN" );
        Assert.assertNull( pgpPlugin.getDescription() );
        Assert.assertEquals( "N/A", pgpPlugin.getScmVersion() );
        Assert.assertEquals( "N/A", pgpPlugin.getScmTimestamp() );
        assertPropertyValid( "Site", pgpPlugin.getSite() );
        Assert.assertFalse( StringUtils.isEmpty( pgpPlugin.getFailureReason() ) );
        Assert.assertTrue( pgpPlugin.getRestInfos().isEmpty() );
    }
}
