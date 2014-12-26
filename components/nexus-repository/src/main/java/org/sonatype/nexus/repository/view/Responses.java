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

package org.sonatype.nexus.repository.view;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Convenience methods for constructing various commonly used responses.
 *
 * @since 3.0
 */
public class Responses
{
  /**
   * Indicates that the requested resource was not found.
   */
  public static Response notFound(final @Nullable String message) {
    return new Response(new Status(false, 404, message));
  }

  public static Response notFound() {
    return notFound(null);
  }

  /**
   * Indicates that the HTTP method is not allowed for the requested resource.
   */
  public static Response methodNotAllowed() {
    return new Response(new Status(false, 405, null));
  }

  public static Response methodNotAllowed(final String methodName, final String... allowedMethods) {
    checkNotNull(methodName);
    checkNotNull(allowedMethods);
    checkArgument(allowedMethods.length != 0);
    Response response = new Response(Status.failure(405, methodName));
    String allow = Joiner.on(',').join(allowedMethods);
    response.getHeaders().set("Allow", allow);
    return response;
  }
}
