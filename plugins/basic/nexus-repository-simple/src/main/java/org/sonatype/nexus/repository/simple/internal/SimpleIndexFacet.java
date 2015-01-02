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
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.simple.SimpleContentCreatedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentDeletedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentEvent;
import org.sonatype.nexus.repository.simple.SimpleContentFacet;
import org.sonatype.nexus.repository.simple.SimpleContentUpdatedEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.template.TemplateEngine;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple {@code index.html} facet.
 *
 * @since 3.0
 */
@Named
@Facet.Exposed
public class SimpleIndexFacet
    extends FacetSupport
{
  private final EventBus eventBus;

  private final TemplateEngine templateEngine;

  private final URL template;

  private volatile String htmlCache;

  @Inject
  public SimpleIndexFacet(final EventBus eventBus,
                          final @Named("shared") TemplateEngine templateEngine)
  {
    this.eventBus = checkNotNull(eventBus);
    this.templateEngine = checkNotNull(templateEngine);

    // resolve template
    template = getClass().getResource("index.vm");
    checkNotNull(template);
    log.debug("Template: {}", template);
  }

  @Override
  protected void doInit(final Repository repository) throws Exception {
    htmlCache = null;
  }

  @Override
  protected void doStart() throws Exception {
    eventBus.register(this);
  }

  @Override
  protected void doStop() throws Exception {
    eventBus.unregister(this);
  }

  @Override
  protected void doDestroy() throws Exception {
    htmlCache = null;
  }

  // TODO: refactor to allow more flexible passing of contents for hosted/proxy and group
  // TODO: group shouldn't have a content-facet, so we'll need to expose the entries directly not the facet

  public String get(final Set<Entry<String, SimpleContent>> entries) {
    checkNotNull(entries);
    ensureStarted();

    // resolve volatile to local for thread sanity
    String html = htmlCache;

    // maybe generate index.html
    if (html == null) {
      html = templateEngine.render(this, template, new TemplateParameters()
          .set("repository", getRepository())
          .set("contents", entries));

      // cache index.html
      htmlCache = html;
      log.trace("Cached index.html");
    }

    return html;
  }

  public String get() {
    SimpleContentFacet content = getRepository().facet(SimpleContentFacet.class);
    return get(content.entries());
  }

  public void invalidate() {
    htmlCache = null;
    log.trace("Invalided");
  }

  //
  // Events
  //

  /**
   * Invalidate index cache when contents change.
   */
  private void maybeInvalidate(final SimpleContentEvent event) {
    // TODO: object equality probably is fine here?
    if (event.getRepository().getName().equals(getRepository().getName())) {
      invalidate();
    }
  }

  @Subscribe
  public void on(final SimpleContentCreatedEvent event) {
    maybeInvalidate(event);
  }

  @Subscribe
  public void on(final SimpleContentUpdatedEvent event) {
    maybeInvalidate(event);
  }

  @Subscribe
  public void on(final SimpleContentDeletedEvent event) {
    maybeInvalidate(event);
  }
}
