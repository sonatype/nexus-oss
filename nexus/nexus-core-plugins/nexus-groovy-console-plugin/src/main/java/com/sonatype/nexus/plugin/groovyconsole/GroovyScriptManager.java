package com.sonatype.nexus.plugin.groovyconsole;

import java.io.IOException;
import java.util.List;

import org.sonatype.plexus.appevents.Event;

import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptDTO;

public interface GroovyScriptManager
{
    List<GroovyScriptDTO> getScripts();

    void save( GroovyScriptDTO script )
        throws IOException;

    void actUponEvent( Event<?> evt );
}
