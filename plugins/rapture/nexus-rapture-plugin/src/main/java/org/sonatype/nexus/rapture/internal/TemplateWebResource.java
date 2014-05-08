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
package org.sonatype.nexus.rapture.internal;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;

import org.sonatype.nexus.webresources.GeneratedWebResource;
import org.sonatype.nexus.webresources.WebResource;
import org.sonatype.sisu.goodies.template.TemplateEngine;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

// TODO: Move to common location (core or webresources plugin)

/**
 * Support for template-based {@link WebResource} implementations.
 *
 * @since 3.0
 */
public abstract class TemplateWebResource
    extends GeneratedWebResource
{
  private TemplateEngine templateEngine;

  @Inject
  public void setTemplateEngine(final TemplateEngine templateEngine) {
    this.templateEngine = checkNotNull(templateEngine);
  }

  protected TemplateEngine getTemplateEngine() {
    checkState(templateEngine != null);
    return templateEngine;
  }

  protected URL template(final String name) {
    URL template = getClass().getResource(name);
    checkState(template != null, "Missing template: %s", name);
    return template;
  }

  protected byte[] render(final String template, final TemplateParameters parameters) throws IOException {
    return getTemplateEngine().render(this, template(template), parameters).getBytes();
  }
}
