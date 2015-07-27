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
package org.sonatype.nexus.testsuite.docker

import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.matchSha1

/**
 * Docker Hosted ITs.
 */
class DockerHostedIT
    extends DockerITSupport
{

  @Test
  void pushAndPull() {
    def repository = createDockerHostedRepository()
    def client = createDockerClient(repository)
    def workDir = testIndex.getDirectory('docker')

    client.push('hello-world')
    client.pull('hello-world', workDir)
    [
        '511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158',
        '31cbccb51277105ba3ae35ce33c22b69c9e3f1002e76e4c736a2e8ebff9d7b5d',
        'e45a5af57b00862e5ef5782a9925979a02ba2b12dff832fd0991335f4a11e5c5'
    ].each { layerId ->
      ['json', 'layer', 'checksum'].each { type ->
        def expected = testData.resolveFile("/v1/images/${layerId}/${type}")
        def actual = new File(workDir, "/v1/images/${layerId}/${type}")
        assertThat(actual, matchSha1(expected))
      }
    }
  }

}
