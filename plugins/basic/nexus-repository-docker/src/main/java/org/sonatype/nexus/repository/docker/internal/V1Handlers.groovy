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
package org.sonatype.nexus.repository.docker.internal

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.docker.DockerFacet
import org.sonatype.nexus.repository.http.HttpResponses
import org.sonatype.nexus.repository.http.HttpStatus
import org.sonatype.nexus.repository.search.SearchService
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.view.Content
import org.sonatype.nexus.repository.view.ContentTypes
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Response
import org.sonatype.nexus.repository.view.Status
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.nexus.repository.view.payloads.StringPayload
import org.sonatype.sisu.goodies.common.ComponentSupport

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Charsets
import com.google.common.net.HttpHeaders
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder

import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_X_DOCKER_CHECKSUM_PAYLOAD
import static org.sonatype.nexus.repository.docker.internal.DockerAttributes.P_X_DOCKER_SIZE

/**
 * Docker v1 protocol handlers.
 *
 * @since 3.0
 */
@Named
@Singleton
class V1Handlers
    extends ComponentSupport
{

  def final static USER_AGENT_PATTERN = ~/([^\s\/]+)\/([^\s\/]+)/

  private final static ObjectMapper mapper = new ObjectMapper()

  private final static sorting = [
      SortBuilders.fieldSort(StorageFacet.P_NAME).order(SortOrder.ASC),
      SortBuilders.fieldSort(StorageFacet.P_VERSION).order(SortOrder.ASC)
  ]

  @Inject
  SearchService searchService

  Closure getImagesIndex = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.debug '[getImagesIndex] repository: {} tokens: {}', repository.name, state.tokens

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
    log.debug '[putImagesIndex] repository: {} tokens: {}', repository.name, state.tokens

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

  Closure getTag = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.debug '[getTag] repository: {} tokens: {}', repository.name, state.tokens

    return HttpResponses.ok(repository.facet(DockerFacet).getTag(
        (state.tokens['namespace'] ?: 'library') + '/' + state.tokens['repository'], state.tokens['tag']
    ))
  }

  Closure putTag = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.debug '[putTag] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putTag(
        (state.tokens['namespace'] ?: 'library') + '/' + state.tokens['repository'], state.tokens['tag'],
        parseUserAgent(context),
        context.request.payload
    )

    return HttpResponses.ok()
  }

  Closure getTags = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.debug '[getTags] repository: {} tokens: {}', repository.name, state.tokens

    return HttpResponses.ok(repository.facet(DockerFacet).getTags(
        (state.tokens['namespace'] ?: 'library') + '/' + state.tokens['repository']
    ))
  }

  Closure getLayerMetadata = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.debug '[getLayerMetadata] repository: {} tokens: {}', repository.name, state.tokens

    Content metadata = repository.facet(DockerFacet).getLayerMetadata(state.tokens['layerId'])
    Response response = HttpResponses.ok(metadata)
    copyAttributeAsHeader(metadata, response, P_X_DOCKER_SIZE)
    copyAttributeAsHeader(metadata, response, P_X_DOCKER_CHECKSUM_PAYLOAD)
    return response
  }

  Closure putLayerMetadata = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.debug '[putLayerMetadata] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putLayerMetadata(state.tokens['layerId'], context.request.payload)

    return HttpResponses.ok()
  }

  Closure getLayerContent = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.debug '[getLayerContent] repository: {} tokens: {}', repository.name, state.tokens

    return HttpResponses.ok(repository.facet(DockerFacet).getLayerContent(state.tokens['layerId']))
  }

  Closure putLayer = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.debug '[putLayerContent] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putLayerContent(state.tokens['layerId'], context.request.payload)

    return HttpResponses.ok()
  }

  Closure putLayerChecksum = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.debug '[putLayerChecksum] repository: {} tokens: {}', repository.name, state.tokens

    repository.facet(DockerFacet).putLayerChecksum(
        state.tokens['layerId'], (String) context.request.headers[P_X_DOCKER_CHECKSUM_PAYLOAD]
    )

    return HttpResponses.ok()
  }

  Closure getLayerAncestry = { Context context ->
    def state = context.attributes.require(TokenMatcher.State)
    def repository = context.repository
    log.debug '[getLayerAncestry] repository: {} tokens: {}', repository.name, state.tokens

    return HttpResponses.ok(repository.facet(DockerFacet).getLayerAncestry(state.tokens['layerId']))
  }

  Closure search = { Context context ->
    def query = context.request.parameters.get('q')
    log.debug '[search] query: {}', query

    def searchResults = new SearchResults(query: query, num_results: 0)
    if (query) {
      def queryBuilder = QueryBuilders.filteredQuery(
          QueryBuilders.queryStringQuery(query),
          FilterBuilders.termFilter(StorageFacet.P_FORMAT, DockerFormat.NAME)
      )
      def searchResponse = searchService.search(queryBuilder, sorting, 0, 100)
      def hits = searchResponse.hits.hits
      if (hits) {
        def host = context.request.headers.get(HttpHeaders.HOST)
        searchResults.num_results = hits.length
        searchResults.results = hits?.collect { hit ->
          new SearchResult(
              name: "${host}/${hit.source[StorageFacet.P_NAME]}:${hit.source[StorageFacet.P_VERSION]}"
          )
        }
      }
    }

    return HttpResponses.ok(
        new StringPayload(mapper.writeValueAsString(searchResults), Charsets.UTF_8, ContentTypes.APPLICATION_JSON)
    )
  }

  Closure getUsers = { Context context ->
    return HttpResponses.ok('OK')
  }

  Closure createUser = { Context context ->
    // send 401 that will force docker into sending a get with basic auth
    return HttpResponses.unauthorized()
  }

  Closure updateUser = { Context context ->
    return HttpResponses.notImplemented('Not Implemented')
  }

  Closure oldDocker = {
    throw new V1Exception(
        'This endpoint does not support Docker daemons older than 0.10', HttpStatus.PRECONDITION_FAILED
    )
  }

  Closure v1Exception = { Context context ->
    try {
      return context.proceed();
    }
    catch (V1Exception e) {
      log.warn("Error: {} {}: {} - {}",
          context.getRequest().getAction(),
          context.getRequest().getPath(),
          e.getHttpCode(),
          e.toString());

      return new Response.Builder()
          .status(Status.failure(e.getHttpCode()))
          .payload(new StringPayload(e.getMessage(), ContentTypes.TEXT_PLAIN))
          .build();
    }
  }

  private void copyAttributeAsHeader(final Content content, final Response response, final String name) {
    def value = content.attributes.get(name)
    if (value) {
      response.headers.set(name, value as String)
    }
  }

  private static class SearchResults
  {
    String query

    int num_results

    List<SearchResult> results
  }

  private static class SearchResult
  {
    int star_count

    boolean is_official

    String name

    boolean is_trusted

    boolean is_automated

    String description
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

}
