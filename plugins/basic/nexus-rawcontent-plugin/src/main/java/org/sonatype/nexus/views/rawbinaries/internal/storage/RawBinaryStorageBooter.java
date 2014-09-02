package org.sonatype.nexus.views.rawbinaries.internal.storage;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;

import com.google.common.eventbus.Subscribe;
import org.eclipse.sisu.EagerSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ensures that there's a view config defined to
 *
 * @since 3.0
 */
@Named
@EagerSingleton
public class RawBinaryStorageBooter
    implements EventSubscriber
{
  private static final Logger log = LoggerFactory.getLogger(RawBinaryStorageBooter.class);

  private final RawBinaryStore rawBinaryStore;

  @Inject
  public RawBinaryStorageBooter(final RawBinaryStore rawBinaryStore) {
    this.rawBinaryStore = checkNotNull(rawBinaryStore);
  }

  @Subscribe
  public void on(NexusInitializedEvent event) throws Exception {
    rawBinaryStore.start();
  }

  @Subscribe
  public void on(NexusStoppingEvent event) throws Exception {
    rawBinaryStore.stop();
  }
}

