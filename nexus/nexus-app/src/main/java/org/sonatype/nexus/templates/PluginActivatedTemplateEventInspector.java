package org.sonatype.nexus.templates;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

/**
 * TODO: Not sure how justified is this EventInspector. This is here only to "ping" the TemplateProviders in plugins!
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "PluginActivatedTemplateEventInspector" )
public class PluginActivatedTemplateEventInspector
    extends AbstractEventInspector
    implements EventInspector
{
    @Requirement
    private TemplateManager templateManager;

    public boolean accepts( Event<?> evt )
    {
        return evt instanceof PluginActivatedEvent;
    }

    public void inspect( Event<?> evt )
    {
        if ( evt instanceof PluginActivatedEvent )
        {
            // just ping it
            templateManager.getTemplates();
        }
    }
}
