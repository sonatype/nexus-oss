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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpConditions;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

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
    final Predicate<Response> requestPredicate = buildPredicate(context.getRequest());
    if (requestPredicate != null) {
      final String action = context.getRequest().getAction();
      switch (action) {
        case GET:
        case HEAD: {
          final Response response = context.proceed();
          if (response.getStatus().isSuccessful() && requestPredicate.apply(response)) {
            return response;
          }
          else {
            return new Response.Builder()
                .status(Status.failure(NOT_MODIFIED, "Not modified"))
                .build();
          }
        }

        case POST:
        case PUT:
        case DELETE: {
          final Response getResponse = performGet(context.getRepository(), context.getRequest());
          if (getResponse.getStatus().isSuccessful()) {
            if (requestPredicate.apply(getResponse)) {
              return context.proceed();
            }
            else {
              return new Response.Builder()
                  .status(Status.failure(PRECONDITION_FAILED, "Precondition failed"))
                  .build();
            }
          }
        }
      }
    }
    return context.proceed();
  }

  /**
   * Builds a {@link Predicate} out of the "if-..." headers in {@link Request}.
   */
  @Nullable
  private Predicate<Response> buildPredicate(final Request request) {
    List<Predicate<Response>> predicates = new ArrayList<>();

    final Predicate<Response> ifModifiedSince = HttpConditions.ifModifiedSince(request);
    if (ifModifiedSince != null) {
      predicates.add(ifModifiedSince);
    }
    final Predicate<Response> ifUnmodifiedSince = HttpConditions.ifUnmodifiedSince(request);
    if (ifUnmodifiedSince != null) {
      predicates.add(ifUnmodifiedSince);
    }

    final Predicate<Response> ifMatch = HttpConditions.ifMatch(request);
    if (ifMatch != null) {
      predicates.add(ifMatch);
    }
    final Predicate<Response> ifNoneMatch = HttpConditions.ifNoneMatch(request);
    if (ifNoneMatch != null) {
      predicates.add(ifNoneMatch);
    }

    if (!predicates.isEmpty()) {
      return Predicates.and(predicates);
    }
    return null;
  }

  /**
   * Performs a GET request against targeted repository with same {@link Request} parameters as incoming
   * request.
   */
  private Response performGet(final Repository repository, final Request request) throws Exception {
    return repository.facet(ViewFacet.class).dispatch(
        new Request.Builder().copy(request).action(GET).build()
    );
  }
}
