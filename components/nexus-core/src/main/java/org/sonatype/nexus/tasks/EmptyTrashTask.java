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
package org.sonatype.nexus.tasks;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.wastebasket.Wastebasket;
import org.sonatype.nexus.proxy.repository.RepositoryTaskSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Empty trash.
 */
@Named
public class EmptyTrashTask
    extends RepositoryTaskSupport
{
  public static final int DEFAULT_OLDER_THAN_DAYS = -1;

  /**
   * The Wastebasket component.
   */
  private final Wastebasket wastebasket;

  @Inject
  public EmptyTrashTask(final Wastebasket wastebasket)
  {
    this.wastebasket = checkNotNull(wastebasket);
  }

  public int getEmptyOlderCacheItemsThan() {
    return getConfiguration().getInteger(EmptyTrashTaskDescriptor.OLDER_THAN_FIELD_ID, DEFAULT_OLDER_THAN_DAYS);
  }

  public void setEmptyOlderCacheItemsThan(int emptyOlderCacheItemsThan) {
    getConfiguration().setString(EmptyTrashTaskDescriptor.OLDER_THAN_FIELD_ID, String.valueOf(
        emptyOlderCacheItemsThan));
  }

  @Override
  protected Void execute()
      throws Exception
  {
    final String repositoryId = getConfiguration().getRepositoryId();
    if (getEmptyOlderCacheItemsThan() == DEFAULT_OLDER_THAN_DAYS) {
      if (repositoryId == null) {
        // all
        wastebasket.purgeAll();
      }
      else {
        wastebasket.purge(getRepositoryRegistry().getRepository(repositoryId));
      }
    }
    else {
      if (repositoryId == null) {
        // all
        wastebasket.purgeAll(TimeUnit.DAYS.toMillis(getEmptyOlderCacheItemsThan()));
      }
      else {
        wastebasket.purge(getRepositoryRegistry().getRepository(repositoryId),
            TimeUnit.DAYS.toMillis(getEmptyOlderCacheItemsThan()));
      }
    }
    return null;
  }

  @Override
  public String getMessage() {
    return "Emptying Trash.";
  }
}
