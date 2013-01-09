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

/**
 * Guava {@link EventBus} handler that listens for Nexus events and performs BC provider registration/removal with using
 * {@link BCManager}.
 * 
 * @author cstamas
 * @since 2.4
 */
@Named
@EagerSingleton
public class BCPluginEventHandler
{
    private final BCManager bcManager;

    /**
     * Default constructor.
     * 
     * @param bcManager the {@link BCManager} instance.
     * @param eventBus the {@link EventBus} to register with.
     */
    @Inject
    public BCPluginEventHandler( final BCManager bcManager, final EventBus eventBus )
    {
        this.bcManager = checkNotNull( bcManager );
        eventBus.register( this );
    }

    /**
     * {@link NexusInitializedEvent} handler: registers BC provider.
     * 
     * @param e the event (not used)
     */
    @Subscribe
    public void onNexusInitializedEvent( final NexusInitializedEvent e )
    {
        bcManager.registerProvider();
    }

    /**
     * {@link NexusStoppedEvent} handler: unregisters BC provider.
     * 
     * @param e the event (not used)
     */
    @Subscribe
    public void onNexusStoppedEvent( final NexusStoppedEvent e )
    {
        bcManager.unregisterProvider();
    }
}
