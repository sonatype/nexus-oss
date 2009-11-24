package org.sonatype.nexus.integrationtests.plugin.nexus2810;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.util.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.sonatype.nexus.test.utils.plugin.PluginConsoleMessageUtil;

public class NEXUS2810PluginConsoleIT
    extends AbstractNexusIntegrationTest
{

    private PluginConsoleMessageUtil pluginConsoleMsgUtil = new PluginConsoleMessageUtil();

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
                           IsCollectionContaining.hasItems( "Nexus Plugin Console Plugin", "Nexus Broken Plugin" ) );

        PluginInfoDTO pluginConsolePlugin = this.getPluginInfoByName( pluginInfos, "Nexus Plugin Console Plugin" );
        assertPropertyValid( "Name", pluginConsolePlugin.getName(), "Nexus Plugin Console Plugin" );
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

    private List<String> getPluginsNames( List<PluginInfoDTO> pluginInfos )
    {
        if ( pluginInfos == null )
        {
            return null;
        }

        List<String> names = new ArrayList<String>();
        for ( PluginInfoDTO pluginInfoDTO : pluginInfos )
        {
            names.add( pluginInfoDTO.getName() );
        }
        return names;
    }

    private PluginInfoDTO getPluginInfoByName( List<PluginInfoDTO> pluginInfos, String name )
    {
        for ( PluginInfoDTO pluginInfo : pluginInfos )
        {
            if ( pluginInfo.getName().equals( name ) )
            {
                return pluginInfo;
            }
        }

        return null;
    }

    private void assertPropertyValid( String name, String value, String... expectedValue )
    {
        if ( StringUtils.isEmpty( value ) )
        {
            Assert.fail( "Property '" + name + "' is empty!" );
        }

        if ( "N/A".equals( value ) )
        {
            Assert.fail( "Property '" + name + "' is N/A!" );
        }

        if ( expectedValue != null && expectedValue.length > 0 )
        {
            Assert.assertEquals( expectedValue[0], value );
        }
    }
}
