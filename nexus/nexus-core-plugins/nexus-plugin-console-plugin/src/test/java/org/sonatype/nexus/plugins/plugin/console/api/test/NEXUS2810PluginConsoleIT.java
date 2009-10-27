package org.sonatype.nexus.plugins.plugin.console.api.test;

import java.util.List;

import junit.framework.Assert;

import org.apache.maven.it.util.StringUtils;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.sonatype.nexus.plugins.plugin.console.api.test.util.PluginConsoleMessageUtil;

public class NEXUS2810PluginConsoleIT
    extends AbstractNexusIntegrationTest
{
    private PluginConsoleMessageUtil pluginConsoleMsgUtil = new PluginConsoleMessageUtil();

    @Test
    public void testListPluginInfos()
        throws Exception
    {
        List<PluginInfoDTO> pluginInfos = pluginConsoleMsgUtil.listPluginInfos();

        Assert.assertEquals( 1, pluginInfos.size() );

        PluginInfoDTO pluginInfo = pluginInfos.get( 0 );

        assertPropertyValid( "Name", pluginInfo.getName(), "Nexus Plugin Console Plugin" );
        assertPropertyValid( "Version", pluginInfo.getVersion() );
        assertPropertyValid( "Description", pluginInfo.getDescription(), "Nexus Core Plugin :: Plugin Console" );
        assertPropertyValid( "Status", pluginInfo.getStatus(), "ACTIVATED" );
        assertPropertyValid( "SCM Version", pluginInfo.getScmVersion() );
        assertPropertyValid( "Site", pluginInfo.getSite() );
        Assert.assertTrue( StringUtils.isEmpty( pluginInfo.getFailureReason() ) );

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
