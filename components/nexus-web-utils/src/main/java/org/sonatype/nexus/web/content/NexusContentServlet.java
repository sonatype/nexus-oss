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

package org.sonatype.nexus.web.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageEOFException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RemoteStorageTransportOverloadedException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.nexus.web.Constants;
import org.sonatype.nexus.web.RemoteIPFinder;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.io.ByteStreams;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Clean-room re-implementation of {@code /content} resource, using plain Servlet API.
 * 
 * @since 2.7.0
 */
@Singleton
@Named
public class NexusContentServlet
    extends HttpServlet
{
  /**
   * Buffer size to be used when pushing content to the {@link HttpServletResponse#getOutputStream()} stream. Default is
   * 16KB.
   */
  private static final int BUFFER_SIZE = SystemPropertiesHelper.getInteger(NexusContentServlet.class.getName()
      + ".BUFFER_SIZE", -1);

  /**
   * A flag setting what should be done if request path retrieval gets a {@link StorageLinkItem} here. If {@code true},
   * this servlet dereference the link (using {@link RepositoryRouter#dereferenceLink(StorageLinkItem)} method), and
   * send proper content to client (ie. client will be "unaware" it actually stepped on link, and content it gets is
   * coming from different path than asked, or even different repository). If {@code false}, a link
   * item will actually send {@link HttpServletResponse#SC_FOUND}, and client might (or might not) follow
   * the redirection to get the linked item.
   */
  private static final boolean DEREFERENCE_LINKS = SystemPropertiesHelper.getBoolean(
      NexusContentServlet.class.getName() + ".DEREFERENCE_LINKS", true);

  /**
   * Stopwatch that is started when {@link ResourceStoreRequest} is created and stopped when request processing returns
   * from {@link RepositoryRouter}.
   */
  private static final String STOPWATCH_KEY = NexusContentServlet.class.getName() + ".stopwatch";

  private final Logger logger = LoggerFactory.getLogger(NexusContentServlet.class);

  private final RepositoryRouter repositoryRouter;
  private final Renderer renderer;
  private final GlobalRestApiSettings globalRestApiSettings;
  private final String serverString;

  @Inject
  public NexusContentServlet(final RepositoryRouter repositoryRouter, final Renderer renderer,
      final GlobalRestApiSettings globalRestApiSettings, final ApplicationStatusSource applicationStatusSource)
  {
    this.repositoryRouter = checkNotNull(repositoryRouter);
    this.renderer = checkNotNull(renderer);
    this.globalRestApiSettings = checkNotNull(globalRestApiSettings);
    this.serverString = "Nexus/" + checkNotNull(applicationStatusSource).getSystemStatus().getVersion();
    logger.debug("bufferSize={}, dereferenceLinks={}", BUFFER_SIZE, DEREFERENCE_LINKS);
  }

  /**
   * Creates a {@link ResourceStoreRequest} out from a {@link HttpServletRequest}.
   */
  protected ResourceStoreRequest getResourceStoreRequest(final HttpServletRequest request) {
    String resourceStorePath = request.getPathInfo();
    if (Strings.isNullOrEmpty(resourceStorePath)) {
      resourceStorePath = "/";
    }
    final ResourceStoreRequest result = new ResourceStoreRequest(resourceStorePath);
    result.getRequestContext().put(STOPWATCH_KEY, new Stopwatch().start());

    // honor the local only and remote only
    final Map<String, String[]> parameterMap = request.getParameterMap();
    result.setRequestLocalOnly(isLocal(request, resourceStorePath));
    result.setRequestRemoteOnly(parameterMap.containsKey(Constants.REQ_QP_IS_REMOTE_PARAMETER));
    result.setRequestAsExpired(parameterMap.containsKey(Constants.REQ_QP_AS_EXPIRED_PARAMETER));
    result.setExternal(true);

    // honor if-modified-since
    final long ifModifiedSince = request.getDateHeader("if-modified-since");
    if (ifModifiedSince > -1) {
      result.setIfModifiedSince(ifModifiedSince);
    }

    // honor if-none-match
    String ifNoneMatch = request.getHeader("if-none-match");
    if (!Strings.isNullOrEmpty(ifNoneMatch)) {
      // shave off quotes if needed (RFC specifies quotes as must)
      if (ifNoneMatch.startsWith("\"") && ifNoneMatch.endsWith("\"")) {
        ifNoneMatch = ifNoneMatch.substring(1, ifNoneMatch.length() - 1);
      }
      // we have the ETag here, shaved from quotes
      // still, WHAT we have here is basically what client sent (should be what Nx sent once to client, and client
      // cached it), see method doGetFile that will basically handle the if-none-match condition.
      result.setIfNoneMatch(ifNoneMatch);
    }

    // stuff in the originating remote address
    result.getRequestContext().put(AccessManager.REQUEST_REMOTE_ADDRESS, RemoteIPFinder.findIP(request));

    // stuff in the user id if we have it in request
    final Subject subject = SecurityUtils.getSubject();
    if (subject != null && subject.getPrincipal() != null) {
      result.getRequestContext().put(AccessManager.REQUEST_USER, subject.getPrincipal().toString());
    }
    result.getRequestContext().put(AccessManager.REQUEST_AGENT, request.getHeader("user-agent"));

    // this is HTTPS, get the cert and stuff it too for later
    if (request.isSecure()) {
      result.getRequestContext().put(AccessManager.REQUEST_CONFIDENTIAL, Boolean.TRUE);
      final List<X509Certificate> certs = Arrays.asList((X509Certificate[]) request
          .getAttribute("javax.servlet.request.X509Certificate"));
      if (certs != null && !certs.isEmpty()) {
        result.getRequestContext().put(AccessManager.REQUEST_CERTIFICATES, certs);
      }
    }

    // put the incoming URLs
    result.setRequestAppRootUrl(getAppRootUrl(request));
    result.setRequestUrl(request.getRequestURL().toString());
    return result;
  }

  /**
   * Calculates the "application root" URL, as seen by client (from {@link HttpServletRequest} made by it), or, if
   * "force base URL" configuration is set, to that URL.
   */
  protected String getAppRootUrl(final HttpServletRequest request) {
    final StringBuilder result = new StringBuilder();
    if (globalRestApiSettings.isEnabled() && globalRestApiSettings.isForceBaseUrl()
        && !Strings.isNullOrEmpty(globalRestApiSettings.getBaseUrl())) {
      result.append(globalRestApiSettings.getBaseUrl());
    }
    else {
      String appRoot = request.getRequestURL().toString();
      final String pathInfo = request.getPathInfo();
      if (!Strings.isNullOrEmpty(pathInfo)) {
        appRoot = appRoot.substring(0, appRoot.length() - pathInfo.length());
      }
      final String servletPath = request.getServletPath();
      if (!Strings.isNullOrEmpty(servletPath)) {
        appRoot = appRoot.substring(0, appRoot.length() - servletPath.length());
      }
      result.append(appRoot);
    }
    if (!result.toString().endsWith("/")) {
      result.append("/");
    }
    return result.toString();
  }

  /**
   * Request is "local" (should tackle local storage only, not generate any remote request at any cause) if client asks
   * for it, or, a request is made for a collection (path ends with slash).
   */
  protected boolean isLocal(final HttpServletRequest request, final String resourceStorePath) {
    // check do we need local only access
    boolean isLocal = request.getParameterMap().containsKey(Constants.REQ_QP_IS_LOCAL_PARAMETER);
    if (!Strings.isNullOrEmpty(resourceStorePath)) {
      // overriding isLocal is we know it will be a collection
      isLocal = isLocal || resourceStorePath.endsWith(RepositoryItemUid.PATH_SEPARATOR);
    }
    return isLocal;
  }

  protected boolean isDescribeRequest(final HttpServletRequest request) {
    return request.getParameterMap().containsKey(Constants.REQ_QP_IS_DESCRIBE_PARAMETER);
  }

  protected void handleException(final HttpServletRequest request, final HttpServletResponse response,
      final ResourceStoreRequest rsr, final Exception exception) throws IOException
  {
    if (exception instanceof LocalStorageEOFException) {
      // in case client drops connection, this makes not much sense, as he will not
      // receive this response, but we have to end it somehow.
      // but, in case when remote proxy peer drops connection on us regularly
      // this makes sense
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
    else if (exception instanceof IllegalArgumentException) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    else if (exception instanceof RemoteStorageTransportOverloadedException) {
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
    else if (exception instanceof RepositoryNotAvailableException) {
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
    else if (exception instanceof IllegalRequestException) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    else if (exception instanceof IllegalOperationException) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    else if (exception instanceof UnsupportedStorageOperationException) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    else if (exception instanceof NoSuchRepositoryException) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
    else if (exception instanceof NoSuchResourceStoreException) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
    else if (exception instanceof ItemNotFoundException) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
    else if (exception instanceof AccessDeniedException) {
      request.setAttribute(Constants.ATTR_KEY_REQUEST_IS_AUTHZ_REJECTED, Boolean.TRUE);
      return;
      // Note: we must ensure response is not committed, hence, no error page is rendered
      // this attribute above will cause filter to either 403 if
      // current user is non anonymous, or 401 and challenge if user is anonymous
    }
    else {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      logger.warn(exception.getMessage(), exception);
    }
    renderer.renderErrorPage(request, response, rsr, exception);
  }

  /**
   * To be added to non-content responses, like collection rendered index page of describe response is.
   * 
   * @see <a href="https://issues.sonatype.org/browse/NEXUS-5155">NEXUS-5155</a>
   */
  protected void addNoCacheResponseHeaders(final HttpServletResponse response) {
    // NEXUS-5155 Force browsers to not cache this page
    response.setHeader("Pragma", "no-cache"); // HTTP/1.0
    response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"); // HTTP/1.1
    response.setHeader("Cache-Control", "post-check=0, pre-check=0"); // MS IE
    response.setHeader("Expires", "0"); // No caching on Proxies in between client and Nexus
  }

  // service

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException
  {
    response.setHeader("Server", serverString);
    response.setHeader("Accept-Ranges", "bytes");
    super.service(request, response);
  }

  // GET

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException
  {
    final ResourceStoreRequest rsr = getResourceStoreRequest(request);
    try {
      try {
        StorageItem item = repositoryRouter.retrieveItem(rsr);
        if (item instanceof StorageLinkItem) {
          final StorageLinkItem link = (StorageLinkItem) item;
          if (DEREFERENCE_LINKS) {
            item = dereferenceLink(link);
          }
          else {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.addHeader("Location", getLinkTargetUrl(link));
            return;
          }
        }
        ((Stopwatch) rsr.getRequestContext().get(STOPWATCH_KEY)).stop();
        if (isDescribeRequest(request)) {
          doGetDescribe(request, response, rsr, item, null);
        }
        else if (item instanceof StorageFileItem) {
          doGetFile(request, response, (StorageFileItem) item);
        }
        else if (item instanceof StorageCollectionItem) {
          doGetCollection(request, response, (StorageCollectionItem) item);
        }
        else {
          // this should never happen, but still
          throw new ServletException("Item type " + item.getClass() + " unsupported!");
        }
      }
      catch (ItemNotFoundException e) {
        ((Stopwatch) rsr.getRequestContext().get(STOPWATCH_KEY)).stop();
        if (isDescribeRequest(request)) {
          doGetDescribe(request, response, rsr, null, e);
        }
        else {
          throw e;
        }
      }
    }
    catch (Exception e) {
      handleException(request, response, rsr, e);
    }
  }

  /**
   * Dereferences the passed in link completely (following link-to-links too) as long as non-link item is found as
   * target. This method will detect cycles, and will fail if such link constellation is found. If any target during
   * dereference is not found, the usual {@link ItemNotFoundException} will be thrown (by the method used to
   * dereference).
   */
  protected StorageItem dereferenceLink(final StorageLinkItem link) throws Exception {
    final List<String> hops = Lists.newArrayList();
    StorageLinkItem currentLink = link;
    while (true) {
      final String hop = currentLink.getRepositoryItemUid().getKey();
      if (!hops.contains(hop)) {
        hops.add(hop);
        final StorageItem item = repositoryRouter.dereferenceLink(currentLink);
        if (!(item instanceof StorageLinkItem)) {
          return item;
        }
        else {
          currentLink = (StorageLinkItem) item;
        }
      }
      else {
        // cycle detected, current link already processed
        throw new ItemNotFoundException(ItemNotFoundException.reasonFor(link.getResourceStoreRequest(), link
            .getRepositoryItemUid().getRepository(),
            "Link item %s introduced a cycle while referencing it, cycle is %s", link.getRepositoryItemUid(), hops));
      }
    }
  }

  /**
   * Creates absolute URL (as String) of the passed link's target. To be used in "Location" header of the redirect
   * message, for example.
   */
  protected String getLinkTargetUrl(final StorageLinkItem link) {
    final RepositoryItemUid targetUid = link.getTarget();
    // TODO: fix this chum
    return link.getResourceStoreRequest().getRequestAppRootUrl() + "content/repositories/"
        + targetUid.getRepository().getId() + targetUid.getPath();
  }

  /**
   * Handles a file response, all the conditional request cases, and eventually the content serving of the file item.
   */
  protected void doGetFile(final HttpServletRequest request, final HttpServletResponse response,
      final StorageFileItem file) throws IOException
  {
    // ETag, in "shaved" form of {SHA1{e5c244520e897865709c730433f8b0c44ef271f1}} (without quotes)
    // or null if file does not have SHA1 (like Virtual) or generated items (as their SHA1 would correspond to template,
    // not to actual generated content).
    final String etag;
    if (!file.isContentGenerated() && !file.isVirtual()
        && file.getRepositoryItemAttributes().containsKey(StorageFileItem.DIGEST_SHA1_KEY)) {
      etag = "{SHA1{" + file.getRepositoryItemAttributes().get(StorageFileItem.DIGEST_SHA1_KEY) + "}}";
      // tag header ETag: "{SHA1{e5c244520e897865709c730433f8b0c44ef271f1}}", quotes are must by RFC
      response.setHeader("ETag", "\"" + etag + "\"");
    }
    else {
      etag = null;
    }
    // content-type
    response.setHeader("Content-Type", file.getMimeType());

    // last-modified
    response.setDateHeader("Last-Modified", file.getModified());

    // content-length, if known
    if (file.getLength() != ContentLocator.UNKNOWN_LENGTH) {
      // Note: response.setContentLength Servlet API method uses ints (max 2GB file)!
      // TODO: apparently, some Servlet containers follow serlvet API and assume
      // contents can have 2GB max, so even this workaround below in inherently unsafe.
      // Jetty is checked, and supports this (uses long internally), but unsure for other containers
      response.setHeader("Content-Length", String.valueOf(file.getLength()));
    }

    // handle conditional GETs only for "static" content, actual content stored, not generated
    if (!file.isContentGenerated() && file.getResourceStoreRequest().getIfModifiedSince() != 0
        && file.getModified() <= file.getResourceStoreRequest().getIfModifiedSince()) {
      // this is a conditional GET using time-stamp
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }
    else if (!file.isContentGenerated() && file.getResourceStoreRequest().getIfNoneMatch() != null && etag != null
        && file.getResourceStoreRequest().getIfNoneMatch().equals(etag)) {
      // this is a conditional GET using ETag
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }
    else {
      // NEXUS-5023 disable IE for sniffing into response content
      response.setHeader("X-Content-Type-Options", "nosniff");

      final List<Range<Long>> ranges = getRequestedRanges(request, file.getLength());

      // pour the content, but only if needed (this method will be called even for HEAD reqs, but with content tossed
      // away), so be conservative as getting input stream involves locking etc, is expensive
      final boolean contentNeeded = "GET".equalsIgnoreCase(request.getMethod());
      if (ranges.isEmpty()) {
        if (contentNeeded) {
          try (final InputStream in = file.getInputStream()) {
            sendContent(in, response);
          }
        }
      }
      else if (ranges.size() > 1) {
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        renderer.renderErrorPage(request, response, file.getResourceStoreRequest(), new UnsupportedOperationException(
            "Multiple ranges not yet supported!"));
      }
      else {
        final Range<Long> range = ranges.get(0);
        if (!isRequestedRangeSatisfiable(file, range)) {
          response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
          response.setHeader("Content-Length", "0");
          response.setHeader("Content-Range", "bytes */" + file.getLength());
          return;
        }
        final long bodySize = range.upperEndpoint() - range.lowerEndpoint();
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Length", String.valueOf(bodySize));
        response.setHeader("Content-Range",
            range.lowerEndpoint() + "-" + range.upperEndpoint() + "/" + file.getLength());
        if (contentNeeded) {
          try (final InputStream in = file.getInputStream()) {
            in.skip(range.lowerEndpoint());
            sendContent(ByteStreams.limit(in, bodySize), response);
          }
        }
      }
    }
  }

  /**
   * Handles collection response, either redirects (to same URL but appended with slash, if request does not end with
   * slash), or renders the "index page" out of collection entries.
   */
  protected void doGetCollection(final HttpServletRequest request, final HttpServletResponse response,
      final StorageCollectionItem coll) throws Exception
  {
    if (!coll.getResourceStoreRequest().getRequestUrl().endsWith("/")) {
      response.setStatus(HttpServletResponse.SC_FOUND);
      response.addHeader("Location", coll.getResourceStoreRequest().getRequestUrl() + "/");
      return;
    }
    // last-modified
    response.setDateHeader("Last-Modified", coll.getModified());
    if ("HEAD".equalsIgnoreCase(request.getMethod())) {
      // do not perform coll.list(), very expensive, just give what we already know
      return;
    }
    // send no cache headers, as any of these responses should not be cached, ever
    addNoCacheResponseHeaders(response);
    // perform fairly expensive operation of fetching children from Nx
    final Collection<StorageItem> children = coll.list();
    // render the page
    renderer.renderCollection(request, response, coll, children);
  }

  /**
   * Describe response, giving out meta-information about request, found item (if any) and so on.
   */
  protected void doGetDescribe(final HttpServletRequest request, final HttpServletResponse response,
      final ResourceStoreRequest rsr, final StorageItem item, final Exception e) throws IOException
  {
    // send no cache headers, as any of these responses should not be cached, ever
    addNoCacheResponseHeaders(response);
    renderer.renderRequestDescription(request, response, rsr, item, e);
  }

  // PUT

  @Override
  protected void doPut(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException
  {
    final ResourceStoreRequest rsr = getResourceStoreRequest(request);
    try {
      final Map<String, String> userAttributes = getUserAttributesFromRequest(request);
      repositoryRouter.storeItem(rsr, request.getInputStream(), userAttributes);
      ((Stopwatch) rsr.getRequestContext().get(STOPWATCH_KEY)).stop();
      response.setStatus(HttpServletResponse.SC_CREATED);
    }
    catch (Exception e) {
      ((Stopwatch) rsr.getRequestContext().get(STOPWATCH_KEY)).stop();
      handleException(request, response, rsr, e);
    }
  }

  /**
   * Gathers "attribute" (probably set by client performing upload) from request, such might be query parameters, extra
   * headers, or such.
   */
  protected Map<String, String> getUserAttributesFromRequest(final HttpServletRequest request) {
    final Map<String, String> result = Maps.newHashMap();
    // TODO: something like grab some query parameters?
    return result;
  }

  // DELETE

  @Override
  protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException
  {
    final ResourceStoreRequest rsr = getResourceStoreRequest(request);
    try {
      repositoryRouter.deleteItem(rsr);
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      ((Stopwatch) rsr.getRequestContext().get(STOPWATCH_KEY)).stop();
    }
    catch (Exception e) {
      ((Stopwatch) rsr.getRequestContext().get(STOPWATCH_KEY)).stop();
      handleException(request, response, rsr, e);
    }
  }

  // ==

  /**
   * Parses the "Range" header of the HTTP request and builds up a list of {@link Range}. If no range header found, or
   * any problem occurred during parsing it (ie. is malformed), empty collection is returned.
   * 
   * @return list of {@link Range}, never {@code null}.
   */
  protected List<Range<Long>> getRequestedRanges(final HttpServletRequest request, final long contentLength) {
    // TODO: Current limitation: only one Range of bytes supported in forms of "-X", "X-Y" (where X<Y) and "X-".
    final String rangeHeader = request.getHeader("Range");
    if (!Strings.isNullOrEmpty(rangeHeader)) {
      try {
        if (rangeHeader.startsWith("bytes=") && rangeHeader.length() > 6 && !rangeHeader.contains(",")) {
          // Range: bytes=500-999 (from 500th byte to 999th)
          // Range: bytes=500- (from 500th byte to the end)
          // Range: bytes=-999 (from 0th byte to the 999th byte, not by RFC but widely supported)
          final String rangeValue = rangeHeader.substring(6, rangeHeader.length());
          if (rangeValue.startsWith("-")) {
            return Collections.singletonList(Range.closed(0L, Long.parseLong(rangeValue.substring(1))));
          }
          else if (rangeValue.endsWith("-")) {
            return Collections.singletonList(Range.closed(
                Long.parseLong(rangeValue.substring(0, rangeValue.length() - 1)), contentLength));
          }
          else if (rangeValue.contains("-")) {
            final String[] parts = rangeValue.split("-");
            return Collections.singletonList(Range.closed(Long.parseLong(parts[0]), Long.parseLong(parts[1])));
          }
          else {
            logger.info("Malformed HTTP Range value: {}, ignoring it", rangeHeader);
          }
        }
        else {
          logger.info(
              "Nexus does not support non-byte or multiple HTTP Ranges, sending complete content: Range value {}",
              rangeHeader);
        }
      }
      catch (Exception e) {
        if (logger.isDebugEnabled()) {
          logger.info("Problem parsing Range value: {}, ignoring it", rangeHeader, e);
        }
        else {
          logger.info("Problem parsing Range value: {}, ignoring it", rangeHeader);
        }
      }
    }
    return Collections.emptyList();
  }

  /**
   * Returns {@code true} if the {@link Range} is applicable to file (file full closed range encloses passed in range).
   */
  protected boolean isRequestedRangeSatisfiable(final StorageFileItem file, final Range<Long> range) {
    return Range.closed(0L, file.getLength()).encloses(range);
  }

  /**
   * Sends content by copying all bytes from the input stream to the output stream while setting the preferred buffer
   * size. At the end, it flushes response buffer.
   * <p>
   * Inspired from {@link ByteStreams#copy(InputStream, OutputStream)} (version 14.0.1) to expose configurable buffer
   * sizes and adapted for current use case.
   */
  private void sendContent(final InputStream from, final HttpServletResponse response) throws IOException {
    int bufferSize = BUFFER_SIZE;
    if (bufferSize < 1) {
      // if no user override, ask container for bufferSize
      bufferSize = response.getBufferSize();
      if (bufferSize < 1) {
        bufferSize = 8192;
        response.setBufferSize(bufferSize);
      }
    }
    else {
      // user override present, tell container what buffer size we'd like
      response.setBufferSize(bufferSize);
    }
    final byte[] buf = new byte[bufferSize];
    try (final OutputStream to = response.getOutputStream()) {
      while (true) {
        int r = from.read(buf);
        if (r == -1) {
          break;
        }
        to.write(buf, 0, r);
      }
    }
    response.flushBuffer();
  }
}
