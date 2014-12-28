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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.httpbridge.HttpResponses;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.httpbridge.HttpMethods.GET;
import static org.sonatype.nexus.repository.view.ContentTypes.TEXT_HTML;

/**
 * Simple {@code index.html} handler.
 *
 * @since 3.0
 */
@Named
@Singleton
public class SimpleIndexHandler
    extends ComponentSupport
    implements Handler
{
  private final SimpleContentService contentService;

  @Inject
  public SimpleIndexHandler(final SimpleContentService contentService) {
    this.contentService = checkNotNull(contentService);
  }

  @Nonnull
  @Override
  public Response handle(final @Nonnull Context context) throws Exception {
    String method = context.getRequest().getAction();
    Repository repository = context.getRequest().getRepository();
    log.debug("{} repository '{}' index", method, repository.getName());

    switch (method) {
      case GET: {
        // TODO: Replace with template to generate contents

        String title = String.format("%s contents", repository.getName());

        StringWriter buff = new StringWriter();
        PrintWriter output = new PrintWriter(buff);
        output.println("<html>");
        output.println("<head>");
        output.format("<title>%s</title>%n", title);
        output.println("</head>");
        output.println("<body>");

        Set<Map.Entry<String,SimpleContent>> entries = contentService.entries(repository);
        output.format("<h1>%s</h1>%n", title);
        output.format("Entries: %s", entries.size());
        output.print("<hr/>");

        if (!entries.isEmpty()) {
          output.println("<table style='width: 100%'>");

          output.println("<thead>");
          output.println("<tr>");
          output.println("<th style='text-align: left'>Name</th>");
          output.println("<th style='text-align: left'>Size</th>");
          output.println("<th style='text-align: left'>Type</th>");
          output.println("</tr>");
          output.println("</thead>");

          output.println("<tbody>");
          for (Map.Entry<String,SimpleContent> entry : entries) {
            String name = entry.getKey();
            SimpleContent content = entry.getValue();
            output.println("<tr>");
            output.format("<td style='text-align: left'><a href='%s'>%s</a></td>%n", name, name);
            output.format("<td style='text-align: left'>%s bytes</td>", content.getSize());
            output.format("<td style='text-align: left'>%s</td>", content.getType() != null ? content.getType() : "-");
            output.println("</tr>");
          }
          output.println("</tbody>");

          output.println("</table>");
          output.print("<hr/>");
        }

        output.println("</body>");
        output.println("</html>");
        output.flush();

        return HttpResponses.ok(new StringPayload(buff.toString(), TEXT_HTML));
      }

      default:
        return HttpResponses.methodNotAllowed(method, GET);
    }
  }
}
