package org.sonatype.nexus.proxy.item.uid;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "RepositoryItemUidAttributeEventInspector" )
public class RepositoryItemUidAttributeEventInspector
    extends AbstractEventInspector
    implements EventInspector
{
    @Requirement
    private RepositoryItemUidAttributeManager manager;

    @Override
    public boolean accepts( Event<?> evt )
    {
        final String simpleName = evt.getClass().getName();

        // TODO: nexus-proxy module does not reference plugin manager, so this is a quick'n'dirty workaround for now
        return evt instanceof NexusStartedEvent
            || StringUtils.equals( simpleName, "org.sonatype.nexus.plugins.events.PluginActivatedEvent" )
            || StringUtils.equals( simpleName, "org.sonatype.nexus.plugins.events.PluginDeactivatedEvent" );
    }

    @Override
    public void inspect( Event<?> evt )
    {
        manager.reset();
    }
}
