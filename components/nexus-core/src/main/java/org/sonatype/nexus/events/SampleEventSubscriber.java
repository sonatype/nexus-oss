package org.sonatype.nexus.events;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.NexusStateChangeEvent;

import com.google.common.eventbus.Subscribe;

/**
 * Created with IntelliJ IDEA.
 * User: cstamas
 * Date: 10/7/13
 * Time: 23:33
 * To change this template use File | Settings | File Templates.
 */
@Named
@Singleton
public class SampleEventSubscriber
    extends AbstractLoggingComponent
    implements EventSubscriber
{
  @Subscribe
  public void on(final NexusStateChangeEvent evt) {
    getLogger().info("@@@@ " + evt.toString());
  }
}
