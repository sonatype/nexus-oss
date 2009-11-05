package org.sonatype.nexus.integrationtests.plugin.nexus2810;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.it.util.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
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

        Assert.assertEquals( 3, pluginInfos.size() );

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

        PluginInfoDTO pgpPlugin = this.getPluginInfoByName( pluginInfos, "Nexus Enterprise Plugin :: PGP" );
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
