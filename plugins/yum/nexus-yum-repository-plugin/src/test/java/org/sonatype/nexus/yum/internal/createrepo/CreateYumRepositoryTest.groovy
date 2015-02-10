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
 * {@link CreateYumRepository} UTs.
 * @since 3.0
 */
class CreateYumRepositoryTest
extends TestSupport
{

  /**
   * Create repository for 2 parsed rpms and check results.
   */
  @Test
  void 'create repository'() {
    File ant_i386 = util.resolveFile('src/test/ut-resources/rpms/ant/1.7.1-13/ant-1.7.1-13.el6.i686.rpm')
    File ant_x86 = util.resolveFile('src/test/ut-resources/rpms/ant/1.7.1-13/ant-1.7.1-13.el6.x86_64.rpm')
    File outputDir = util.createTempDir('repodata')
    new CreateYumRepository(outputDir, 1422620943).withCloseable { CreateYumRepository writer ->
      writer.write(new YumPackageParser().parse(
          new FileInputStream(ant_i386),
          'Packages/ant-1.7.1-13.el6.i686.rpm',
          1422616782
      ))
      writer.write(new YumPackageParser().parse(
          new FileInputStream(ant_x86),
          'Packages/ant-1.7.1-13.el6.x86_64.rpm',
          1309665722
      ))
    }
    assertThat(
        IOUtils.toString(new GZIPInputStream(new FileInputStream(new File(outputDir, 'primary.xml.gz')))),
        equalToOnlyDiffs(readFileToString(util.resolveFile('src/test/ut-resources/createrepo/result/repodata/primary.xml')))
    )
    assertThat(
        IOUtils.toString(new GZIPInputStream(new FileInputStream(new File(outputDir, 'filelists.xml.gz')))),
        equalToOnlyDiffs(readFileToString(util.resolveFile('src/test/ut-resources/createrepo/result/repodata/filelists.xml')))
    )
    assertThat(
        IOUtils.toString(new GZIPInputStream(new FileInputStream(new File(outputDir, 'other.xml.gz')))),
        equalToOnlyDiffs(readFileToString(util.resolveFile('src/test/ut-resources/createrepo/result/repodata/other.xml')))
    )
    assertThat(
        readFileToString(new File(outputDir, 'repomd.xml')),
        equalToOnlyDiffs(readFileToString(util.resolveFile('src/test/ut-resources/createrepo/result/repodata/repomd.xml')))
    )
  }

  @Test
  void 'create repository with grooups'() {
    File ant_i386 = util.resolveFile('src/test/ut-resources/rpms/ant/1.7.1-13/ant-1.7.1-13.el6.i686.rpm')
    File ant_x86 = util.resolveFile('src/test/ut-resources/rpms/ant/1.7.1-13/ant-1.7.1-13.el6.x86_64.rpm')
    File comps = util.resolveFile('src/test/ut-resources/createrepo/comps.xml')
    File outputDir = util.createTempDir('repodata')
    new CreateYumRepository(outputDir, 1422620943, comps).withCloseable { CreateYumRepository writer ->
      writer.write(new YumPackageParser().parse(
          new FileInputStream(ant_i386),
          'Packages/ant-1.7.1-13.el6.i686.rpm',
          1422616782
      ))
      writer.write(new YumPackageParser().parse(
          new FileInputStream(ant_x86),
          'Packages/ant-1.7.1-13.el6.x86_64.rpm',
          1309665722
      ))
    }
    assertThat(
        readFileToString(new File(outputDir, 'repomd.xml')),
        equalToOnlyDiffs(readFileToString(util.resolveFile('src/test/ut-resources/createrepo/result/repodata/repomd-group.xml')))
    )
  }

}
