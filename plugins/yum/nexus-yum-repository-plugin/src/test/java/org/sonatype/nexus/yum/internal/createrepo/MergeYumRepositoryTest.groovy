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
package org.sonatype.nexus.yum.internal.createrepo

import org.apache.commons.io.IOUtils
import org.junit.Test
import org.sonatype.sisu.litmus.testsupport.TestSupport

import java.util.zip.GZIPInputStream

import static org.apache.commons.io.FileUtils.readFileToString
import static org.hamcrest.MatcherAssert.assertThat
import static org.sonatype.sisu.litmus.testsupport.hamcrest.DiffMatchers.equalToOnlyDiffs

/**
 * {@link MergeYumRepository} UTs.
 * @since 3.0
 */
class MergeYumRepositoryTest
extends TestSupport
{

  /**
   * Merge two yum repositories and check results.
   */
  @Test
  void 'merge repositories'() {
    File outputDir = util.createTempDir('repodata')
    new MergeYumRepository(outputDir, 1422624764).withCloseable { MergeYumRepository writer ->
      writer.merge(util.resolveFile('src/test/ut-resources/mergerepo/repo1'))
      writer.merge(util.resolveFile('src/test/ut-resources/mergerepo/repo2'))
    }
    assertThat(
        readFileToString(new File(outputDir, 'repomd.xml')),
        equalToOnlyDiffs(readFileToString(util.resolveFile('src/test/ut-resources/mergerepo/result/repodata/repomd.xml')))
    )
    assertThat(
        IOUtils.toString(new GZIPInputStream(new FileInputStream(new File(outputDir, 'primary.xml.gz')))),
        equalToOnlyDiffs(readFileToString(util.resolveFile('src/test/ut-resources/mergerepo/result/repodata/primary.xml')))
    )
    assertThat(
        IOUtils.toString(new GZIPInputStream(new FileInputStream(new File(outputDir, 'filelists.xml.gz')))),
        equalToOnlyDiffs(readFileToString(util.resolveFile('src/test/ut-resources/mergerepo/result/repodata/filelists.xml')))
    )
    assertThat(
        IOUtils.toString(new GZIPInputStream(new FileInputStream(new File(outputDir, 'other.xml.gz')))),
        equalToOnlyDiffs(readFileToString(util.resolveFile('src/test/ut-resources/mergerepo/result/repodata/other.xml')))
    )
  }

}
