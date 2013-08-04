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

package org.sonatype.nexus.plugins.p2.repository.internal;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.GlobalRemoteProxySettings;
import org.sonatype.nexus.configuration.application.events.GlobalRemoteProxySettingsChangedEvent;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteHttpProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.p2.bridge.HttpProxy;
import org.sonatype.plexus.appevents.Event;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class GlobalRemoteProxySettingsInspector
    extends AbstractLoggingComponent
    implements EventInspector
{

  private final GlobalRemoteProxySettings globalRemoteProxySettings;

  private final HttpProxy httpProxy;

  @Inject
  public GlobalRemoteProxySettingsInspector(final GlobalRemoteProxySettings globalRemoteProxySettings,
                                            final HttpProxy httpProxy)
  {
    this.globalRemoteProxySettings = checkNotNull(globalRemoteProxySettings);
    this.httpProxy = checkNotNull(httpProxy);
  }

  @Override
  public boolean accepts(final Event<?> evt) {
    return evt instanceof GlobalRemoteProxySettingsChangedEvent
        || evt instanceof NexusStartedEvent;
  }

  @Override
  public void inspect(final Event<?> evt) {
    if (!accepts(evt)) {
      return;
    }

    // FIXME: Sort out what to do with http/https here
    RemoteHttpProxySettings httpProxySettings = globalRemoteProxySettings.getHttpProxySettings();

    if (httpProxySettings != null && httpProxySettings.isEnabled()) {
      String username = null;
      String password = null;

      final RemoteAuthenticationSettings authentication = httpProxySettings.getProxyAuthentication();

      if (authentication != null
          && UsernamePasswordRemoteAuthenticationSettings.class.isAssignableFrom(authentication.getClass())) {
        username = ((UsernamePasswordRemoteAuthenticationSettings) authentication).getUsername();
        password = ((UsernamePasswordRemoteAuthenticationSettings) authentication).getPassword();
      }

      final String hostname = httpProxySettings.getHostname();
      final int port = httpProxySettings.getPort();
      final Set<String> nonProxyHosts = globalRemoteProxySettings.getNonProxyHosts();

      getLogger().debug(
          "Configure P2 proxy using global http proxy settings: hostname={}, port={}, username={}, nonProxyHosts={}",
          hostname, port, username, nonProxyHosts
      );

      httpProxy.setProxySettings(hostname, port, username, password, nonProxyHosts);
    }
    else {
      getLogger().debug("No global http proxy settings. Resetting P2 proxy.");
      httpProxy.setProxySettings(null, -1, null, null, null);
    }
  }

}
