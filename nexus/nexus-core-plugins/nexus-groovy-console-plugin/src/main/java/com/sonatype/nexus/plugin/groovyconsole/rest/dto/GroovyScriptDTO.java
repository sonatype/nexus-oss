package com.sonatype.nexus.plugin.groovyconsole.rest.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "groovyScript" )
public class GroovyScriptDTO
{

    private String name;

    private String script;

    public GroovyScriptDTO()
    {
        super();
    }

    public GroovyScriptDTO( String name, String script )
    {
        this();
        this.name = name;
        this.script = script;
    }

    public String getScript()
    {
        return script;
    }

    public String getName()
    {
        return name;
    }

    public void setScript( String script )
    {
        this.script = script;
    }

    public void setName( String name )
    {
        this.name = name;
    }
}
