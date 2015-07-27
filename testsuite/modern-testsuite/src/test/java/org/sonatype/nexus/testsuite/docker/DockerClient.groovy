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

import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.docker.RepositoryReverseProxyFacet
import org.sonatype.nexus.testsuite.repository.FormatClientSupport

import org.apache.http.client.HttpClient
import org.apache.http.client.protocol.HttpClientContext

/**
 * Docker Client.
 */
class DockerClient
    extends FormatClientSupport
{

  DockerClient(final HttpClient httpClient,
               final HttpClientContext httpClientContext,
               final Repository repository)
  {
    super(httpClient, httpClientContext, dockerUri(repository))
  }

  private static URI dockerUri(final Repository repository) {
    def port = repository.facet(RepositoryReverseProxyFacet).getPort()
    return new URI("http://localhost:${port}")
  }

}
