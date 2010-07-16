package com.sonatype.nexus.plugin.groovyconsole.rest.dto;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "groovyScriptResponse" )
public class GroovyScriptResponseDTO
{

    private List<GroovyScriptDTO> data;

    public List<GroovyScriptDTO> getData()
    {
        if ( data == null )
        {
            data = new ArrayList<GroovyScriptDTO>();
        }
        return data;
    }

    public void setData( List<GroovyScriptDTO> data )
    {
        this.data = data;
    }

}
