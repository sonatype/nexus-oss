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

import org.sonatype.nexus.blobstore.api.BlobStoreManager
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.config.Configuration
import org.sonatype.nexus.repository.storage.WritePolicy
import org.sonatype.nexus.testsuite.repository.RepositoryTestSupport

import org.junit.Rule
import org.junit.rules.TestName

/**
 * Support for Docker ITs.
 */
class DockerITSupport
    extends RepositoryTestSupport
{

  @Rule
  public TestName testName = new TestName();

  //  @org.ops4j.pax.exam.Configuration
  //  public static Option[] configureNexus() {
  //    return options(
  //        NexusCoreITSupport.configureNexus(),
  //        wrappedBundle(maven("org.apache.httpcomponents", "httpmime").versionAsInProject())
  //    )
  //  }

  Repository createDockerHostedRepository() {
    return createRepository(new Configuration(
        repositoryName: testName.methodName,
        recipeName: 'docker-hosted',
        online: true,
        attributes: [
            docker : [
                port: portRegistry.reservePort()
            ],
            storage: [
                blobStoreName: BlobStoreManager.DEFAULT_BLOBSTORE_NAME,
                writePolicy  : WritePolicy.ALLOW
            ]
        ]
    ))
  }

  DockerClient createDockerClient(final Repository repository) {
    return new DockerClient(clientBuilder().build(), clientContext(), repository)
  }

}
