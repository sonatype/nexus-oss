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

package org.sonatype.nexus.repository.simple;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Responses;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.nexus.repository.view.matchers.RegexMatcher;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple content handler.
 *
 * Allows GET, PUT, DELETE to manage repository-specific named content in-memory.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ContentHandler
    extends ComponentSupport
    implements Handler
{
  private static class Content
  {
    private final String type;

    private final byte[] bytes;

    public Content(final Payload payload) throws IOException {
      checkNotNull(payload);
      this.type = payload.getContentType();
      try (InputStream input = checkNotNull(payload.openInputStream())) {
        this.bytes = ByteStreams.toByteArray(input);
      }
    }

    public Payload toPayload() {
      return new BytesPayload(type, bytes);
    }
  }

  /**
   * Map of repository-name -> [Map of content-name -> content]
   */
  private final Map<String,Map<String, Content>> allContents = Maps.newConcurrentMap();

  /**
   * Return collection of repository specific contents.
   */
  private Map<String, Content> repositoryContents(final Repository repository) {
    Map<String,Content> contents = allContents.get(repository.getName());
    if (contents == null) {
      contents = Maps.newConcurrentMap();
      allContents.put(repository.getName(), contents);
    }
    return contents;
  }

  @Nonnull
  @Override
  public Response handle(final @Nonnull Context context) throws Exception {
    String name = contentName(context);
    String method = context.getRequest().getAction();

    log.debug("{} content: {}", method, name);

    Map<String, Content> contents = repositoryContents(context.getRequest().getRepository());

    switch (method) {
      case "GET": {
        Content content = contents.get(name);
        if (content == null) {
          return Responses.notFound(name);
        }
        return new PayloadResponse(Status.success(200), content.toPayload());
      }

      case "PUT": {
        Payload payload = context.getRequest().getPayload();
        Content content = new Content(payload);
        Content prev = contents.put(name, content);
        if (prev == null) {
          return new Response(Status.success(201));
        }
        return new Response(Status.success(204));
      }

      case "DELETE": {
        Content prev = contents.remove(name);
        if (prev == null) {
          return Responses.notFound(name);
        }
        return new Response(Status.success(204));
      }

      default:
        return Responses.methodNotAllowed(method, "GET", "PUT", "DELETE");
    }
  }

  /**
   * Find content name from request context matcher state.
   */
  private String contentName(final Context context) {
    RegexMatcher.State matchState = context.require(RegexMatcher.State.class);
    return matchState.getMatchResult().group(1);
  }
}
