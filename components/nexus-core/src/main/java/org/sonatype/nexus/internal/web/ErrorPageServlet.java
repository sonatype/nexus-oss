/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.internal.web;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.common.app.SystemStatus;
import org.sonatype.nexus.servlet.WebUtils;
import org.sonatype.sisu.goodies.template.TemplateEngine;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * An {@code error.html} servlet to handle generic servlet error-page dispatched requests.
 *
 * @since 2.8
 *
 * @see ErrorPageFilter
 */
@Singleton
public class ErrorPageServlet
    extends HttpServlet
{
  private static final Logger log = LoggerFactory.getLogger(ErrorPageServlet.class);

  /**
   * @since 3.0
   */
  private static final String ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";

  /**
   * @since 3.0
   */
  private static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";

  /**
   * @since 3.0
   */
  private static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";

  /**
   * @since 3.0
   */
  private static final String ERROR_MESSAGE = "javax.servlet.error.message";

  /**
   * @since 3.0
   */
  private static final String ERROR_EXCEPTION_TYPE = "javax.servlet.error.exception_type";

  /**
   * @since 3.0
   */
  private static final String ERROR_EXCEPTION = "javax.servlet.error.exception";

  private final TemplateEngine templateEngine;

  private final String applicationVersion;

  private final WebUtils webUtils;

  @Inject
  public ErrorPageServlet(@Named("shared-velocity") final TemplateEngine templateEngine,
                          final SystemStatus systemStatus,
                          final WebUtils webUtils)
  {
    this.templateEngine = checkNotNull(templateEngine);
    this.applicationVersion = checkNotNull(systemStatus).getVersion();
    this.webUtils = checkNotNull(webUtils);
  }

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException
  {
    webUtils.addNoCacheResponseHeaders(response);

    String servletName = (String) request.getAttribute(ERROR_SERVLET_NAME);
    String requestUri = (String) request.getAttribute(ERROR_REQUEST_URI);
    Integer errorCode = (Integer) request.getAttribute(ERROR_STATUS_CODE);
    String errorMessage = (String) request.getAttribute(ERROR_MESSAGE);
    Class causeType = (Class) request.getAttribute(ERROR_EXCEPTION_TYPE);
    Throwable cause = (Throwable) request.getAttribute(ERROR_EXCEPTION);
    String errorName = null;

    // this happens if someone browses directly to the error page
    if (errorCode == null) {
      errorCode = SC_NOT_FOUND;
      errorMessage = "Not found";
    }

    // error message must always be non-null when rendering
    if (errorMessage == null) {
      errorMessage = "Unknown error";
    }

    // ensure sanity of passed in strings which are used to render html content
    errorMessage = StringEscapeUtils.escapeHtml(errorMessage);
    if (errorName != null) {
      errorName = StringEscapeUtils.escapeHtml(errorName);
    }
    else {
      errorName = "";
    }

    response.setStatus(errorCode, errorName);
    render(response, errorCode, errorName, errorMessage);
  }

  /**
   * Render error page.
   */
  private void render(final HttpServletResponse response,
                      final int errorCode,
                      final String errorName,
                      final String errorMessage)
      throws IOException
  {
    Map<String, Object> dataModel = Maps.newHashMapWithExpectedSize(5);
    dataModel.put("nexusRoot", BaseUrlHolder.get());
    dataModel.put("urlSuffix", applicationVersion); // for cache busting
    dataModel.put("errorCode", errorCode);
    dataModel.put("errorName", errorName);
    dataModel.put("errorDescription", errorMessage);

    String html = templateEngine.render(this, "errorPageContentHtml.vm", dataModel);
    response.setContentType("text/html");
    try (PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream()))) {
      out.println(html);
    }
  }

  /**
   * Returns the message of given throwable, or if message is null will toString throwable.
   */
  private static String messageOf(final Throwable cause) {
    String message = cause.getMessage();
    if (message == null) {
      return cause.toString();
    }
    return message;
  }

  /**
   * Attach exception details to request.
   *
   * @since 3.0
   */
  static void attachCause(final HttpServletRequest request, final Throwable cause) {
    log.debug("Attaching cause", cause);
    request.setAttribute(ERROR_EXCEPTION_TYPE, cause.getClass());
    request.setAttribute(ERROR_EXCEPTION, cause);
  }
}
