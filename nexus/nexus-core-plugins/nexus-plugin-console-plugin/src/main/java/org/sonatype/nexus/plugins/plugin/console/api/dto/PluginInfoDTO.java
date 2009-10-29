package org.sonatype.nexus.plugins.plugin.console.api.dto;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "pluginInfo" )
public class PluginInfoDTO
{
    private String name;

    private String description;

    private String version;

    private String status;

    private String failureReason;

    private String scmVersion;

    private String scmTimestamp;

    private String site;

    private List<RestInfoDTO> restInfos = new ArrayList<RestInfoDTO>();

    public String getSite()
    {
        return site;
    }

    public void setSite( String site )
    {
        this.site = site;
    }

    public String getFailureReason()
    {
        return failureReason;
    }

    public void setFailureReason( String failureReason )
    {
        this.failureReason = failureReason;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus( String status )
    {
        this.status = status;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getScmVersion()
    {
        return scmVersion;
    }

    public void setScmVersion( String scmVersion )
    {
        this.scmVersion = scmVersion;
    }

    public String getScmTimestamp()
    {
        return scmTimestamp;
    }

    public void setScmTimestamp( String scmTimestamp )
    {
        this.scmTimestamp = scmTimestamp;
    }

    public List<RestInfoDTO> getRestInfos()
    {
        return restInfos;
    }

    public void setRestInfos( List<RestInfoDTO> restInfos )
    {
        this.restInfos = restInfos;
    }

    public void addRestInfo( RestInfoDTO restInfo )
    {
        this.restInfos.add( restInfo );
    }
}
