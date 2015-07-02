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
package org.sonatype.nexus.repository.view.handlers;

import javax.annotation.Nonnull;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Predicate;

import static org.sonatype.nexus.repository.http.HttpConditions.copyAsUnconditionalGet;
import static org.sonatype.nexus.repository.http.HttpConditions.requestPredicate;
import static org.sonatype.nexus.repository.http.HttpMethods.DELETE;
import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD;
import static org.sonatype.nexus.repository.http.HttpMethods.POST;
import static org.sonatype.nexus.repository.http.HttpMethods.PUT;
import static org.sonatype.nexus.repository.http.HttpStatus.NOT_MODIFIED;
import static org.sonatype.nexus.repository.http.HttpStatus.PRECONDITION_FAILED;

/**
 * A format-neutral handler for conditional requests.
 *
 * @since 3.0
 */
public class ConditionalRequestHandler
    extends ComponentSupport
    implements Handler
{
  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) throws Exception {
    final Predicate<Response> requestPredicate = requestPredicate(context.getRequest());
    if (requestPredicate != null) {
      return handleConditional(context, requestPredicate);
    }

    return context.proceed();
  }

  @Nonnull
  private Response handleConditional(@Nonnull final Context context,
                                     @Nonnull final Predicate<Response> requestPredicate) throws Exception
  {
    final String action = context.getRequest().getAction();
    log.debug("Conditional request: {} {}: {}",
        action,
        context.getRequest().getPath(),
        requestPredicate);
    switch (action) {
      case GET:
      case HEAD: {
        final Response response = context.proceed();
        // keep all response headers like Last-Modified and ETag, etc
        if (response.getStatus().isSuccessful() && !requestPredicate.apply(response)) {
          return new Response.Builder()
              .copy(response)
              .status(Status.failure(NOT_MODIFIED))
              .payload(null)
              .build();
        }
        else {
          return response;
        }
      }

      case POST:
      case PUT:
      case DELETE: {
        final Response response = context.getRepository().facet(ViewFacet.class).dispatch(
            copyAsUnconditionalGet(context.getRequest()));
        if (response.getStatus().isSuccessful() && !requestPredicate.apply(response)) {
          // keep all response headers like Last-Modified and ETag, etc
          return new Response.Builder()
              .copy(response)
              .status(Status.failure(PRECONDITION_FAILED))
              .payload(null)
              .build();
        }
        else {
          return context.proceed();
        }
      }

      default:
        return context.proceed();
    }
  }
}
