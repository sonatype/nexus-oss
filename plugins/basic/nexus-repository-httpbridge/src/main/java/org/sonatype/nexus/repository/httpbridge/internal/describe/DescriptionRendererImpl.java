/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.httpbridge.internal.describe;

import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.common.app.SystemStatus;
import org.sonatype.sisu.goodies.template.TemplateEngine;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link DescriptionRenderer}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class DescriptionRendererImpl
    implements DescriptionRenderer
{
  private static final String TEMPLATE_RESOURCE = "describe.vm";

  private final Provider<SystemStatus> systemStatus;

  private final TemplateEngine templateEngine;

  private final URL template;

  private final ObjectMapper objectMapper;

  @Inject
  public DescriptionRendererImpl(final Provider<SystemStatus> systemStatus,
                                 final @Named("shared-velocity") TemplateEngine templateEngine)
  {
    this.systemStatus = checkNotNull(systemStatus);
    this.templateEngine = checkNotNull(templateEngine);
    template = getClass().getResource(TEMPLATE_RESOURCE);
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Override
  public String renderHtml(final Description description) {
    SystemStatus status = systemStatus.get();
    TemplateParameters params = new TemplateParameters();
    params.setAll(description.getParameters());
    params.set("nexusVersion", status.getVersion());
    params.set("nexusEdition", status.getEditionShort());
    params.set("nexusUrl", BaseUrlHolder.get());
    params.set("items", description.getItems());
    params.set("esc", new EscapeHelper());
    return templateEngine.render(this, template, params);
  }

  @Override
  public String renderJson(final Description description) {
    try {
      return objectMapper.writeValueAsString(description);
    }
    catch (JsonProcessingException e) {
      throw Throwables.propagate(e);
    }
  }
}
