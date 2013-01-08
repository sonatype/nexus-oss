package org.sonatype.nexus.plugins.bcprov.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.inject.EagerSingleton;
import org.sonatype.nexus.plugins.bcprov.BCManager;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;

@Named
@EagerSingleton
public class BCPluginEventHandler
{
    private final EventBus eventBus;

    private final BCManager bcManager;

    @Inject
    public BCPluginEventHandler( final EventBus eventBus, final BCManager bcManager )
    {
        this.eventBus = checkNotNull( eventBus );
        this.bcManager = checkNotNull( bcManager );
        eventBus.register( this );
    }

    @Subscribe
    public void onNexusInitializedEvent( final NexusInitializedEvent e )
    {
        bcManager.registerProvider();
    }

    @Subscribe
    public void onNexusStoppedEvent( final NexusStoppedEvent e )
    {
        bcManager.unregisterProvider();
    }
}
