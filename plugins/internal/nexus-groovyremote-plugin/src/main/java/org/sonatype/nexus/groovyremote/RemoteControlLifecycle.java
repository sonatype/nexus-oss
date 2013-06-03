package org.sonatype.nexus.groovyremote;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.sonatype.inject.EagerSingleton;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import javax.inject.Named;
import javax.inject.Provider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles starting and stopping {@link RemoteControl} service when Nexus starts and stops.
 *
 * @since 2.6
 */
@Named
@EagerSingleton
public class RemoteControlLifecycle
    extends ComponentSupport
{
    private final Provider<RemoteControl> service;

    @Inject
    public RemoteControlLifecycle(final Provider<RemoteControl> service,
                                  final EventBus eventBus)
    {
        this.service = checkNotNull(service);
        eventBus.register(this);
    }

    @Subscribe
    public void handle(final NexusStartedEvent event) throws Exception {
        service.get().start();
    }

    @Subscribe
    public void handle(final NexusStoppedEvent event) throws Exception {
        service.get().stop();
    }
}
