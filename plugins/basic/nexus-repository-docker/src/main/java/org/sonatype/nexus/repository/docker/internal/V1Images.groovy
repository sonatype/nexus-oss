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

import org.sonatype.nexus.repository.http.HttpResponses
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.sisu.goodies.common.ComponentSupport

/**
 * @since 3.0
 */
@Named
@Singleton
class V1Images
    extends ComponentSupport
{

  Closure getMetadata = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[getMetadata] repository: {} tokens: {}', repository.name, state.tokens

    return HttpResponses.notFound()
  }

  Closure putMetadata = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[putMetadata] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putMetadata(state.tokens['image'], context.request.payload)

    return HttpResponses.ok()
  }

  Closure getLayer = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    log.info '[getLayer] tokens: {}', state.tokens

    return HttpResponses.notFound()
  }

  Closure putLayer = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[putLayer] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putLayer(state.tokens['image'], context.request.payload)

    return HttpResponses.ok()
  }

  Closure putChecksum = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[putChecksum] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putChecksum(
        state.tokens['image'], (String) context.request.headers[DockerAttributes.P_X_DOCKER_CHECKSUM_PAYLOAD]
    )

    return HttpResponses.ok()
  }

  Closure getAncestry = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    log.info '[getAncestry] tokens: {}', state.tokens

    return HttpResponses.notFound()
  }

}
