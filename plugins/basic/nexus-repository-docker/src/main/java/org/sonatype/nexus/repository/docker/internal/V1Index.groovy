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
package org.sonatype.nexus.repository.docker.internal

import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.docker.DockerFacet
import org.sonatype.nexus.repository.http.HttpResponses
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Response
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.sisu.goodies.common.ComponentSupport

/**
 * Index (hub like) handlers for Docker v1 protocol.
 *
 * @since 3.0
 */
@Named
@Singleton
class V1Index
    extends ComponentSupport
{

  Closure getImagesIndex = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[getImagesIndex] repository: {} tokens: {}', repository.name, state.tokens

    return addHeaders(
        repository,
        HttpResponses.ok(repository.facet(DockerFacet).getImagesIndex(
            (state.tokens['namespace'] ?: 'library') + '/' + state.tokens['repository']
        ))
    )
  }

  Closure putImagesIndex = { Context context, boolean images ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[putImagesIndex] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putImagesIndex(
        (state.tokens['namespace'] ?: 'library') + '/' + state.tokens['repository'], context.request.payload
    )

    return addHeaders(
        repository,
        images ? HttpResponses.noContent() : HttpResponses.ok()
    )
  }

  private Response addHeaders(final Repository repository, final Response response) {
    // endpoint should not have the protocol (e.g. http://)
    def url = new URL(repository.url)
    def endpoint = url.toExternalForm()
    if (url.protocol) {
      endpoint = endpoint.substring(url.protocol.length() + 3)
    }
    response.headers.set('X-Docker-Endpoints', endpoint)
    return response
  }

}
