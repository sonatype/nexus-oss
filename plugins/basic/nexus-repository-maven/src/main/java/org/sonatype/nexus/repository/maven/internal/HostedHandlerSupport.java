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

import java.io.IOException;
import java.util.Collections;

import javax.annotation.Nonnull;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.time.Clock;
import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.hash.HashCode;
import org.joda.time.DateTime;

import static org.sonatype.nexus.repository.http.HttpMethods.DELETE;
import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.http.HttpMethods.PUT;
import static org.sonatype.nexus.repository.maven.internal.MavenPayload.ok;

/**
 * Maven hosted handler base.
 *
 * @since 3.0
 */
public abstract class HostedHandlerSupport<C extends Path>
    extends ComponentSupport
    implements Handler
{
  private final Clock clock = new Clock();

  @Nonnull
  @Override
  public Response handle(final @Nonnull Context context) throws Exception {
    final C path;
    try {
      path = getPath(context);
    }
    catch (IllegalArgumentException e) {
      return HttpResponses.badRequest(e.getMessage());
    }
    final String action = context.getRequest().getAction();
    switch (action) {
      case GET: {
        final MavenPayload content = get(context, path);
        if (content == null) {
          return HttpResponses.notFound(path.getPath());
        }
        return ok(content);
      }

      case PUT: {
        try {
          final DateTime now = clock.dateTime();
          final MavenPayload mavenPayload = new MavenPayload(
              context.getRequest().getPayload(),
              now,
              now,
              Collections.<HashAlgorithm, HashCode>emptyMap()
          );
          put(context, path, mavenPayload);
          return HttpResponses.created();
        }
        catch (InvalidContentException e) {
          return HttpResponses.badRequest(e.getMessage());
        }
      }

      case DELETE: {
        final boolean deleted = delete(context, path);
        if (!deleted) {
          return HttpResponses.notFound(path.getPath());
        }
        return HttpResponses.noContent();
      }

      default:
        return HttpResponses.methodNotAllowed(context.getRequest().getAction(), GET, PUT, DELETE);
    }
  }

  protected abstract C getPath(final Context context) throws IllegalArgumentException;

  protected abstract MavenPayload get(final Context context, final C path) throws IOException;

  protected abstract void put(final Context context, final C path, final MavenPayload payload)
      throws IOException, InvalidContentException;

  protected abstract boolean delete(final Context context, final C path) throws IOException;
}
