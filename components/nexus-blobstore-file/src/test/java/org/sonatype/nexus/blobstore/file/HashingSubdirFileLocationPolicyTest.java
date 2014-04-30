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
package org.sonatype.nexus.blobstore.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

public class HashingSubdirFileLocationPolicyTest
    extends TestSupport
{
  @Test
  public void blobsMustBeWellDistributedIntoFolders() {

    final HashingSubdirFileLocationPolicy policy = new HashingSubdirFileLocationPolicy();

    final Path dataDir = Paths.get(".");
    final int numberOfBlobsToStore = 100000;

    ListMultimap<Path, BlobId> locations = placeBlobsInDirectories(policy, dataDir, numberOfBlobsToStore);

    SortedSet<Integer> fileCounts = new TreeSet<Integer>();
    for (Path path : locations.keySet()) {
      fileCounts.add(locations.get(path).size());
    }

    final Integer leastFilesPerDir = fileCounts.first();
    final Integer mostFilesPerDir = fileCounts.last();

    log("Storing {} blobs produces {} data dirs each containing between {} and {} files.", numberOfBlobsToStore,
        locations.keySet().size(), leastFilesPerDir, mostFilesPerDir);

    // We want the density of file allocation to directories not to be too lumpy. The fullest directory should have
    // no more than 5 times the number of files of the least full directory.
    assertThat("peanut", mostFilesPerDir, lessThan(leastFilesPerDir * 5));

    log("Actual most/least factor: {}", (mostFilesPerDir * 1.0) / (leastFilesPerDir * 1.0));
  }

  /**
   * Simulate storing an arbitrary number of blobs in directories, and return a data structure that has each directory
   * as a key, and the contained blobs as (multi) values.
   */
  private ListMultimap<Path, BlobId> placeBlobsInDirectories(final HashingSubdirFileLocationPolicy policy,
                                                             final Path dataDir,
                                                             final int numberOfBlobsToPlace)
  {
    ListMultimap<Path, BlobId> locations = ArrayListMultimap.create();
    for (int i = 0; i < numberOfBlobsToPlace; i++) {

      final BlobId blobId = new BlobId(UUID.randomUUID().toString());

      final Path blobDirectory = policy.getBlobPath(dataDir, blobId).getParent();

      locations.put(blobDirectory, blobId);
    }
    return locations;
  }

}