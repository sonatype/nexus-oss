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

package org.sonatype.nexus.proxy.storage.remote;

import org.sonatype.nexus.proxy.storage.remote.httpclient.HttpClientRemoteStorage;
import org.sonatype.nexus.util.SystemPropertiesHelper;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: for now, we have limited the RRS selection, but in future, this should be made dynamic!
 */
@Component(role = RemoteProviderHintFactory.class)
public class DefaultRemoteProviderHintFactory
    implements RemoteProviderHintFactory
{
  public final static String DEFAULT_HTTP_PROVIDER_KEY = "nexus.default.http.provider";

  public final static String DEFAULT_HTTP_PROVIDER_FORCED_KEY = "nexus.default.http.providerForced";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private Boolean httpProviderForced = null;

  protected synchronized boolean isHttpProviderForced() {
    if (httpProviderForced == null) {
      httpProviderForced = SystemPropertiesHelper.getBoolean(DEFAULT_HTTP_PROVIDER_FORCED_KEY, false);

      if (httpProviderForced) {
        logger.warn("HTTP Provider forcing is in effect (system property \""
            + DEFAULT_HTTP_PROVIDER_FORCED_KEY
            + "\" is set to \"true\"!), so regardless of your configuration, for HTTP RemoteRepositoryStorage the \""
            + getDefaultHttpRoleHint()
            +
            "\" provider will be used! Consider adjusting your configuration instead and stop using provider forcing.");
      }
    }

    return httpProviderForced;
  }

  public String getDefaultRoleHint(final String remoteUrl)
      throws IllegalArgumentException
  {
    if (StringUtils.isBlank(remoteUrl)) {
      throw new IllegalArgumentException("Remote URL cannot be null!");
    }

    final String remoteUrlLowered = remoteUrl.toLowerCase();

    if (remoteUrlLowered.startsWith("http:") || remoteUrlLowered.startsWith("https:")) {
      return getDefaultHttpRoleHint();
    }

    throw new IllegalArgumentException("No known remote repository storage provider for remote URL " + remoteUrl);
  }

  public String getRoleHint(final String remoteUrl, final String hint)
      throws IllegalArgumentException
  {
    if (StringUtils.isBlank(remoteUrl)) {
      throw new IllegalArgumentException("Remote URL cannot be null!");
    }

    final String remoteUrlLowered = remoteUrl.toLowerCase();

    if (remoteUrlLowered.startsWith("http:") || remoteUrlLowered.startsWith("https:")) {
      return getHttpRoleHint(hint);
    }

    if (StringUtils.isBlank(hint)) {
      throw new IllegalArgumentException("RemoteRepositoryStorage hint cannot be null!");
    }

    logger.debug("Returning supplied \"{}\" hint for remote URL {}.", remoteUrl, hint);

    return hint;
  }

  public String getDefaultHttpRoleHint() {
    return SystemPropertiesHelper.getString(DEFAULT_HTTP_PROVIDER_KEY, HttpClientRemoteStorage.PROVIDER_STRING);
  }

  public String getHttpRoleHint(final String hint) {
    if (isHttpProviderForced() || StringUtils.isBlank(hint)) {
      return getDefaultHttpRoleHint();
    }
    else {
      return hint;
    }
  }
}
