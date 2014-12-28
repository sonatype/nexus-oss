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
package org.sonatype.nexus.repository.simple.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.simple.SimpleContentFacet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link SimpleContentFacet}.
 *
 * @since 3.0
 */
@Named
public class SimpleContentFacetImpl
  extends FacetSupport
  implements SimpleContentFacet
{
  private final Map<String, SimpleContent> backing = Maps.newConcurrentMap();

  private volatile String indexHtmlCache;

  private void reset() {
    backing.clear();
    indexHtmlCache = null;
  }

  @Override
  protected void doInit(final Repository repository) throws Exception {
    reset();
  }

  @Override
  protected void doDestroy() throws Exception {
    reset();
  }

  @Override
  @Nullable
  public SimpleContent get(final String name) {
    checkNotNull(name);
    ensureStarted();
    return backing.get(name);
  }

  @Override
  @Nullable
  public SimpleContent put(final String name, final SimpleContent content) {
    checkNotNull(name);
    checkNotNull(content);
    ensureStarted();
    SimpleContent prev = backing.put(name, content);
    log.debug("{} repository '{}' content: {} -> {}",
        prev == null ? "Saved" : "Replaced",
        getRepository().getName(),
        name,
        content
    );
    invalidateIndexHtmlCache();
    return prev;
  }

  @Override
  @Nullable
  public SimpleContent delete(final String name) {
    checkNotNull(name);
    ensureStarted();
    SimpleContent prev = backing.remove(name);
    if (prev != null) {
      log.debug("Deleted repository '{}' content: {} -> {}", getRepository().getName(), name, prev);
      invalidateIndexHtmlCache();
    }
    return prev;
  }

  private void invalidateIndexHtmlCache() {
    indexHtmlCache = null;
    log.debug("Invalidated index.html cache");
  }

  @Override
  public String indexHtml() {
    ensureStarted();

    // resolve volatile to local for thread sanity
    String indexHtml = indexHtmlCache;

    // maybe generate index.html
    if (indexHtml == null) {
      // snapshot contents
      Map<String,SimpleContent> contents = ImmutableMap.copyOf(backing);

      // else build a new index.html
      String title = String.format("%s contents", getRepository().getName());

      // TODO: Replace with template to generate contents

      StringWriter buff = new StringWriter();
      PrintWriter output = new PrintWriter(buff);
      output.println("<html>");
      output.println("<head>");
      output.format("<title>%s</title>%n", title);
      output.println("</head>");
      output.println("<body>");

      Set<Map.Entry<String, SimpleContent>> entries = contents.entrySet();
      output.format("<h1>%s</h1>%n", title);
      output.format("<div><strong>Entries</strong>: %s</div>%n", contents.size());
      output.println("<hr/>");

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
        for (Map.Entry<String, SimpleContent> entry : entries) {
          String name = entry.getKey();
          SimpleContent content = entry.getValue();
          output.println("<tr>");
          output.format("<td style='text-align: left'><a href='%s'>%s</a></td>%n", name, name);
          output.format("<td style='text-align: left'>%s bytes</td>%n", content.getSize());
          output.format("<td style='text-align: left'>%s</td>%n", content.getType() != null ? content.getType() : "-");
          output.println("</tr>");
        }
        output.println("</tbody>");

        output.println("</table>");
        output.println("<hr/>");
      }

      output.println("</body>");
      output.println("</html>");
      output.flush();
      indexHtml = buff.toString();

      // cache index.html
      indexHtmlCache = indexHtml;
      log.debug("Cached index.html");
    }

    return indexHtml;
  }
}
