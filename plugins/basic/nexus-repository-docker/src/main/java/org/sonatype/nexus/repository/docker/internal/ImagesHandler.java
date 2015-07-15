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
package org.sonatype.nexus.repository.docker.internal;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static org.sonatype.nexus.repository.http.HttpMethods.DELETE;
import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD;
import static org.sonatype.nexus.repository.http.HttpMethods.MKCOL;
import static org.sonatype.nexus.repository.http.HttpMethods.PUT;

/**
 * @since 3.0
 */
@Named
@Singleton
public class ImagesHandler
    extends ComponentSupport
    implements Handler
{
  @Nonnull
  @Override
  public Response handle(final @Nonnull Context context) throws Exception {
    TokenMatcher.State state = context.getAttributes().require(TokenMatcher.State.class);
    String method = context.getRequest().getAction();
    Repository repository = context.getRepository();
    log.info("{} {} tokens: {}", method, repository.getName(), state.getTokens());

    switch (method) {
      case HEAD:
      case GET: {
        return HttpResponses.notFound();
      }
      case PUT: {
        return HttpResponses.ok();
      }
      default:
        return HttpResponses.methodNotAllowed(method, GET, HEAD, PUT, DELETE);
    }
  }

}
