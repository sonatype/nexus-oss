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

package org.sonatype.nexus.proxy.utils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

import com.google.common.annotations.VisibleForTesting;
import org.codehaus.plexus.util.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
@Deprecated
public class DefaultUserAgentBuilder
    implements UserAgentBuilder
{
  private final Provider<SystemStatus> systemStatusProvider;

  /**
   * The edition, that will tell us is there some change happened with installation.
   */
  private String platformEditionShort;

  /**
   * The lazily calculated invariant part of the UserAgentString.
   */
  private String userAgentPlatformInfo;

  @Inject
  public DefaultUserAgentBuilder(final Provider<SystemStatus> systemStatusProvider) {
    this.systemStatusProvider = checkNotNull(systemStatusProvider);
  }

  @Override
  public String formatUserAgentString(final RemoteStorageContext ctx) {
    return ua(ctx).toString();
  }

  // ==

  @VisibleForTesting
  StringBuilder ua(final RemoteStorageContext ctx) {
    final StringBuilder buff = new StringBuilder(getUserAgentPlatformInfo());

    // user customization
    RemoteConnectionSettings remoteConnectionSettings = ctx.getRemoteConnectionSettings();

    if (!StringUtils.isEmpty(remoteConnectionSettings.getUserAgentCustomizationString())) {
      buff.append(" ").append(remoteConnectionSettings.getUserAgentCustomizationString());
    }

    return buff;
  }

  private synchronized String getUserAgentPlatformInfo() {
    SystemStatus status = systemStatusProvider.get();

    // Cache platform details or re-cache if the edition has changed
    if (userAgentPlatformInfo == null || !status.getEditionShort().equals(platformEditionShort)) {
      // track edition for cache invalidation
      platformEditionShort = status.getEditionShort();

      userAgentPlatformInfo =
          String.format("Nexus/%s (%s; %s; %s; %s; %s)",
              status.getVersion(),
              platformEditionShort,
              System.getProperty("os.name"),
              System.getProperty("os.version"),
              System.getProperty("os.arch"),
              System.getProperty("java.version"));
    }

    return userAgentPlatformInfo;
  }

}
