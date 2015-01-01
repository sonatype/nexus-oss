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

import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.simple.SimpleContentFacet;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.template.TemplateEngine;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple content {@code index.html} cache.
 *
 * @since 3.0
 */
@Named
public class IndexHtmlCache
    extends ComponentSupport
{
  private final TemplateEngine templateEngine;

  private final URL template;

  private volatile String htmlCache;

  @Inject
  public IndexHtmlCache(final @Named("shared") TemplateEngine templateEngine) {
    this.templateEngine = checkNotNull(templateEngine);

    // resolve template
    template = getClass().getResource("index.vm");
    checkNotNull(template);
    log.debug("Template: {}", template);
  }

  public String get(final Repository repository, final SimpleContentFacet facet) {
    checkNotNull(repository);
    checkNotNull(facet);

    // resolve volatile to local for thread sanity
    String html = htmlCache;

    // maybe generate index.html
    if (html == null) {
      html = templateEngine.render(this, template, new TemplateParameters()
          .set("repository", repository)
          .set("contents", facet.entries()));

      // cache index.html
      htmlCache = html;
      log.trace("Cached index.html");
    }

    return html;
  }

  public void invalidate() {
    htmlCache = null;
    log.trace("Invalided");
  }
}
