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
package org.sonatype.nexus.maven.tasks;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.scheduling.RepositoryTaskSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.maven.tasks.UnusedSnapshotRemoverTaskDescriptor.DAYS_SINCE_LAST_REQUESTED_FIELD_ID;

/**
 * Unused Snapshot Remover Task.
 *
 * @since 2.7.0
 */
@Named
public class UnusedSnapshotRemoverTask
    extends RepositoryTaskSupport<SnapshotRemovalResult>
{
  private final SnapshotRemover snapshotRemover;

  @Inject
  public UnusedSnapshotRemoverTask(final SnapshotRemover snapshotRemover)
  {
    this.snapshotRemover = checkNotNull(snapshotRemover);
  }

  @Override
  public SnapshotRemovalResult execute()
      throws Exception
  {
    int daysSinceLastRequested = getConfiguration().getInteger(DAYS_SINCE_LAST_REQUESTED_FIELD_ID, -1);
    checkArgument(daysSinceLastRequested > 0);
    final SnapshotRemovalRequest req = new SnapshotRemovalRequest(
        getConfiguration().getRepositoryId(),
        -1,                             // not applicable (minCountOfSnapshotsToKeep)
        daysSinceLastRequested,
        false,                          // do not remove if release available
        -1,                             // not applicable (graceDaysAfterRelease)
        false,                          // do not delete immediately (will move to trash),
        true                            // calculate number of days based on last time snapshot was requested
    );

    return snapshotRemover.removeSnapshots(req);
  }

  @Override
  protected String getMessage() {
    if (getConfiguration().getRepositoryId() != null) {
      return "Removing unused snapshots from repository " + getConfiguration().getRepositoryId();
    }
    else {
      return "Removing unused snapshots from all registered repositories";
    }
  }

}
