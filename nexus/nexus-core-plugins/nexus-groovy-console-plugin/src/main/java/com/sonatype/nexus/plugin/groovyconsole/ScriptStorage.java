package com.sonatype.nexus.plugin.groovyconsole;

import java.io.IOException;
import java.util.Map;

import org.sonatype.plexus.appevents.Event;

public interface ScriptStorage
{

    String getScript( Class<? extends Event<?>> eventClass );

    Map<String, String> getScripts();

    void store( String name, String body ) throws IOException;

}
