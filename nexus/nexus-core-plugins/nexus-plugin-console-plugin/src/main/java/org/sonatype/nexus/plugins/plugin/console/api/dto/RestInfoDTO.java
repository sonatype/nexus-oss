package org.sonatype.nexus.plugins.plugin.console.api.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "restInfo" )
public class RestInfoDTO
{
    private String URI;

    private String description;

    public String getURI()
    {
        return URI;
    }

    public void setURI( String uRI )
    {
        URI = uRI;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }
}
