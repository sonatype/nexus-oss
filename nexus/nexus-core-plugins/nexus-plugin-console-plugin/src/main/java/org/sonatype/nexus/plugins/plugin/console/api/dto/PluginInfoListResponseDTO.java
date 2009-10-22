package org.sonatype.nexus.plugins.plugin.console.api.dto;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "pluginInfos" )
public class PluginInfoListResponseDTO
{
    private List<PluginInfoDTO> data = new ArrayList<PluginInfoDTO>();

    public List<PluginInfoDTO> getData()
    {
        return data;
    }

    public void setData( List<PluginInfoDTO> data )
    {
        this.data = data;
    }

    public void addPluginInfo( PluginInfoDTO pluginInfo )
    {
        data.add( pluginInfo );
    }
}
