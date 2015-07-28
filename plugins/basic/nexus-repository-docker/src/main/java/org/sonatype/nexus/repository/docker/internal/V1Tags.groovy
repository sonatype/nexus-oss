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
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.sisu.goodies.common.ComponentSupport

import com.google.common.net.HttpHeaders

/**
 * Tag handlers for Docker v1 protocol.
 *
 * @since 3.0
 */
@Named
@Singleton
class V1Tags
    extends ComponentSupport
{
  def final static USER_AGENT_PATTERN = ~/([^\s\/]+)\/([^\s\/]+)/

  Closure getTag = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[getTag] repository: {} tokens: {}', repository.name, state.tokens

    return HttpResponses.ok(repository.facet(DockerFacet).getTag(
        (state.tokens['namespace'] ?: 'library') + '/' + state.tokens['repository'], state.tokens['tag']
    ))
  }

  Closure putTag = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[putTag] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putTag(
        (state.tokens['namespace'] ?: 'library') + '/' + state.tokens['repository'], state.tokens['tag'],
        parseUserAgent(context),
        context.request.payload
    )

    return HttpResponses.ok()
  }

  protected static Map<String, String> parseUserAgent(Context context) {
    def userAgent = [:]
    if (context.request.headers[HttpHeaders.USER_AGENT]) {
      def userAgentEntries = [:]
      USER_AGENT_PATTERN.matcher(context.request.headers[HttpHeaders.USER_AGENT] as String).each {
        userAgentEntries[it[1]] = it[2]
      }
      if (userAgentEntries['docker']) {
        userAgent[DockerAttributes.P_DOCKER_VERSION] = userAgentEntries['docker']
      }
      if (userAgentEntries['go']) {
        userAgent[DockerAttributes.P_DOCKER_GO_VERSION] = userAgentEntries['go']
      }
      [DockerAttributes.P_ARCH, DockerAttributes.P_KERNEL, DockerAttributes.P_OS].each {
        if (userAgentEntries[it]) {
          userAgent[it] = userAgentEntries[it]
        }
      }
    }
    return userAgent
  }

  Closure getTags = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.info '[getTags] repository: {} tokens: {}', repository.name, state.tokens

    return HttpResponses.ok(repository.facet(DockerFacet).getTags(
        (state.tokens['namespace'] ?: 'library') + '/' + state.tokens['repository']
    ))
  }

}
