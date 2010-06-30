package org.sonatype.nexus.plugins.plugin.console.api.dto;

import javax.xml.bind.annotation.XmlType;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "pluginDocumentation" )
@XmlType( name = "pluginDocumentation" )
public class DocumentationLinkDTO
{
    private String label;
    
    private String url;

    public String getLabel()
    {
        return label;
    }

    public void setLabel( String label )
    {
        this.label = label;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }
}
