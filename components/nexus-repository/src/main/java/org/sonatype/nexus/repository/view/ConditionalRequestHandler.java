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
package org.sonatype.nexus.repository.view;

import javax.annotation.Nonnull;

import org.sonatype.nexus.repository.Repository;
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
      final String action = context.getRequest().getAction();
      switch (action) {
        case GET:
        case HEAD: {
          final Response response = context.proceed();
          if (response.getStatus().isSuccessful() && !requestPredicate.apply(response)) {
            // keep all response headers like Last-Modified and ETag, etc
            return new Response.Builder()
                .copy(response)
                .status(Status.failure(NOT_MODIFIED, "Not modified"))
                .payload(null)
                .build();
          }
          return response;
        }

        case POST:
        case PUT:
        case DELETE: {
          final Response getResponse = performGet(context.getRepository(), context.getRequest());
          if (getResponse.getStatus().isSuccessful() && !requestPredicate.apply(getResponse)) {
            // keep all response headers like Last-Modified and ETag, etc
            return new Response.Builder()
                .copy(getResponse)
                .status(Status.failure(PRECONDITION_FAILED, "Precondition failed"))
                .payload(null)
                .build();
          }
        }
      }
    }

    return context.proceed();
  }

  /**
   * Performs a GET request against targeted repository with same {@link Request} parameters as incoming
   * request.
   */
  private Response performGet(final Repository repository, final Request request) throws Exception {
    return repository.facet(ViewFacet.class).dispatch(copyAsUnconditionalGet(request));
  }
}
