/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.repository.RepositoryTaskSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Snapshot Remover Task.
 */
@Named
public class SnapshotRemovalTask
    extends RepositoryTaskSupport<SnapshotRemovalResult>
{
  public static final int DEFAULT_MIN_SNAPSHOTS_TO_KEEP = 0;

  public static final int DEFAULT_OLDER_THAN_DAYS = -1;

  public static final int DEFAULT_GRACE_DAYS_AFTER_RELEASE = 0;

  private final SnapshotRemover snapshotRemover;

  @Inject
  public SnapshotRemovalTask(final SnapshotRemover snapshotRemover)
  {
    this.snapshotRemover = checkNotNull(snapshotRemover);
  }

  @Override
  public SnapshotRemovalResult execute()
      throws Exception
  {
    int minSnapshotsToKeep = getConfiguration()
        .getInteger(SnapshotRemovalTaskDescriptor.MIN_TO_KEEP_FIELD_ID, DEFAULT_MIN_SNAPSHOTS_TO_KEEP);
    int removeOlderThanDays = getConfiguration()
        .getInteger(SnapshotRemovalTaskDescriptor.KEEP_DAYS_FIELD_ID, DEFAULT_OLDER_THAN_DAYS);
    boolean removeIfReleaseExists = getConfiguration()
        .getBoolean(SnapshotRemovalTaskDescriptor.REMOVE_WHEN_RELEASED_FIELD_ID, false);
    int graceDaysAfterRelease = getConfiguration()
        .getInteger(SnapshotRemovalTaskDescriptor.GRACE_DAYS_AFTER_RELEASE_FIELD_ID, DEFAULT_GRACE_DAYS_AFTER_RELEASE);
    boolean deleteImmediately = getConfiguration().getBoolean(SnapshotRemovalTaskDescriptor.DELETE_IMMEDIATELY, false);

    SnapshotRemovalRequest req =
        new SnapshotRemovalRequest(getConfiguration().getRepositoryId(), minSnapshotsToKeep,
            removeOlderThanDays, removeIfReleaseExists, graceDaysAfterRelease, deleteImmediately);

    return snapshotRemover.removeSnapshots(req);
  }

  @Override
  public String getMessage() {
    if (getConfiguration().getRepositoryId() != null) {
      return "Removing snapshots from repository " + getConfiguration().getRepositoryId();
    }
    else {
      return "Removing snapshots from all registered repositories";
    }
  }

}
