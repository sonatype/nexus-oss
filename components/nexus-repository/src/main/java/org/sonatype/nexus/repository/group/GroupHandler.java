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

package org.sonatype.nexus.repository.group;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Sets;

import static org.sonatype.nexus.repository.http.HttpMethods.GET;

/**
 * Group handler.
 *
 * @since 3.0
 */
@Named
@Singleton
public class GroupHandler
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

  @Nonnull
  @Override
  public Response handle(final @Nonnull Context context) throws Exception {
    Request request = context.getRequest();

    // Lookup/create dispatched repositories in request-context
    DispatchedRepositories dispatched = request.getAttributes().getOrCreate(DispatchedRepositories.class);

    Repository repository = context.getRepository();
    GroupFacet group = repository.facet(GroupFacet.class);

    // FIXME: To move forward prototype, exposing HTTP semantics here
    String method = request.getAction();
    switch (method) {
      case GET: {
        for (Repository member : group.members()) {
          log.trace("Trying member: {}", member);

          // track repositories we have dispatched to, prevent circular dispatch for nested groups
          if (dispatched.contains(member)) {
            log.trace("Skipping already dispatched member: {}", member);
            continue;
          }
          dispatched.add(member);

          ViewFacet view = member.facet(ViewFacet.class);
          Response response = view.dispatch(request);
          if (response.getStatus().getCode() == HttpStatus.OK) {
            return response;
          }
        }
        return HttpResponses.notFound();
      }

      default:
        return HttpResponses.methodNotAllowed(method, GET);
    }
  }
}
