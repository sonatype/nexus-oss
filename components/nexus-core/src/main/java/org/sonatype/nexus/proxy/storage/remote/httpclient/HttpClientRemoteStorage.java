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

package org.sonatype.nexus.proxy.storage.remote.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.apachehttpclient.Hc4Provider;
import org.sonatype.nexus.apachehttpclient.page.Page;
import org.sonatype.nexus.apachehttpclient.page.Page.PageContext;
import org.sonatype.nexus.apachehttpclient.page.Page.RepositoryPageContext;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessDeniedException;
import org.sonatype.nexus.proxy.RemoteAuthenticationNeededException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.RemoteStorageTransportOverloadedException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.AbstractHTTPRemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext.BooleanFlagHolder;
import org.sonatype.nexus.proxy.storage.remote.RemoteItemNotFoundException;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.http.QueryStringBuilder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Apache HTTP client (4) {@link RemoteRepositoryStorage} implementation.
 *
 * @since 2.0
 */
@Named(HttpClientRemoteStorage.PROVIDER_STRING)
@Singleton
public class HttpClientRemoteStorage
    extends AbstractHTTPRemoteRepositoryStorage
    implements RemoteRepositoryStorage
{

  private static final Logger outboundRequestLog = LoggerFactory.getLogger("remote.storage.outbound");

  // ----------------------------------------------------------------------
  // Constants
  // ----------------------------------------------------------------------

  /**
   * ID of this provider.
   */
  public static final String PROVIDER_STRING = "apacheHttpClient4x";

  /**
   * HTTP header key sent back by Nexus in case of a missing artifact.
   */
  public static final String NEXUS_MISSING_ARTIFACT_HEADER = "x-nexus-missing-artifact";

  /**
   * Context key of HTTP client.
   */
  private static final String CTX_KEY_CLIENT = PROVIDER_STRING + ".client";

  /**
   * Context key of a flag present in case that remote server is an Amazon S3.
   */
  private static final String CTX_KEY_S3_FLAG = PROVIDER_STRING + ".remoteIsAmazonS3";

  /**
   * Created items while retrieving, can be read.
   */
  private static final boolean CAN_READ = true;

  /**
   * Created items while retrieving, can be written.
   */
  private static final boolean CAN_WRITE = true;

  /**
   * Key used in HttpGet method parameters in {@link #retrieveItem(ProxyRepository, ResourceStoreRequest, String)}
   * method
   * that this request is about content retrieval, hence, the special redirection strategy set up in
   * {@link HttpClientManagerImpl#getProxyRepositoryRedirectStrategy(ProxyRepository, RemoteStorageContext)} should
   * be applied. See that method for more.
   *
   * @since 2.7.0
   */
  public static final String CONTENT_RETRIEVAL_MARKER_KEY = HttpClientRemoteStorage.class.getName() + "#retrieveItem";

  /**
   * How many "redirect hops" we follow.
   *
   * @since 2.8.0
   */
  private static final int REDIRECT_HOPS_COUNT = 5;

  private final MetricsRegistry metricsRegistry;

  private final QueryStringBuilder queryStringBuilder;

  private final HttpClientManager httpClientManager;

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  @Inject
  HttpClientRemoteStorage(final ApplicationStatusSource applicationStatusSource,
                          final MimeSupport mimeSupport,
                          final QueryStringBuilder queryStringBuilder,
                          final HttpClientManager httpClientManager)
  {
    super(applicationStatusSource, mimeSupport);
    this.metricsRegistry = Metrics.defaultRegistry();
    this.queryStringBuilder = queryStringBuilder;
    this.httpClientManager = httpClientManager;
  }

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  @Override
  public String getProviderId() {
    return PROVIDER_STRING;
  }

  @Override
  public AbstractStorageItem retrieveItem(final ProxyRepository repository, final ResourceStoreRequest request,
                                          final String baseUrl)
      throws ItemNotFoundException, RemoteStorageException
  {
    final URL remoteURL =
        appendQueryString(getAbsoluteUrlFromBase(baseUrl, request.getRequestPath()), repository);
    return retrieveItem(repository, request, baseUrl, Lists.newArrayList(remoteURL));
  }

  private AbstractStorageItem retrieveItem(final ProxyRepository repository, final ResourceStoreRequest request,
                                           final String currentBaseUrl, final List<URL> urls)
      throws ItemNotFoundException, RemoteStorageException
  {
    if (urls.size() >= REDIRECT_HOPS_COUNT) {
      log.info("Proxy repository {} followed too many redirects, giving up: {}", repository, urls);
      throw new RemoteStorageException("Proxy repository " + repository + " too many redirects: " + urls);
    }
    final URL remoteURL = urls.get(urls.size() - 1); // last URL in list is "current" one

    if (remoteURL.getPath().endsWith("/")) {
      // we do not want to fetch index pages
      throw new RemoteItemNotFoundException(request, repository, "remoteIsCollection", remoteURL.toString());
    }

    final HttpGet method = new HttpGet(remoteURL.toString());
    method.getParams().setBooleanParameter(CONTENT_RETRIEVAL_MARKER_KEY, true);

    final HttpResponse httpResponse = executeRequest(repository, request, method, currentBaseUrl);
    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      try {
        final InputStream is = new Hc4InputStream(repository,
            new InterruptableInputStream(method, httpResponse.getEntity().getContent()));

        String mimeType = ContentType.getOrDefault(httpResponse.getEntity()).getMimeType();
        if (mimeType == null) {
          mimeType =
              getMimeSupport().guessMimeTypeFromPath(repository.getMimeRulesSource(),
                  request.getRequestPath());
        }

        final long entityLength = httpResponse.getEntity().getContentLength();
        final DefaultStorageFileItem httpItem =
            new DefaultStorageFileItem(repository, request, CAN_READ, CAN_WRITE, new PreparedContentLocator(
                is, mimeType, entityLength != -1 ? entityLength : ContentLocator.UNKNOWN_LENGTH));

        httpItem.setRemoteUrl(remoteURL.toString());
        httpItem.setModified(makeDateFromHeader(httpResponse.getFirstHeader("last-modified")));
        httpItem.setCreated(httpItem.getModified());

        return httpItem;
      }
      catch (IOException ex) {
        release(httpResponse);
        throw new RemoteStorageException("IO Error during response stream handling [repositoryId=\""
            + repository.getId() + "\", requestPath=\"" + request.getRequestPath() + "\", remoteUrl=\""
            + remoteURL.toString() + "\"]!", ex);
      }
      catch (RuntimeException ex) {
        release(httpResponse);
        throw ex;
      }
    }
    else {
      try {
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
          release(httpResponse);
          throw new RemoteItemNotFoundException(request, repository, "NotFound", remoteURL.toString());
        }
        else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY
            || httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY
            || httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_TEMPORARY_REDIRECT) {
          final URL redirectionUrl = shouldFollowRedirect(repository, request, method, httpResponse, remoteURL);
          if (redirectionUrl != null) {
            urls.add(redirectionUrl);
            return retrieveItem(repository, request, currentBaseUrl, urls);
          }
          else {
            throw new RemoteItemNotFoundException(request, repository, "RedirectedNotFollowing", remoteURL.toString());
          }
        }
        else {
          release(httpResponse);
          throw new RemoteStorageException("The method execution returned result code "
              + httpResponse.getStatusLine().getStatusCode() + " (expected 200). [repositoryId=\""
              + repository.getId() + "\", requestPath=\"" + request.getRequestPath() + "\", remoteUrl=\""
              + remoteURL.toString() + "\"]");
        }
      }
      finally {
        release(httpResponse);
      }
    }
  }

  /**
   * Returns {@link URL} if received response is redirect, and redirection should be followed. In any other case
   * {@code null} is returned.
   *
   * @since 2.8.0
   */
  protected URL shouldFollowRedirect(final ProxyRepository repository, final ResourceStoreRequest request,
                                     final HttpUriRequest httpRequest, final HttpResponse httpResponse,
                                     final URL remoteURL)
      throws RemoteStorageException
  {
    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY
        || httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY
        || httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_TEMPORARY_REDIRECT) {
      final Header locationHeader = httpResponse.getFirstHeader("location");
      if (locationHeader != null) {
        final String redirectionUrlHeader = locationHeader.getValue();
        try {
          final URL redirectionURL = new URL(redirectionUrlHeader);
          // protocol change?
          if (!Objects.equals(remoteURL.getProtocol().toLowerCase(Locale.US), redirectionURL.getProtocol().toLowerCase(
              Locale.US))) {
            if ("http".equals(redirectionURL.getProtocol().toLowerCase(Locale.US))) {
              // security risk: HTTPS > HTTP downgrade, you are not safe as you think!
              log.info(
                  "Proxy repository {} remote access issue, downgrade from HTTPS to HTTP during redirection {} -> {}",
                  repository, remoteURL, redirectionURL);
            }
            if ("https".equals(redirectionURL.getProtocol().toLowerCase(Locale.US))) {
              // misconfiguration: your repository uses wrong protocol and causes performance problems?
              log.info(
                  "Proxy repository {} remote URL misconfiguration, protocol upgrade during redirection {} -> {}",
                  repository, remoteURL, redirectionURL);
            }
          }
          // host change?
          if (!Objects.equals(remoteURL.getHost().toLowerCase(Locale.US), redirectionURL.getHost().toLowerCase(
              Locale.US))) {
            log.info(
                "Proxy repository {} remote URL misconfiguration, hostname change during redirection {} -> {}",
                repository, remoteURL, redirectionURL);
          }
          if (httpRequest.getParams().isParameterTrue(CONTENT_RETRIEVAL_MARKER_KEY) &&
              redirectionURL.getPath().endsWith("/")) {
            // assuming properly set up remote server following conventions
            // http://googlewebmastercentral.blogspot.hu/2010/04/to-slash-or-not-to-slash.html
            // We assume paths ending with slash denote directories, so do not follow
            return null;
          }
          return redirectionURL;
        }
        catch (MalformedURLException e) {
          throw new RemoteStorageException("Remote server returned redirection "
              + httpResponse.getStatusLine() + " but provided illegal Location header URL " +
              redirectionUrlHeader + " [repositoryId=\""
              + repository.getId() + "\", requestPath=\"" + request.getRequestPath() + "\", remoteUrl=\""
              + request.getRequestUrl() + "\"]", e);
        }
      }
      else {
        throw new RemoteStorageException("Remote server returned redirection "
            + httpResponse.getStatusLine() + " but provided no Location header " + " [repositoryId=\""
            + repository.getId() + "\", requestPath=\"" + request.getRequestPath() + "\", remoteUrl=\""
            + request.getRequestUrl() + "\"]");
      }
    }
    return null;
  }

  @Override
  public void storeItem(final ProxyRepository repository, final StorageItem item)
      throws UnsupportedStorageOperationException, RemoteStorageException
  {
    if (!(item instanceof StorageFileItem)) {
      throw new UnsupportedStorageOperationException("Storing of non-files remotely is not supported!");
    }

    final StorageFileItem fileItem = (StorageFileItem) item;

    final ResourceStoreRequest request = new ResourceStoreRequest(item);

    final URL remoteUrl = appendQueryString(getAbsoluteUrlFromBase(repository, request), repository);

    final HttpPut method = new HttpPut(remoteUrl.toExternalForm());

    final InputStreamEntity entity;
    try {
      entity =
          new InputStreamEntity(new InterruptableInputStream(fileItem.getInputStream()), fileItem.getLength());
    }
    catch (IOException e) {
      throw new RemoteStorageException(e.getMessage() + " [repositoryId=\"" + repository.getId()
          + "\", requestPath=\"" + request.getRequestPath() + "\", remoteUrl=\"" + remoteUrl.toString() + "\"]",
          e);
    }

    entity.setContentType(fileItem.getMimeType());
    method.setEntity(entity);

    final HttpResponse httpResponse = executeRequestAndRelease(repository, request, method, repository.getRemoteUrl());
    final int statusCode = httpResponse.getStatusLine().getStatusCode();

    if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED
        && statusCode != HttpStatus.SC_NO_CONTENT && statusCode != HttpStatus.SC_ACCEPTED) {
      throw new RemoteStorageException("Unexpected response code while executing " + method.getMethod()
          + " method [repositoryId=\"" + repository.getId() + "\", requestPath=\"" + request.getRequestPath()
          + "\", remoteUrl=\"" + remoteUrl.toString() + "\"]. Expected: \"any success (2xx)\". Received: "
          + statusCode + " : " + httpResponse.getStatusLine().getReasonPhrase());
    }
  }

  @Override
  public void deleteItem(final ProxyRepository repository, final ResourceStoreRequest request)
      throws ItemNotFoundException, UnsupportedStorageOperationException, RemoteStorageException
  {
    final URL remoteUrl = appendQueryString(getAbsoluteUrlFromBase(repository, request), repository);

    final HttpDelete method = new HttpDelete(remoteUrl.toExternalForm());

    final HttpResponse httpResponse = executeRequestAndRelease(repository, request, method, repository.getRemoteUrl());
    final int statusCode = httpResponse.getStatusLine().getStatusCode();

    if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NO_CONTENT
        && statusCode != HttpStatus.SC_ACCEPTED) {
      throw new RemoteStorageException("The response to HTTP " + method.getMethod()
          + " was unexpected HTTP Code " + statusCode + " : " + httpResponse.getStatusLine().getReasonPhrase()
          + " [repositoryId=\"" + repository.getId() + "\", requestPath=\"" + request.getRequestPath()
          + "\", remoteUrl=\"" + remoteUrl.toString() + "\"]");
    }
  }

  @Override
  protected boolean checkRemoteAvailability(final long newerThen, final ProxyRepository repository,
                                            final ResourceStoreRequest request, final boolean isStrict)
      throws RemoteStorageException
  {
    return checkRemoteAvailability(newerThen, repository, request, isStrict,
        Lists.newArrayList(appendQueryString(getAbsoluteUrlFromBase(repository, request), repository)));
  }

  private boolean checkRemoteAvailability(final long newerThen, final ProxyRepository repository,
                                          final ResourceStoreRequest request, final boolean isStrict,
                                          final List<URL> urls)
      throws RemoteStorageException
  {
    if (urls.size() >= REDIRECT_HOPS_COUNT) {
      log.info("Proxy repository {} followed too many redirects, giving up: {}", repository, urls);
      throw new RemoteStorageException("Proxy repository " + repository + " too many redirects: " + urls);
    }
    final URL remoteUrl = urls.get(urls.size() - 1);

    HttpRequestBase method;
    HttpResponse httpResponse = null;
    int statusCode = HttpStatus.SC_BAD_REQUEST;

    // artifactory hack, it pukes on HEAD so we will try with GET if HEAD fails
    boolean doGet = false;

    {
      method = new HttpHead(remoteUrl.toExternalForm());
      try {
        httpResponse = executeRequestAndRelease(repository, request, method, repository.getRemoteUrl());
        statusCode = httpResponse.getStatusLine().getStatusCode();
        final URL redirectionUrl = shouldFollowRedirect(repository, request, method, httpResponse, remoteUrl);
        if (redirectionUrl != null) {
          urls.add(redirectionUrl);
          return checkRemoteAvailability(newerThen, repository, request, isStrict, urls);
        }
      }
      catch (RemoteStorageException e) {
        // If HEAD failed, attempt a GET. Some repos may not support HEAD method
        doGet = true;

        log.debug("HEAD method failed, will attempt GET. Exception: " + e.getMessage(), e);
      }
      finally {
        // HEAD returned error, but not exception, try GET before failing
        if (!doGet && statusCode != HttpStatus.SC_OK) {
          doGet = true;

          log.debug("HEAD method failed, will attempt GET. Status: " + statusCode);
        }
      }
    }

    {
      if (doGet) {
        // create a GET
        method = new HttpGet(remoteUrl.toExternalForm());

        // execute it
        httpResponse = executeRequestAndRelease(repository, request, method, repository.getRemoteUrl());
        statusCode = httpResponse.getStatusLine().getStatusCode();
        final URL redirectionUrl = shouldFollowRedirect(repository, request, method, httpResponse, remoteUrl);
        if (redirectionUrl != null) {
          urls.add(redirectionUrl);
          return checkRemoteAvailability(newerThen, repository, request, isStrict, urls);
        }
      }
    }

    // if we are not strict and remote is S3
    if (!isStrict && isRemotePeerAmazonS3Storage(repository)) {
      // if we are relaxed, we will accept any HTTP response code below 500. This means anyway the HTTP
      // transaction succeeded. This method was never really detecting that the remoteUrl really denotes a root of
      // repository (how could we do that?)
      // this "relaxed" check will help us to "pass" S3 remote storage.
      return statusCode >= HttpStatus.SC_OK && statusCode <= HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }
    else {
      // non relaxed check is strict, and will select only the OK response
      if (statusCode == HttpStatus.SC_OK) {
        // we have it
        // we have newer if this below is true
        return makeDateFromHeader(httpResponse.getFirstHeader("last-modified")) > newerThen;
      }
      else if ((statusCode >= HttpStatus.SC_MULTIPLE_CHOICES && statusCode < HttpStatus.SC_BAD_REQUEST)
          || statusCode == HttpStatus.SC_NOT_FOUND) {
        if (RepositoryItemUid.PATH_ROOT.equals(request.getRequestPath()) && statusCode == HttpStatus.SC_NOT_FOUND) {
          // NEXUS-5944: Give it a chance: it might be remote Nexus with browsing disabled?
          // to check that, we will check is remote is Nexus by pinging "well know" location
          // if we got it, we will know it's only browsing forbidden on remote
          if (isRemoteNexus(repository)) {
            log.debug(
                "Original GET request for URL {} failed with 404, but remote detected as Nexus, we assume remote is a Nexus repository having browsing disabled.",
                remoteUrl);
            return true;
          }
        }
        return false;
      }
      else {
        throw new RemoteStorageException("Unexpected response code while executing " + method.getMethod()
            + " method [repositoryId=\"" + repository.getId() + "\", requestPath=\"" + request.getRequestPath()
            + "\", remoteUrl=\"" + remoteUrl.toString() + "\"]. Expected: \"SUCCESS (200)\". Received: "
            + statusCode + " : " + httpResponse.getStatusLine().getReasonPhrase());
      }
    }
  }

  @Override
  protected boolean isRemoteNexus(final ProxyRepository repository) throws RemoteStorageException {
    final RemoteStorageContext ctx = getRemoteStorageContext(repository);
    final HttpClient httpClient = (HttpClient) ctx.getContextObject(CTX_KEY_CLIENT);
    final PageContext pageContext = new RepositoryPageContext(httpClient, repository);
    final URL nxRepoMetadataUrl = appendQueryString(
        getAbsoluteUrlFromBase(repository, new ResourceStoreRequest("/.meta/repository-metadata.xml")),
        repository);
    try {
      final Page page = Page.getPageFor(pageContext, nxRepoMetadataUrl.toExternalForm());
      if (page.getStatusCode() == 200) {
        // remote is a Nexus, it has published repository-metadata
        return true;
      }
    }
    catch (IOException e) {
      // just fall trough
      log.warn(e.getMessage());
    }
    return false;
  }


  @Override
  protected void updateContext(final ProxyRepository repository, final RemoteStorageContext ctx)
      throws RemoteStorageException
  {
    // reset current http client, if exists
    ctx.removeContextObject(CTX_KEY_CLIENT);
    ctx.removeContextObject(CTX_KEY_S3_FLAG);
    httpClientManager.release(repository, ctx);

    try {
      // and create a new one
      final HttpClient httpClient = httpClientManager.create(repository, ctx);
      ctx.putContextObject(CTX_KEY_CLIENT, httpClient);
      // NEXUS-3338: we don't know after config change is remote S3 (url changed maybe)
      ctx.putContextObject(CTX_KEY_S3_FLAG, new BooleanFlagHolder());
    }
    catch (IllegalStateException e) {
      throw new RemoteStorageException("Could not create HTTPClient4x instance!", e);
    }
  }

  @Override
  protected String getS3FlagKey() {
    return CTX_KEY_S3_FLAG;
  }

  // ----------------------------------------------------------------------
  // Implementation methods
  // ----------------------------------------------------------------------

  /**
   * Executes the HTTP request.
   * <p/>
   * In case of any exception thrown by HttpClient, it will release the connection. In other cases it is the duty of
   * caller to do it, or process the input stream.
   *
   * @param repository  to execute the HTTP method for
   * @param request     resource store request that triggered the HTTP request
   * @param httpRequest HTTP request to be executed
   * @param baseUrl     The BaseURL used to construct final httpRequest
   * @return response of making the request
   * @throws RemoteStorageException If an error occurred during execution of HTTP request
   */
  @VisibleForTesting
  HttpResponse executeRequest(final ProxyRepository repository, final ResourceStoreRequest request,
                              final HttpUriRequest httpRequest, final String baseUrl) throws RemoteStorageException
  {
    final Timer timer = timer(repository, httpRequest, baseUrl);
    final TimerContext timerContext = timer.time();
    Stopwatch stopwatch = null;
    if (outboundRequestLog.isDebugEnabled()) {
      stopwatch = new Stopwatch().start();
    }
    try {
      return doExecuteRequest(repository, request, httpRequest);
    }
    finally {
      timerContext.stop();
      if (stopwatch != null) {
        // log actual request completely with timing
        outboundRequestLog.debug("[{}] {} - {}", repository.getId(), httpRequest.getRequestLine(), stopwatch);
      }
    }
  }

  private Timer timer(final ProxyRepository repository, final HttpUriRequest httpRequest, final String baseUrl) {
    return metricsRegistry
        .newTimer(HttpClientRemoteStorage.class, baseUrl, repository.getId());
    // TODO: Better would be this below, as would distinguish between HEADs and GETs as some servers behave wildly differently
    // return metricsRegistry
    //    .newTimer(HttpClientRemoteStorage.class, httpRequest.getMethod() + " " + baseUrl, repository.getId());
  }

  private HttpResponse doExecuteRequest(final ProxyRepository repository, final ResourceStoreRequest request,
                                        final HttpUriRequest httpRequest)
      throws RemoteStorageException
  {
    final URI methodUri = httpRequest.getURI();

    if (log.isDebugEnabled()) {
      log.debug("Invoking HTTP {} method against remote location {}", httpRequest.getMethod(), methodUri);
    }

    final RemoteStorageContext ctx = getRemoteStorageContext(repository);

    final HttpClient httpClient = (HttpClient) ctx.getContextObject(CTX_KEY_CLIENT);

    httpRequest.setHeader("Accept", "*/*");
    httpRequest.setHeader("Accept-Language", "en-us");
    httpRequest.setHeader("Accept-Encoding", "gzip,deflate,identity");
    httpRequest.setHeader("Cache-Control", "no-cache");

    HttpResponse httpResponse = null;
    try {
      final BasicHttpContext httpContext = new BasicHttpContext();
      httpContext.setAttribute(Hc4Provider.HTTP_CTX_KEY_REPOSITORY, repository);

      httpResponse = httpClient.execute(httpRequest, httpContext);
      final int statusCode = httpResponse.getStatusLine().getStatusCode();

      final Header httpServerHeader = httpResponse.getFirstHeader("server");
      checkForRemotePeerAmazonS3Storage(repository,
          httpServerHeader == null ? null : httpServerHeader.getValue());

      Header proxyReturnedErrorHeader = httpResponse.getFirstHeader(NEXUS_MISSING_ARTIFACT_HEADER);
      boolean proxyReturnedError =
          proxyReturnedErrorHeader != null && Boolean.valueOf(proxyReturnedErrorHeader.getValue());

      if (statusCode == HttpStatus.SC_FORBIDDEN) {
        throw new RemoteAccessDeniedException(repository, methodUri.toASCIIString(),
            httpResponse.getStatusLine().getReasonPhrase());
      }
      else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
        throw new RemoteAuthenticationNeededException(repository,
            httpResponse.getStatusLine().getReasonPhrase());
      }
      else if (statusCode == HttpStatus.SC_OK && proxyReturnedError) {
        throw new RemoteStorageException(
            "Invalid artifact found, most likely a proxy redirected to an HTML error page.");
      }

      return httpResponse;
    }
    catch (RemoteStorageException ex) {
      release(httpResponse);
      throw ex;
    }
    catch (ClientProtocolException ex) {
      release(httpResponse);
      throw new RemoteStorageException("Protocol error while executing " + httpRequest.getMethod()
          + " method. [repositoryId=\"" + repository.getId() + "\", requestPath=\"" + request.getRequestPath()
          + "\", remoteUrl=\"" + methodUri.toASCIIString() + "\"]", ex);
    }
    catch (ConnectionPoolTimeoutException ex) {
      release(httpResponse);
      throw new RemoteStorageTransportOverloadedException(repository,
          "Connection pool timeout error while executing " + httpRequest.getMethod() + " method [repositoryId=\""
              + repository.getId() + "\", requestPath=\"" + request.getRequestPath() + "\", remoteUrl=\""
              + methodUri.toASCIIString() + "\"]", ex);
    }
    catch (IOException ex) {
      release(httpResponse);
      throw new RemoteStorageException("Transport error while executing " + httpRequest.getMethod()
          + " method [repositoryId=\"" + repository.getId() + "\", requestPath=\"" + request.getRequestPath()
          + "\", remoteUrl=\"" + methodUri.toASCIIString() + "\"]", ex);
    }
  }

  /**
   * Executes the HTTP request and automatically releases any related resources.
   *
   * @param repository  to execute the HTTP method fpr
   * @param request     resource store request that triggered the HTTP request
   * @param httpRequest HTTP request to be executed
   * @return response of making the request
   * @throws RemoteStorageException If an error occurred during execution of HTTP request
   */
  private HttpResponse executeRequestAndRelease(final ProxyRepository repository,
                                                final ResourceStoreRequest request, final HttpUriRequest httpRequest,
                                                final String baseUrl)
      throws RemoteStorageException
  {
    final HttpResponse httpResponse = executeRequest(repository, request, httpRequest, baseUrl);
    release(httpResponse);
    return httpResponse;
  }

  /**
   * Make date from header.
   *
   * @param date the date
   * @return the long
   */
  private long makeDateFromHeader(final Header date) {
    long result = System.currentTimeMillis();
    if (date != null) {
      try {
        result = DateUtils.parseDate(date.getValue()).getTime();
      }
      catch (DateParseException ex) {
        log.warn("Could not parse date '{}', using system current time as item creation time.", date, ex);
      }
      catch (NullPointerException ex) {
        log.warn("Parsed date is null, using system current time as item creation time.");
      }
    }
    return result;
  }

  /**
   * Appends repository configured additional query string to provided URL.
   *
   * @param url        to append to
   * @param repository that may contain additional query string
   * @return URL with appended query string or original URL if repository does not have an configured query string
   * @throws RemoteStorageException if query string could not be appended (resulted in an Malformed URL exception)
   */
  private URL appendQueryString(final URL url, final ProxyRepository repository)
      throws RemoteStorageException
  {
    final RemoteStorageContext ctx = getRemoteStorageContext(repository);

    String queryString = queryStringBuilder.getQueryString(ctx, repository);

    if (StringUtils.isNotBlank(queryString)) {
      try {
        if (StringUtils.isBlank(url.getQuery())) {
          return new URL(url.toExternalForm() + "?" + queryString);
        }
        else {
          return new URL(url.toExternalForm() + "&" + queryString);
        }
      }
      catch (MalformedURLException e) {
        throw new RemoteStorageException("Could not append query string \"" + queryString + "\" to url \""
            + url + "\"", e);
      }
    }
    return url;
  }

  /**
   * Releases connection resources (back to pool). If an exception appears during releasing, exception is just
   * logged.
   *
   * @param httpResponse to be released
   */
  private void release(final HttpResponse httpResponse) {
    if (httpResponse != null) {
      try {
        EntityUtils.consume(httpResponse.getEntity());
      }
      catch (IOException e) {
        log.warn("Failed to consume entity: " + e); // terse
      }
    }
  }

}
