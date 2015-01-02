/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.repository.simple.internal;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.httpbridge.HttpResponses;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.simple.SimpleContentFacet;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.httpbridge.HttpMethods.GET;

/**
 * Simple content group handler.
 *
 * @since 3.0
 */
@Named
@Singleton
public class SimpleGroupHandler
    extends ComponentSupport
    implements Handler
{
  @Nonnull
  @Override
  public Response handle(final @Nonnull Context context) throws Exception {
    String name = contentName(context);
    String method = context.getRequest().getAction();
    Repository repository = context.getRequest().getRepository();
    log.debug("{} repository '{}' content-name: {}", method, repository.getName(), name);

    SimpleContentFacet facet = repository.facet(SimpleContentFacet.class);

    switch (method) {
      case GET: {
        SimpleContent content = facet.get(name);
        if (content == null) {
          return HttpResponses.notFound(name);
        }
        return HttpResponses.ok(content.toPayload());
      }

      default:
        return HttpResponses.methodNotAllowed(method, GET);
    }
  }

  /**
   * Find content name from request context matcher state.
   */
  private String contentName(final Context context) {
    TokenMatcher.State state = context.getAttributes().require(TokenMatcher.State.class);
    String name = state.getTokens().get("name");
    checkState(name != null, "Missing token: name");
    return name;
  }
}
