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
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventPostRemove;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.DeleteRepositoryFoldersTask;
import org.sonatype.plexus.appevents.Event;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Spawns a background task to delete repository folders upon removal.
 *
 * @author cstamas
 */
@Component(role = EventInspector.class, hint = "DeleteRepositoryFoldersEventInspector")
public class DeleteRepositoryFoldersEventInspector
    extends AbstractEventInspector
    implements AsynchronousEventInspector
{
  @Requirement
  private NexusScheduler nexusScheduler;

  public boolean accepts(Event<?> evt) {
    return (evt instanceof RepositoryRegistryEventPostRemove);
  }

  public void inspect(Event<?> evt) {
    Repository repository = ((RepositoryRegistryEventPostRemove) evt).getRepository();

    try {
      // remove the storage folders for the repository
      DeleteRepositoryFoldersTask task = nexusScheduler.createTaskInstance(DeleteRepositoryFoldersTask.class);

      task.setRepository(repository);

      nexusScheduler.submit("Deleting repository folder for repository \"" + repository.getName() + "\" (id="
          + repository.getId() + ").", task);
    }
    catch (Exception e) {
      getLogger().error(
          "Could not remove repository folders for repository \"" + repository.getName() + "\" (id="
              + repository.getId() + ")!", e);
    }
  }
}
