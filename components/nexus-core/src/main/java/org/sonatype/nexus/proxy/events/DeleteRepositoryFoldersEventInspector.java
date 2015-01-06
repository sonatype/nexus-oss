/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.events;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.events.Asynchronous;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.wastebasket.RepositoryFolderRemover;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Spawns a background task to delete repository folders upon removal.
 *
 * @author cstamas
 */
@Named
@Singleton
public class DeleteRepositoryFoldersEventInspector
    extends ComponentSupport
    implements EventSubscriber, Asynchronous
{
  private final RepositoryFolderRemover repositoryFolderRemover;

  @Inject
  public DeleteRepositoryFoldersEventInspector(final RepositoryFolderRemover repositoryFolderRemover)
  {
    this.repositoryFolderRemover = checkNotNull(repositoryFolderRemover);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void inspect(final RepositoryRegistryEventPostRemove evt) {
    final Repository repository = evt.getRepository();
    try {
      repositoryFolderRemover.deleteRepositoryFolders(repository, false);
    }
    catch (IOException e) {
      log.warn("Unable to delete repository folders ", e);
    }
  }
}
