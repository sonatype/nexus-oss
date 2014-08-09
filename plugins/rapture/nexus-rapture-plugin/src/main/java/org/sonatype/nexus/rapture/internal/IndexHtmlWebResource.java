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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.web.BaseUrlHolder;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides {@code /index.html}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class IndexHtmlWebResource
    extends TemplateWebResource
{
  private final Provider<HttpServletRequest> servletRequestProvider;

  @Inject
  public IndexHtmlWebResource(final Provider<HttpServletRequest> servletRequestProvider) {
    this.servletRequestProvider = checkNotNull(servletRequestProvider);
  }

  @Override
  public String getPath() {
    return "/index.html";
  }

  @Override
  public String getContentType() {
    return HTML;
  }

  @Override
  protected byte[] generate() throws IOException {
    String baseUrl = BaseUrlHolder.get();
    log.trace("Base URL: {}", baseUrl);

    boolean debug = isDebug();
    log.trace("Debug: {}", debug);

    return render("index.vm", new TemplateParameters()
        .set("baseUrl", baseUrl)
        .set("debug", debug)
    );
  }

  /**
   * Check if ?debug parameter is given on the request.
   */
  private boolean isDebug() {
    HttpServletRequest request = servletRequestProvider.get();
    String value = request.getParameter("debug");

    // not set
    if (value == null) {
      return false;
    }

    // ?debug
    if (value.trim().length() == 0) {
      return true;
    }

    // ?debug=<flag>
    return Boolean.parseBoolean(value);
  }
}
