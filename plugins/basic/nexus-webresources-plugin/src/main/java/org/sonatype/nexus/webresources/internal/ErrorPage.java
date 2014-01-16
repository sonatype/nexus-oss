/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.webresources.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.web.BaseUrlHolder;
import org.sonatype.nexus.web.DelegatingWebResource;
import org.sonatype.nexus.web.WebResource;
import org.sonatype.nexus.web.WebResource.Prepareable;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.template.TemplateEngine;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An {@code error.html} {@link WebResource} to handle generic servlet error-page dispatched requests.
 *
 * @since 2.8
 */
@Named
@Singleton
public class ErrorPage
  extends ComponentSupport
  implements WebResource, Prepareable
{
  private final ApplicationStatusSource applicationStatusSource;

  private final TemplateEngine templateEngine;

  private final Provider<HttpServletRequest> requestProvider;

  @Inject
  public ErrorPage(final ApplicationStatusSource applicationStatusSource,
                   final TemplateEngine templateEngine,
                   final Provider<HttpServletRequest> requestProvider)
  {
    this.applicationStatusSource = checkNotNull(applicationStatusSource);
    this.templateEngine = checkNotNull(templateEngine);
    this.requestProvider = checkNotNull(requestProvider);
  }

  @Override
  public String getPath() {
    return "/error.html";
  }

  @Nullable
  @Override
  public String getContentType() {
    return "text/html";
  }

  @Override
  public boolean isCacheable() {
    return false;
  }

  @Override
  public long getLastModified() {
    return System.currentTimeMillis();
  }

  @Override
  public long getSize() {
    throw new UnsupportedOperationException("Preparation required");
  }

  @Override
  public InputStream getInputStream() throws IOException {
    throw new UnsupportedOperationException("Preparation required");
  }

  private byte[] renderTemplate(final String templateName) throws IOException {
    Map<String, Object> params = Maps.newHashMap();
    params.put("nexusVersion", applicationStatusSource.getSystemStatus().getVersion());
    params.put("nexusRoot", BaseUrlHolder.get());

    HttpServletRequest request = requestProvider.get();
    params.put("request", request);
    params.put("servletName", request.getAttribute("javax.servlet.error.servlet_name"));
    params.put("requestUri", request.getAttribute("javax.servlet.error.request_uri"));
    params.put("statusCode", request.getAttribute("javax.servlet.error.status_code"));
    params.put("message", request.getAttribute("javax.servlet.error.message"));
    params.put("exceptionType", request.getAttribute("javax.servlet.error.exception_type"));
    params.put("exception", request.getAttribute("javax.servlet.error.exception"));

    URL template = getClass().getResource(templateName);
    checkState(template != null, "Missing template: %s", templateName);

    log.debug("Rendering template: {}", template);
    String content = templateEngine.render(this, template, params);

    return content.getBytes();
  }

  @Override
  public WebResource prepare() throws IOException {
    return new DelegatingWebResource(this)
    {
      private final byte[] content = renderTemplate("error.vm");

      @Override
      public long getSize() {
        return content.length;
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
      }
    };
  }
}
