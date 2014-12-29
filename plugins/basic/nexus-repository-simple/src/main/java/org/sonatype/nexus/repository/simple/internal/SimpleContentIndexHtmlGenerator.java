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
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.template.TemplateEngine;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple content {@code index.html} generator.
 *
 * @since 3.0
 */
@Named
@Singleton
public class SimpleContentIndexHtmlGenerator
    extends ComponentSupport
{
  private final TemplateEngine templateEngine;

  private URL template;

  @Inject
  public SimpleContentIndexHtmlGenerator(final @Named("shared") TemplateEngine templateEngine) {
    this.templateEngine = checkNotNull(templateEngine);

    // resolve template
    template = getClass().getResource("index.vm");
    checkNotNull(template);
    log.debug("Template: {}", template);
  }

  public String generate(final Repository repository, final Set<Entry<String, SimpleContent>> contents) {
    return templateEngine.render(this, template, new TemplateParameters()
        .set("repository", repository)
        .set("contents", contents));
  }
}
