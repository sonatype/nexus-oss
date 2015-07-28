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

import org.sonatype.nexus.repository.docker.DockerFacet
import org.sonatype.nexus.repository.http.HttpResponses
import org.sonatype.nexus.repository.view.Content
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Response
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.sisu.goodies.common.ComponentSupport

import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_X_DOCKER_CHECKSUM_PAYLOAD
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_X_DOCKER_SIZE

/**
 * Images handlers for Docker v1 protocol.
 *
 * @since 3.0
 */
@Named
@Singleton
class V1Images
    extends ComponentSupport
{

  Closure getLayerMetadata = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[getLayerMetadata] repository: {} tokens: {}', repository.name, state.tokens

    Content metadata = repository.facet(DockerFacet).getLayerMetadata(state.tokens['layerId'])
    Response response = HttpResponses.ok(metadata)
    copyAttributeAsHeader(metadata, response, P_X_DOCKER_SIZE)
    copyAttributeAsHeader(metadata, response, P_X_DOCKER_CHECKSUM_PAYLOAD)
    return response
  }

  Closure putLayerMetadata = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[putLayerMetadata] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putLayerMetadata(state.tokens['layerId'], context.request.payload)

    return HttpResponses.ok()
  }

  Closure getLayerContent = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[getLayerContent] repository: {} tokens: {}', repository.name, state.tokens

    return HttpResponses.ok(repository.facet(DockerFacet).getLayerContent(state.tokens['layerId']))
  }

  Closure putLayer = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[putLayerContent] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putLayer(state.tokens['layerId'], context.request.payload)

    return HttpResponses.ok()
  }

  Closure putLayerChecksum = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[putLayerChecksum] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putLayerChecksum(
        state.tokens['layerId'], (String) context.request.headers[P_X_DOCKER_CHECKSUM_PAYLOAD]
    )

    return HttpResponses.ok()
  }

  Closure getLayerAncestry = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[getLayerAncestry] repository: {} tokens: {}', repository.name, state.tokens

    return HttpResponses.ok(repository.facet(DockerFacet).getLayerAncestry(state.tokens['layerId']))
  }

  private void copyAttributeAsHeader(final Content content, final Response response, final String name) {
    def value = content.attributes.get(name)
    if (value) {
      response.headers.set(name, value as String)
    }
  }

}
