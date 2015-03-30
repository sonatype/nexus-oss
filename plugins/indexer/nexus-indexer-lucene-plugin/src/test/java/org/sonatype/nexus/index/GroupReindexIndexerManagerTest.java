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
package org.sonatype.nexus.index;

import java.io.File;
import java.net.URL;

import org.sonatype.nexus.proxy.repository.GroupRepository;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GroupReindexIndexerManagerTest
    extends AbstractIndexerManagerTest
{

  @Test
  @Ignore("This test is flakey, too often failing, ignoring for now to avoid the churn")
  public void testGroupReindex()
      throws Exception
  {
    fillInRepo();

    GroupRepository group = (GroupRepository) repositoryRegistry.getRepository("public");

    File groupRoot = new File(new URL(group.getLocalUrl()).toURI());
    File index = new File(groupRoot, ".index");

    File indexFile = new File(index, "nexus-maven-repository-index.gz");
    File incrementalIndexFile = new File(index, "nexus-maven-repository-index.1.gz");

    assertFalse("No index .gz file should exist.", indexFile.exists());
    assertFalse("No incremental chunk should exists.", incrementalIndexFile.exists());

    indexerManager.reindexRepository(null, group.getId(), true);

    assertTrue("Index .gz file should exist.", indexFile.exists());
    assertFalse("No incremental chunk should exists.", incrementalIndexFile.exists());

    // copy some _new_ stuff, not found in any of the members
    File sourceApacheSnapshotsRoot = new File(getBasedir(), "src/test/resources/reposes/apache-snapshots-2");
    File snapshotsRoot = new File(new URL(snapshots.getLocalUrl()).toURI());
    FileUtils.copyDirectory(sourceApacheSnapshotsRoot, snapshotsRoot, HiddenFileFilter.VISIBLE);
    indexerManager.reindexRepository(null, group.getId(), false);

    assertTrue("Index .gz file should exist.", indexFile.exists());
    assertTrue("Incremental chunk should exists.", incrementalIndexFile.exists());

    assertTrue("We expected less than 300 bytes but got " + incrementalIndexFile.length(),
        incrementalIndexFile.length() < 300);

  }
}
