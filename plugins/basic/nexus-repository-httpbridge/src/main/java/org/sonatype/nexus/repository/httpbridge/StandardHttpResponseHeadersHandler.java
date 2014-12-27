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
package org.sonatype.nexus.repository.httpbridge;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Headers;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adds standard HTTP response headers:
 *
 * <ul>
 *   <li>Server</li>
 *   <li>X-Frame-Options</li>
 *   <li>X-Content-Type-Options</li>
 * </ul>
 *
 * @since 3.0
 */
@Named
@Singleton
public class StandardHttpResponseHeadersHandler
  extends ComponentSupport
  implements Handler
{
  private final String server;

  @Inject
  public StandardHttpResponseHeadersHandler(final Provider<SystemStatus> systemStatus) {
    checkNotNull(systemStatus);

    SystemStatus status = systemStatus.get();
    this.server = String.format("Nexus/%s (%s)", status.getVersion(), status.getEditionShort());
  }

  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) throws Exception {
    Response response = context.proceed();
    Headers headers = response.getHeaders();
    headers.set("Server", server);
    headers.set("X-Frame-Options", "SAMEORIGIN");
    headers.set("X-Content-Type-Options", "nosniff");
    return response;
  }
}
