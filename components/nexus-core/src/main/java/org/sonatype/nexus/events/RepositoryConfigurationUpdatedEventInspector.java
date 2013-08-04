/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.events;

import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ExpireCacheTask;
import org.sonatype.plexus.appevents.Event;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Event inspector listening for configuration changes to expire caches when Local or Remote URL changed of the
 * repository.
 */
@Component(role = EventInspector.class, hint = "RepositoryConfigurationUpdatedEventInspector")
public class RepositoryConfigurationUpdatedEventInspector
    extends AbstractEventInspector
    implements AsynchronousEventInspector
{
  @Requirement
  private NexusScheduler nexusScheduler;

  public boolean accepts(Event<?> evt) {
    return evt instanceof RepositoryConfigurationUpdatedEvent;
  }

  public void inspect(Event<?> evt) {
    if (evt instanceof RepositoryConfigurationUpdatedEvent) {
      RepositoryConfigurationUpdatedEvent event = (RepositoryConfigurationUpdatedEvent) evt;

      if (event.isLocalUrlChanged() || event.isRemoteUrlChanged()) {
        String taskName = null;
        String logMessage = null;

        if (event.isLocalUrlChanged() && event.isRemoteUrlChanged()) {
          // both changed
          taskName = "Local and Remote URLs changed, repositoryId=" + event.getRepository().getId() + ".";

          logMessage =
              "The Local and Remote URL of repository \"" + event.getRepository().getName() + "\" (id="
                  + event.getRepository().getId() + ") has been changed, expiring its caches.";

        }
        else if (!event.isLocalUrlChanged() && event.isRemoteUrlChanged()) {
          // remote URL changed
          taskName = "Remote URL changed, repositoryId=" + event.getRepository().getId() + ".";

          logMessage =
              "The Remote URL of repository \"" + event.getRepository().getName() + "\" (id="
                  + event.getRepository().getId() + ") has been changed, expiring its caches.";
        }
        else if (event.isLocalUrlChanged() && !event.isRemoteUrlChanged()) {
          // local URL changed
          taskName = "Local URL changed, repositoryId=" + event.getRepository().getId() + ".";

          logMessage =
              "The Local URL of repository \"" + event.getRepository().getName() + "\" (id="
                  + event.getRepository().getId() + ") has been changed, expiring its caches.";
        }

        ExpireCacheTask task = nexusScheduler.createTaskInstance(ExpireCacheTask.class);

        task.setRepositoryId(event.getRepository().getId());

        nexusScheduler.submit(taskName, task);

        getLogger().info(logMessage);
      }
    }
  }
}
