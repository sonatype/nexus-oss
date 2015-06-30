/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.internal.proxy;

import java.util.List;

import javax.annotation.Nullable;

import static java.util.Arrays.asList;

/**
 * Synthesizes a count from remote and hosted component counts.
 *
 * @since 3.0
 */
public class CountReportingPolicy
{
  private CountReportingPolicy() {}

  public static int determineReportedCount(final int remoteCount, final int localCount,
                                           @Nullable final Integer pageSize, @Nullable final Integer skip)
  {
    return determineReportedCount(asList((Integer) remoteCount), localCount, pageSize, skip);
  }

  /**
   * Works out what count to show given various counts from remote proxies, and the local count (after remote metadata
   * was fetched and cached).
   */
  public static int determineReportedCount(final List<Integer> remoteCounts,
                                           final int localCount,
                                           @Nullable final Integer pageSize,
                                           @Nullable final Integer skip)
  {
    int remoteMaximum = 0;
    for (Integer remoteCount : remoteCounts) {
      remoteMaximum = Math.max(remoteMaximum, remoteCount);
    }

    if (pageSize != null && skip != null) {

      // The number of records locally available is the number returned by the local query, plus the number of records
      // we were asked to skip

      final int localResultsWeCouldDisplayOnThisPage = localCount - skip;

      if (localResultsWeCouldDisplayOnThisPage < pageSize) {
        // This means we're on the last page. We can't offer any more pages without producing a count that conflicts
        // with the number of entries we sent, so we have to report our local result set honestly.
        return localCount;
      }

      if (localCount > remoteMaximum) {
        // If we have more locally available than are remote available (probably a search impl difference), then
        // we should return the size of what we have cached
        return localCount;
      }
    }

    // Otherwise, just return the largest results size from any of the remote repos.
    return remoteMaximum;
  }
}
