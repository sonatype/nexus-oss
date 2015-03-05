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
package org.sonatype.nexus.repository.maven.internal.hosted;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.maven.Content;
import org.sonatype.nexus.repository.maven.internal.storage.MavenStorageFacet;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.joda.time.DateTime;

import static org.sonatype.nexus.repository.http.HttpMethods.DELETE;
import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.http.HttpMethods.PUT;
import static org.sonatype.nexus.repository.maven.internal.ContentHelper.contentName;
import static org.sonatype.nexus.repository.maven.internal.ContentHelper.toContent;
import static org.sonatype.nexus.repository.maven.internal.ContentHelper.toPayload;

/**
 * Maven hosted handler.
 *
 * @since 3.0
 */
@Singleton
@Named
public class MavenHostedHandler
    extends ComponentSupport
    implements Handler
{
  @Nonnull
  @Override
  public Response handle(final @Nonnull Context context) throws Exception {
    final String action = context.getRequest().getAction();
    final String name = contentName(context);
    final MavenStorageFacet mavenStorageFacet = context.getRepository().facet(MavenStorageFacet.class);

    switch (action) {
      case GET: {
        final Content content = mavenStorageFacet.get(name);
        if (content == null) {
          return HttpResponses.notFound(name);
        }
        return HttpResponses.ok(toPayload(content));
      }

      case PUT: {
        final Content content = toContent(context.getRequest().getPayload(), new DateTime());
        mavenStorageFacet.put(name, content);
        return HttpResponses.created();
      }

      case DELETE: {
        final boolean deleted = mavenStorageFacet.delete(name);
        if (!deleted) {
          return HttpResponses.notFound(name);
        }
        return HttpResponses.noContent();
      }

      default:
        return HttpResponses.methodNotAllowed(context.getRequest().getAction(), GET, PUT, DELETE);
    }
  }
}
