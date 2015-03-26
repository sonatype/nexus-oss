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

package org.sonatype.nexus.repository.http;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;

import com.google.common.base.Joiner;
import com.google.common.net.HttpHeaders;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.http.HttpStatus.*;

/**
 * Convenience methods for constructing various commonly used HTTP responses.
 *
 * @since 3.0
 */
public class HttpResponses
{
  private HttpResponses() {}

  // Ok: 200

  public static Response ok(final @Nullable String message) {
    return new Response(Status.success(OK, message));
  }

  public static Response ok() {
    return ok((String) null);
  }

  public static Response ok(final Payload payload) {
    return new PayloadResponse(Status.success(OK), payload);
  }

  // Created: 201

  public static Response created(final @Nullable String message) {
    return new Response(Status.success(CREATED, message));
  }

  public static Response created() {
    return created((String) null);
  }

  public static Response created(final Payload payload) {
    return new PayloadResponse(Status.success(CREATED), payload);
  }

  // No Content: 204

  public static Response noContent(final @Nullable String message) {
    return new Response(Status.success(NO_CONTENT, message));
  }

  public static Response noContent() {
    return noContent(null);
  }

  // Not Found: 404

  public static Response notFound(final @Nullable String message) {
    return new Response(Status.failure(NOT_FOUND, message));
  }

  public static Response notFound() {
    return notFound(null);
  }

  // Bad request: 400

  public static Response badRequest(final @Nullable String message) {
    return new Response(Status.failure(BAD_REQUEST, message));
  }

  public static Response badRequest() {
    return badRequest(null);
  }

  // Unauthorized: 401

  public static Response unauthorized(final @Nullable String message) {
    return new Response(Status.failure(UNAUTHORIZED, message));
  }

  public static Response unauthorized() {
    return unauthorized(null);
  }

  // Forbidden: 403

  public static Response forbidden(final @Nullable String message) {
    return new Response(Status.failure(FORBIDDEN, message));
  }

  public static Response forbidden() {
    return forbidden(null);
  }

  // Method not allowed: 405

  public static Response methodNotAllowed(final String methodName, final String... allowedMethods) {
    checkNotNull(methodName);
    checkNotNull(allowedMethods);
    checkArgument(allowedMethods.length != 0);
    Response response = new Response(Status.failure(METHOD_NOT_ALLOWED, methodName));
    String allow = Joiner.on(',').join(allowedMethods);
    response.getHeaders().set(HttpHeaders.ALLOW, allow);
    return response;
  }

  public static Response serviceUnavailable(final @Nullable String message) {
    return new Response(Status.failure(SERVICE_UNAVAILABLE, message));
  }

  public static Response serviceUnavailable() {
    return serviceUnavailable(null);
  }

  public static Response notImplemented(final @Nullable String message) {
    return new Response(Status.failure(NOT_IMPLEMENTED, message));
  }

  public static Response rangeNotSatisfiable(final long contentSize) {
    final Response response = new Response(Status.failure(REQUESTED_RANGE_NOT_SATISFIABLE));
    response.getHeaders().set(HttpHeaders.CONTENT_LENGTH, "0");
    response.getHeaders().set(HttpHeaders.CONTENT_RANGE, "bytes */" + contentSize);
    return response;
  }
}
