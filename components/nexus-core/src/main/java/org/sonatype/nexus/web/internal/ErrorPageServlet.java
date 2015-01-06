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
package org.sonatype.nexus.web.internal;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.web.BaseUrlHolder;
import org.sonatype.nexus.web.ErrorStatusException;
import org.sonatype.nexus.web.TemplateRenderer;
import org.sonatype.nexus.web.TemplateRenderer.TemplateLocator;
import org.sonatype.nexus.web.WebUtils;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringEscapeUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * An {@code error.html} servlet to handle generic servlet error-page dispatched requests.
 *
 * @since 2.8
 *
 * @see ErrorPageFilter
 * @see ErrorStatusException
 */
@Singleton
public class ErrorPageServlet
    extends HttpServlet
{
  /**
   * @since 3.0
   */
  public static final String ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";

  /**
   * @since 3.0
   */
  public static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";

  /**
   * @since 3.0
   */
  public static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";

  /**
   * @since 3.0
   */
  public static final String ERROR_MESSAGE = "javax.servlet.error.message";

  /**
   * @since 3.0
   */
  public static final String ERROR_EXCEPTION_TYPE = "javax.servlet.error.exception_type";

  /**
   * @since 3.0
   */
  public static final String ERROR_EXCEPTION = "javax.servlet.error.exception";

  private final TemplateRenderer templateRenderer;

  private final String applicationVersion;

  private final WebUtils webUtils;

  private final TemplateLocator templateLocator;

  @Inject
  public ErrorPageServlet(final TemplateRenderer templateRenderer,
                          final SystemStatus systemStatus,
                          final WebUtils webUtils)
  {
    this.templateRenderer = checkNotNull(templateRenderer);
    this.applicationVersion = checkNotNull(systemStatus).getVersion();
    this.webUtils = checkNotNull(webUtils);

    this.templateLocator = templateRenderer.template(
        "/org/sonatype/nexus/web/internal/errorPageContentHtml.vm",
        getClass().getClassLoader());
  }

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException
  {
    webUtils.addNoCacheResponseHeaders(response);

    //String servletName = (String) request.getAttribute(ERROR_SERVLET_NAME);
    //String requestUri = (String) request.getAttribute(ERROR_REQUEST_URI);
    Integer errorCode = (Integer) request.getAttribute(ERROR_STATUS_CODE);
    String errorMessage = (String) request.getAttribute(ERROR_MESSAGE);
    //Class causeType = (Class) request.getAttribute(ERROR_EXCEPTION_TYPE);
    Throwable cause = (Throwable) request.getAttribute(ERROR_EXCEPTION);
    String errorName = null;

    // Handle customization of error from exception details
    if (cause instanceof ErrorStatusException) {
      ErrorStatusException e = (ErrorStatusException) cause;
      errorCode = e.getResponseCode();
      errorName = e.getReasonPhrase();
      errorMessage = messageOf(e);
    }

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
      errorName = errorMessage; // already sanitized above
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

    templateRenderer.render(templateLocator, dataModel, response);
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
   * @since 3.0
   */
  static void attachCause(final HttpServletRequest request, final Throwable cause) {
    request.setAttribute(ERROR_EXCEPTION_TYPE, cause.getClass());
    request.setAttribute(ERROR_EXCEPTION, cause);
  }
}
