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

package org.sonatype.nexus.internal.httpclient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.sonatype.nexus.httpclient.HttpClientFactory.Builder;
import org.sonatype.nexus.httpclient.HttpClientFactory.Customizer;
import org.sonatype.nexus.proxy.repository.ClientSSLRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.DefaultSchemePortResolver;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * RemoteStorageContext {@link Customizer}.
 *
 * @since 3.0
 */
public class RemoteStorageContextCustomizer
    extends ComponentSupport
    implements Customizer
{
  private final RemoteStorageContext context;

  public RemoteStorageContextCustomizer(final RemoteStorageContext context) {
    this.context = checkNotNull(context);
  }

  @Override
  public void customize(final Builder builder) {
    // connection/socket timeouts
    int timeout = 1000;
    if (context.getRemoteConnectionSettings() != null) {
      timeout = context.getRemoteConnectionSettings().getConnectionTimeout();
    }
    builder.getSocketConfigBuilder().setSoTimeout(timeout);
    builder.getRequestConfigBuilder().setConnectTimeout(timeout);
    builder.getRequestConfigBuilder().setSocketTimeout(timeout);

    // obey the given retries count and apply it to client.
    int retries = context.getRemoteConnectionSettings() != null ? context.getRemoteConnectionSettings()
        .getRetrievalRetryCount() : 0;
    builder.getHttpClientBuilder().setRetryHandler(new StandardHttpRequestRetryHandler(retries, false));

    applyAuthenticationConfig(builder, context.getRemoteAuthenticationSettings(), null);
    applyProxyConfig(builder, context.getRemoteProxySettings());

    // Apply optional context-specific user-agent suffix
    if (context.getRemoteConnectionSettings() != null) {
      String userAgentSuffix = context.getRemoteConnectionSettings().getUserAgentCustomizationString();
      if (!StringUtils.isEmpty(userAgentSuffix)) {
        builder.setUserAgent(builder.getUserAgent() + " " + userAgentSuffix);
      }
    }
  }

  @VisibleForTesting
  void applyAuthenticationConfig(final Builder builder,
                                 final RemoteAuthenticationSettings ras,
                                 final HttpHost proxyHost)
  {
    if (ras != null) {
      String authScope = "target";
      if (proxyHost != null) {
        authScope = proxyHost.toHostString() + " proxy";
      }

      final List<String> authorisationPreference = Lists.newArrayListWithExpectedSize(3);
      authorisationPreference.add(AuthSchemes.DIGEST);
      authorisationPreference.add(AuthSchemes.BASIC);
      Credentials credentials = null;
      if (ras instanceof ClientSSLRemoteAuthenticationSettings) {
        throw new IllegalArgumentException("SSL client authentication not yet supported!");
      }
      else if (ras instanceof NtlmRemoteAuthenticationSettings) {
        final NtlmRemoteAuthenticationSettings nras = (NtlmRemoteAuthenticationSettings) ras;
        // Using NTLM auth, adding it as first in policies
        authorisationPreference.add(0, AuthSchemes.NTLM);
        log.debug("{} authentication setup for NTLM domain '{}'", authScope, nras.getNtlmDomain());
        credentials = new NTCredentials(
            nras.getUsername(), nras.getPassword(), nras.getNtlmHost(), nras.getNtlmDomain()
        );
      }
      else if (ras instanceof UsernamePasswordRemoteAuthenticationSettings) {
        final UsernamePasswordRemoteAuthenticationSettings uras =
            (UsernamePasswordRemoteAuthenticationSettings) ras;
        log.debug("{} authentication setup for remote storage with username '{}'", authScope,
            uras.getUsername());
        credentials = new UsernamePasswordCredentials(uras.getUsername(), uras.getPassword());
      }

      if (credentials != null) {
        if (proxyHost != null) {
          builder.setCredentials(new AuthScope(proxyHost), credentials);
          builder.getRequestConfigBuilder().setProxyPreferredAuthSchemes(authorisationPreference);
        }
        else {
          builder.setCredentials(AuthScope.ANY, credentials);
          builder.getRequestConfigBuilder().setTargetPreferredAuthSchemes(authorisationPreference);
        }
      }
    }
  }

  @VisibleForTesting
  void applyProxyConfig(final Builder builder, final RemoteProxySettings remoteProxySettings) {
    if (remoteProxySettings != null
        && remoteProxySettings.getHttpProxySettings() != null
        && remoteProxySettings.getHttpProxySettings().isEnabled()) {
      final Map<String, HttpHost> proxies = Maps.newHashMap();

      final HttpHost httpProxy = new HttpHost(
          remoteProxySettings.getHttpProxySettings().getHostname(),
          remoteProxySettings.getHttpProxySettings().getPort()
      );
      applyAuthenticationConfig(
          builder, remoteProxySettings.getHttpProxySettings().getProxyAuthentication(), httpProxy
      );

      log.debug("http proxy setup with host '{}'", remoteProxySettings.getHttpProxySettings().getHostname());
      proxies.put("http", httpProxy);
      proxies.put("https", httpProxy);

      if (remoteProxySettings.getHttpsProxySettings() != null
          && remoteProxySettings.getHttpsProxySettings().isEnabled()) {
        final HttpHost httpsProxy = new HttpHost(
            remoteProxySettings.getHttpsProxySettings().getHostname(),
            remoteProxySettings.getHttpsProxySettings().getPort()
        );
        applyAuthenticationConfig(
            builder, remoteProxySettings.getHttpsProxySettings().getProxyAuthentication(), httpsProxy
        );
        log.debug("https proxy setup with host '{}'", remoteProxySettings.getHttpsProxySettings().getHostname());
        proxies.put("https", httpsProxy);
      }

      final Set<Pattern> nonProxyHostPatterns = Sets.newHashSet();
      if (remoteProxySettings.getNonProxyHosts() != null && !remoteProxySettings.getNonProxyHosts().isEmpty()) {
        for (String nonProxyHostRegex : remoteProxySettings.getNonProxyHosts()) {
          try {
            nonProxyHostPatterns.add(Pattern.compile(nonProxyHostRegex, Pattern.CASE_INSENSITIVE));
          }
          catch (PatternSyntaxException e) {
            log.warn("Invalid non proxy host regex: {}", nonProxyHostRegex, e);
          }
        }
      }

      builder.getHttpClientBuilder().setRoutePlanner(
          new NexusHttpRoutePlanner(
              proxies, nonProxyHostPatterns, DefaultSchemePortResolver.INSTANCE
          )
      );
    }
  }
}
