package org.sonatype.nexus.integrationtests.plugin.nexus2810;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.util.StringUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.testng.Assert;

public abstract class AbstractPluginConsoleIT
    extends AbstractNexusIntegrationTest
{

    protected PluginConsoleMessageUtil pluginConsoleMsgUtil = new PluginConsoleMessageUtil();

    public AbstractPluginConsoleIT()
    {
        super();
    }

    public AbstractPluginConsoleIT( String testRepositoryId )
    {
        super( testRepositoryId );
    }

    protected List<String> getPluginsNames( List<PluginInfoDTO> pluginInfos )
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

    protected PluginInfoDTO getPluginInfoByName( List<PluginInfoDTO> pluginInfos, String name )
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

    protected void assertPropertyValid( String name, String value, String... expectedValue )
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