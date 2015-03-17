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
package org.sonatype.nexus.repository.httpbridge.internal;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.httpbridge.DefaultHttpResponseSender;
import org.sonatype.nexus.repository.httpbridge.HttpResponseSender;
import org.sonatype.nexus.repository.httpbridge.internal.describe.Description;
import org.sonatype.nexus.repository.httpbridge.internal.describe.DescriptionRenderer;
import org.sonatype.nexus.repository.httpbridge.internal.describe.DescriptionUtils;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.nexus.web.BaseUrlHolder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Repository view servlet.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ViewServlet
    extends HttpServlet
{
  private static final Logger log = LoggerFactory.getLogger(ViewServlet.class);

  private final RepositoryManager repositoryManager;

  private final Map<String, HttpResponseSender> responseSenders;

  private final DescriptionRenderer descriptionRenderer;

  private final DefaultHttpResponseSender defaultHttpResponseSender;

  @Inject
  public ViewServlet(final RepositoryManager repositoryManager,
                     final Map<String, HttpResponseSender> responseSenders,
                     final DefaultHttpResponseSender defaultHttpResponseSender,
                     final DescriptionRenderer descriptionRenderer)
  {

    this.repositoryManager = checkNotNull(repositoryManager);
    this.responseSenders = checkNotNull(responseSenders);
    this.descriptionRenderer = checkNotNull(descriptionRenderer);
    this.defaultHttpResponseSender = checkNotNull(defaultHttpResponseSender);
  }

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    log.info("Initialized");
  }

  @Override
  public void destroy() {
    super.destroy();
    log.info("Destroyed");
  }

  @Override
  protected void service(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
      throws ServletException, IOException
  {
    String uri = httpRequest.getRequestURI();
    if (httpRequest.getQueryString() != null) {
      uri = uri + "?" + httpRequest.getQueryString();
    }

    if (log.isDebugEnabled()) {
      log.debug("Servicing: {} {} ({})", httpRequest.getMethod(), uri, httpRequest.getRequestURL());
    }

    MDC.put(getClass().getName(), uri);
    try {
      doService(httpRequest, httpResponse);
      log.debug("Service completed");
    }
    catch (Exception e) {
      log.warn("Service failure", e);
      Throwables.propagateIfPossible(e, ServletException.class, IOException.class);
      throw new ServletException(e);
    }
    finally {
      MDC.remove(getClass().getName());
    }
  }

  protected void doService(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
      throws Exception
  {
    // resolve repository for request
    RepositoryPath path = path(httpRequest);
    if (path == null) {
      send(HttpResponses.badRequest("Invalid repository path"), httpResponse);
      return;
    }
    log.debug("Parsed path: {}", path);

    Repository repo = repository(path.getRepositoryName());
    if (repo == null) {
      send(HttpResponses.notFound("Repository not found"), httpResponse);
      return;
    }
    log.debug("Repository: {}", repo);

    ViewFacet facet = repo.facet(ViewFacet.class);
    if (!facet.isOnline()) {
      send(HttpResponses.serviceUnavailable("Repository offline"), httpResponse);
      return;
    }
    log.debug("Dispatching to view facet: {}", facet);

    // Dispatch the request
    final HttpRequestAdapter request = new HttpRequestAdapter(httpRequest, path.getRemainingPath());
    dispatchAndSend(request, facet, sender(repo), httpResponse);
  }

  @VisibleForTesting
  void dispatchAndSend(final Request request,
                       final ViewFacet facet,
                       final HttpResponseSender sender,
                       final HttpServletResponse httpResponse)
      throws Exception
  {
    Response response = null;
    Exception failure = null;
    try {
      response = facet.dispatch(request);
    }
    catch (Exception e) {
      failure = e;
    }

    if (request.getParameters().contains("describe")) {
      send(describe(request, response, failure), httpResponse);
    }
    else {
      if (failure != null) {
        throw failure;
      }
      log.debug("HTTP response sender: {}", sender);
      sender.send(response, httpResponse);
    }
  }

  @VisibleForTesting
  Response describe(final Request request, final Response response, final Exception exception) {
    final Description description = new Description(ImmutableMap.<String, Object>of(
        "path", request.getPath(),
        "nexusUrl", BaseUrlHolder.get()
    ));
    if (exception != null) {
      DescriptionUtils.describeException(description, exception);
    }
    DescriptionUtils.describeRequest(description, request);
    if (response != null) {
      DescriptionUtils.describeResponse(description, response);
    }
    return toHtmlResponse(description);
  }

  /**
   * Send with default sender.
   *
   * Needed in a few places _before_ we have a repository instance to determine its specific sender.
   */
  @VisibleForTesting
  void send(final Response response, final HttpServletResponse httpResponse)
      throws ServletException, IOException
  {
    defaultHttpResponseSender.send(response, httpResponse);
  }

  /**
   * @return a parsed repository path, or {@code null} if parsing was impossible.
   */
  @Nullable
  private RepositoryPath path(final HttpServletRequest httpRequest) {
    String pathInfo = httpRequest.getPathInfo();
    RepositoryPath path = RepositoryPath.parse(pathInfo);
    if (path == null) {
      log.debug("Unable to parse repository path from: {}", pathInfo);
    }
    return path;
  }

  /**
   * @return the named repository or {@code null}
   */
  @Nullable
  private Repository repository(final String name) {
    log.debug("Looking for repository: {}", name);
    return repositoryManager.get(name);
  }

  /**
   * Find sender for repository format.
   *
   * If no format-specific sender is configured, the default is used.
   */
  private HttpResponseSender sender(final Repository repository) {
    String format = repository.getFormat().getValue();
    log.debug("Looking for HTTP response sender: {}", format);
    HttpResponseSender sender = responseSenders.get(format);
    if (sender == null) {
      return defaultHttpResponseSender;
    }
    return sender;
  }

  private Response toHtmlResponse(final Description description) {
    final String html = descriptionRenderer.renderHtml(description);
    return HttpResponses.ok(new StringPayload(html, Charsets.UTF_8, "text/html"));
  }
}
