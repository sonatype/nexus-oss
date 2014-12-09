/*
 * Copyright (c) 2007-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.bolyuba.nexus.plugin.npm.service.internal.proxy;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.apachehttpclient.Hc4Provider;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.FileContentLocator;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.storage.remote.httpclient.HttpClientManager;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.bolyuba.nexus.plugin.npm.NpmRepository;
import com.bolyuba.nexus.plugin.npm.proxy.NpmProxyRepository;
import com.bolyuba.nexus.plugin.npm.service.PackageRoot;
import com.bolyuba.nexus.plugin.npm.service.internal.MetadataParser;
import com.bolyuba.nexus.plugin.npm.service.internal.PackageRootIterator;
import com.bolyuba.nexus.plugin.npm.service.internal.ProxyMetadataTransport;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link ProxyMetadataTransport} for HTTP.
 */
@Singleton
@Named
public class HttpProxyMetadataTransport
    extends ComponentSupport
    implements ProxyMetadataTransport
{
  private static final Logger outboundRequestLog = LoggerFactory.getLogger("remote.storage.outbound");

  private static final String PROP_ETAG = "remote.etag";

  private final MetadataParser metadataParser;

  private final HttpClientManager httpClientManager;

  @Inject
  public HttpProxyMetadataTransport(final MetadataParser metadataParser,
                                    final HttpClientManager httpClientManager)
  {
    this.metadataParser = checkNotNull(metadataParser);
    this.httpClientManager = checkNotNull(httpClientManager);
  }

  /**
   * Performs a HTTP GET to fetch the registry root. Note: by testing on my mac (MBP 2012 SSD), seems OrientDB is
   * "slow" to consume the streamed HTTP response (ie. to push it immediately into database, maintaining indexes etc).
   * Hence, we save the response JSON to temp file and parse it from there to not have remote registry HTTP Server give
   * up on connection with us.
   */
  @Override
  public PackageRootIterator fetchRegistryRoot(final NpmProxyRepository npmProxyRepository) throws IOException {
    final HttpClient httpClient = httpClientManager.create(npmProxyRepository,
        npmProxyRepository.getRemoteStorageContext());
    try {
      final HttpGet get = new HttpGet(
          buildUri(npmProxyRepository, "-/all")); // TODO: this in NPM specific, might try both root and NPM api
      outboundRequestLog.debug("{} - NPM GET {}", npmProxyRepository.getId(), get.getURI());
      get.addHeader("accept", NpmRepository.JSON_MIME_TYPE);
      final HttpClientContext context = new HttpClientContext();
      context.setAttribute(Hc4Provider.HTTP_CTX_KEY_REPOSITORY, npmProxyRepository);
      final HttpResponse httpResponse = httpClient.execute(get, context);
      try {
        outboundRequestLog.debug("{} - NPM GET {} - {}", npmProxyRepository.getId(), get.getURI(),
            httpResponse.getStatusLine());
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          final File tempFile = File
              .createTempFile(npmProxyRepository.getId() + "-root", "temp.json",
                  metadataParser.getTemporaryDirectory());
          try (final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            httpResponse.getEntity().writeTo(bos);
            bos.flush();
          }
          log.debug("Registry root written out to file {}, size {} bytes", tempFile.getAbsolutePath(), tempFile.length());
          final FileContentLocator cl = new FileContentLocator(tempFile, NpmRepository.JSON_MIME_TYPE, true);
          return metadataParser.parseRegistryRoot(npmProxyRepository.getId(), cl);
        }
        throw new IOException("Unexpected response from registry root " + httpResponse.getStatusLine());
      }
      finally {
        EntityUtils.consumeQuietly(httpResponse.getEntity());
      }
    }
    finally {
      httpClientManager.release(npmProxyRepository, npmProxyRepository.getRemoteStorageContext());
    }
  }

  /**
   * Performs a conditional GET to fetch the package root and returns the fetched package root. If fetch succeeded
   * (HTTP 200 Ok is returned), the package root is also pushed into {@code MetadataStore}. In short, the returned
   * package root from this method is guaranteed to be present in the store too.
   */
  @Override
  public PackageRoot fetchPackageRoot(final NpmProxyRepository npmProxyRepository, final String packageName,
                                      final PackageRoot expired) throws IOException
  {
    final HttpClient httpClient = httpClientManager.create(npmProxyRepository,
        npmProxyRepository.getRemoteStorageContext());
    try {
      final HttpGet get = new HttpGet(buildUri(npmProxyRepository, packageName));
      outboundRequestLog.debug("{} - NPM GET {}", npmProxyRepository.getId(), get.getURI());
      get.addHeader("accept", NpmRepository.JSON_MIME_TYPE);
      if (expired != null && expired.getProperties().containsKey(PROP_ETAG)) {
        get.addHeader("if-none-match", expired.getProperties().get(PROP_ETAG));
      }
      final HttpClientContext context = new HttpClientContext();
      context.setAttribute(Hc4Provider.HTTP_CTX_KEY_REPOSITORY, npmProxyRepository);
      final HttpResponse httpResponse = httpClient.execute(get, context);
      try {
        outboundRequestLog.debug("{} - NPM GET {} - {}", npmProxyRepository.getId(), get.getURI(),
            httpResponse.getStatusLine());
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
          return expired;
        }
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          final PreparedContentLocator pcl = new PreparedContentLocator(httpResponse.getEntity().getContent(),
              NpmRepository.JSON_MIME_TYPE, ContentLocator.UNKNOWN_LENGTH);
          final PackageRoot fresh = metadataParser.parsePackageRoot(npmProxyRepository.getId(), pcl);
          if (httpResponse.containsHeader("etag")) {
            fresh.getProperties().put(PROP_ETAG, httpResponse.getFirstHeader("etag").getValue());
          }
          return fresh;
        }
        return null;
      }
      finally {
        EntityUtils.consumeQuietly(httpResponse.getEntity());
      }
    }
    finally {
      httpClientManager.release(npmProxyRepository, npmProxyRepository.getRemoteStorageContext());
    }
  }

  /**
   * Builds and return registry URI for given package name.
   */
  private String buildUri(final NpmProxyRepository npmProxyRepository, final String pathElem) {
    final String registryUrl = npmProxyRepository.getRemoteUrl();
    if (registryUrl.endsWith("/")) {
      return registryUrl + pathElem;
    }
    else {
      return registryUrl + "/" + pathElem;
    }
  }
}
