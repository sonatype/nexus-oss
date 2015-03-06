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
package org.sonatype.nexus.repository.maven.internal;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.group.GroupFacet;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.maven.internal.ContextHelper.coordinates;

/**
 * Maven group handler.
 *
 * @since 3.0
 */
// TODO: this is started from copy+paste of GroupHandler, but that class as-is cannot be extended to suit this use case
@Singleton
@Named
public class MavenGroupHandler
    extends ComponentSupport
    implements Handler
{
  /**
   * Request-context state container for set of repositories already dispatched to.
   */
  private static class DispatchedRepositories
  {
    private final Set<String> dispatched = Sets.newHashSet();

    public void add(final Repository repository) {
      dispatched.add(repository.getName());
    }

    public boolean contains(final Repository repository) {
      return dispatched.contains(repository.getName());
    }

    @Override
    public String toString() {
      return dispatched.toString();
    }
  }

  @Override
  public Response handle(final @Nonnull Context context) throws Exception {
    final String action = context.getRequest().getAction();
    final Coordinates coordinates = coordinates(context);
    switch (action) {
      case GET: {
        if (coordinates.main().getFileName().equals("maven-metadata.xml")) {
          return handleMerged(context);
        }
        else {
          return handleFirstFound(context);
        }
      }

      default:
        return HttpResponses.methodNotAllowed(action, GET);
    }
  }

  @Nonnull
  private Response handleFirstFound(final @Nonnull Context context) throws Exception {
    final Request request = context.getRequest();
    final DispatchedRepositories dispatched = request.getAttributes().getOrCreate(DispatchedRepositories.class);
    final Repository repository = context.getRepository();
    final GroupFacet group = repository.facet(GroupFacet.class);
    for (Repository member : group.members()) {
      log.trace("Trying member: {}", member);

      // track repositories we have dispatched to, prevent circular dispatch for nested groups
      if (dispatched.contains(member)) {
        log.trace("Skipping already dispatched member: {}", member);
        continue;
      }
      dispatched.add(member);

      final ViewFacet view = member.facet(ViewFacet.class);
      final Response response = view.dispatch(request);
      if (response.getStatus().getCode() == HttpStatus.OK) {
        return response;
      }
    }
    return HttpResponses.notFound();
  }

  // TODO: merge and then cache in group storage (for next hashCode req)? If so, then
  // TODO: add event handler that would listen for member content changes and evict?
  // TODO: ie. group storage would contain something only if asked for, and would be safe to just nuke store anytime?
  @Nonnull
  private Response handleMerged(final @Nonnull Context context) throws Exception {
    final Request request = context.getRequest();
    final DispatchedRepositories dispatched = request.getAttributes().getOrCreate(DispatchedRepositories.class);
    final Repository repository = context.getRepository();
    final GroupFacet group = repository.facet(GroupFacet.class);
    final Map<Repository, Response> responses = Maps.newHashMap();
    for (Repository member : group.members()) {
      log.trace("Trying member: {}", member);

      // track repositories we have dispatched to, prevent circular dispatch for nested groups
      if (dispatched.contains(member)) {
        log.trace("Skipping already dispatched member: {}", member);
        continue;
      }
      dispatched.add(member);

      final ViewFacet view = member.facet(ViewFacet.class);
      final Response response = view.dispatch(request);
      if (response.getStatus().getCode() == HttpStatus.OK) {
        responses.put(member, response);
      }
    }
    if (responses.isEmpty()) {
      return HttpResponses.notFound();
    }

    // TODO: do the actual metadatamerge
    return responses.values().iterator().next();
  }
}
