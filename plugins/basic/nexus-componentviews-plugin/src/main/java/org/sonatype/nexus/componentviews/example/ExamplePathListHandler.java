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
package org.sonatype.nexus.componentviews.example;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.componentviews.ViewResponse;
import org.sonatype.nexus.componentviews.responses.Responses;

import static org.sonatype.nexus.componentviews.responses.Responses.created;
import static org.sonatype.nexus.componentviews.responses.Responses.deleted;
import static org.sonatype.nexus.componentviews.responses.Responses.html;
import static org.sonatype.nexus.componentviews.responses.Responses.notFound;

/**
 * This example handler is stateful because it uses in-memory persistence; in practice this would be rare or
 * prohibited.
 *
 * @since 3.0
 */
public class ExamplePathListHandler
    implements Handler
{
  private final Map<String, Date> entries = new ConcurrentHashMap<>();

  @Override
  public ViewResponse handle(final ViewRequest req) {
    final String requestPath = req.getPath();

    switch (req.getMethod()) {
      case GET:
        SortedSet<String> paths = new TreeSet<String>();
        for (String path : entries.keySet()) {
          if (path.startsWith(requestPath)) {
            paths.add(path);
          }
        }

        final StringBuilder s = new StringBuilder();
        s.append("<html><body>");
        s.append("<ul>");
        for (String path : paths) {
          s.append("<li>");
          s.append(path).append(" ").append(new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z").format(
              entries.get(path)));
          s.append("</li>");
        }
        s.append("</ul>");

        s.append("</body></html>");

        return html(s.toString());

      case PUT:
        entries.put(requestPath, new Date());

        return created();

      case DELETE:
        final Date removed = entries.remove(requestPath);
        if (removed == null) {
          return notFound(null);
        }
        return deleted();

      default:
        return Responses.methodNotAllowed();
    }
  }

  private ByteArrayInputStream toInputStream(
      final String string)
  {
    return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
  }
}
