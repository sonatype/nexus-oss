/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.tasks.api;

import javax.inject.Named;

import org.sonatype.nexus.scheduling.Cancelable;
import org.sonatype.nexus.scheduling.CancelableSupport;
import org.sonatype.nexus.proxy.repository.RepositoryTaskSupport;

@Named
public class SleepRepositoryTask
    extends RepositoryTaskSupport<Void>
    implements Cancelable
{
  private boolean cancellable;

  @Override
  protected Void execute()
      throws Exception
  {
    cancellable = getConfiguration().getBoolean("cancellable", false);

    log.debug(getMessage());

    final int time = getTime();
    sleep(time);
    getRepositoryRegistry().getRepository(getConfiguration().getRepositoryId());
    sleep(time);
    return null;
  }

  protected void sleep(final int time)
      throws InterruptedException
  {
    for (int i = 0; i < time; i++) {
      Thread.sleep(1000 / 2);
      if (cancellable) {
        CancelableSupport.checkCancellation();
      }
    }
  }

  private int getTime() {
    return getConfiguration().getInteger("time", 5);
  }

  @Override
  public String getMessage() {
    return "Sleeping for " + getTime() + " seconds (cancellable: " + cancellable + ")!";
  }
}
