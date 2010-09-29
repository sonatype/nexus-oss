package com.sonatype.nexus.plugin.groovyconsole;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "groovy" )
public class DefaultGroovyEventInspector
    extends AbstractEventInspector
    implements AsynchronousEventInspector
{
    @Requirement
    private GroovyScriptManager groovyScriptManager;

    public boolean accepts( Event<?> evt )
    {
        return true;
    }

    public void inspect( Event<?> evt )
    {
        groovyScriptManager.actUponEvent( evt );
    }
}
