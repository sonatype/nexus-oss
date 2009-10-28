package org.sonatype.nexus.plugins.plugin.console.api.test.nexus2810;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.it.util.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.sonatype.nexus.plugins.plugin.console.api.test.util.PluginConsoleMessageUtil;

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

        Assert.assertEquals( 2, pluginInfos.size() );

        PluginInfoDTO activatedPluginInfo = pluginInfos.get( 0 );

        assertPropertyValid( "Name", activatedPluginInfo.getName(), "Nexus Plugin Console Plugin" );
        assertPropertyValid( "Version", activatedPluginInfo.getVersion() );
        assertPropertyValid( "Description", activatedPluginInfo.getDescription(), "Nexus Core Plugin :: Plugin Console" );
        assertPropertyValid( "Status", activatedPluginInfo.getStatus(), "ACTIVATED" );
        assertPropertyValid( "SCM Version", activatedPluginInfo.getScmVersion() );
        assertPropertyValid( "Site", activatedPluginInfo.getSite() );
        Assert.assertTrue( StringUtils.isEmpty( activatedPluginInfo.getFailureReason() ) );

        PluginInfoDTO brokenPluginInfo = pluginInfos.get( 1 );

        assertPropertyValid( "Name", brokenPluginInfo.getName() );
        assertPropertyValid( "Version", brokenPluginInfo.getVersion() );
        assertPropertyValid( "Status", brokenPluginInfo.getStatus(), "BROKEN" );
        Assert.assertEquals( "N/A", brokenPluginInfo.getDescription() );
        Assert.assertEquals( "N/A", brokenPluginInfo.getScmVersion() );
        Assert.assertEquals( "N/A", brokenPluginInfo.getSite() );
        Assert.assertFalse( StringUtils.isEmpty( brokenPluginInfo.getFailureReason() ) );
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
