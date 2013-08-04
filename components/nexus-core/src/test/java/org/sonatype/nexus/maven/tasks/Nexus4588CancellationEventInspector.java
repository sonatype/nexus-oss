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

package org.sonatype.nexus.maven.tasks;

import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryEventExpireCaches;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.scheduling.TaskUtil;

/**
 * Cancellation inspector that cancels the current thread, simulating user intervention about cancelling tasks.
 * It will cancel whenever a repository subject expires caches. This relies on implementation detail that
 * snapshot remover upon 1st pass will perform cache expiration.
 *
 * @author: cstamas
 */
public class Nexus4588CancellationEventInspector
    implements EventInspector
{

  private boolean active;

  public boolean isActive() {
    return active;
  }

  public void setActive(final boolean active) {
    this.active = active;
  }

  @Override
  public boolean accepts(final Event<?> evt) {
    return isActive() && evt instanceof RepositoryEventExpireCaches;
  }

  @Override
  public void inspect(final Event<?> evt) {
    if (isActive()) {
      TaskUtil.getCurrentProgressListener().cancel();
    }
  }
}
