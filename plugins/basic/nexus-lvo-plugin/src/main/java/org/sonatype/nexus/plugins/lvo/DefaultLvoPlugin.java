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

package org.sonatype.nexus.plugins.lvo;

import java.io.IOException;
import java.util.Map;

import org.sonatype.aether.util.version.GenericVersionScheme;
import org.sonatype.aether.version.InvalidVersionSpecificationException;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionScheme;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.lvo.config.LvoPluginConfiguration;
import org.sonatype.nexus.plugins.lvo.config.model.CLvoKey;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

import com.google.common.annotations.VisibleForTesting;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

@Component(role = LvoPlugin.class)
public class DefaultLvoPlugin
    extends AbstractLoggingComponent
    implements LvoPlugin
{
  private final VersionScheme versionScheme = new GenericVersionScheme();

  @Requirement
  private LvoPluginConfiguration lvoPluginConfiguration;

  @Requirement(role = DiscoveryStrategy.class)
  private Map<String, DiscoveryStrategy> strategies;

  /**
   * For plexus injection.
   */
  public DefaultLvoPlugin() {
  }

  @VisibleForTesting
  DefaultLvoPlugin(final LvoPluginConfiguration lvoPluginConfiguration,
                   final Map<String, DiscoveryStrategy> strategies)
  {
    this.lvoPluginConfiguration = lvoPluginConfiguration;
    this.strategies = strategies;
  }

  public DiscoveryResponse getLatestVersionForKey(String key)
      throws NoSuchKeyException,
             NoSuchStrategyException,
             NoSuchRepositoryException,
             IOException
  {
    if (lvoPluginConfiguration.isEnabled()) {
      CLvoKey info = lvoPluginConfiguration.getLvoKey(key);

      String strategyId = info.getStrategy();

      if (StringUtils.isEmpty(strategyId)) {
        // default value was 'index', not available anymore
        getLogger().warn("Misconfigured version check key '{}': strategy ID missing.", key);
        throw new NoSuchStrategyException(info.getStrategy());
      }

      if (strategies.containsKey(strategyId)) {
        DiscoveryStrategy strategy = strategies.get(strategyId);

        DiscoveryRequest req = new DiscoveryRequest(key, info);

        return strategy.discoverLatestVersion(req);
      }
      else {
        throw new NoSuchStrategyException(info.getStrategy());
      }
    }
    else {
      return getDisabledResponse();
    }
  }

  public DiscoveryResponse queryLatestVersionForKey(String key, String v)
      throws NoSuchKeyException,
             NoSuchStrategyException,
             NoSuchRepositoryException,
             IOException
  {
    if (lvoPluginConfiguration.isEnabled()) {
      DiscoveryResponse response = getLatestVersionForKey(key);

      if (!response.isSuccessful()) {
        // nothing to compare to
        return response;
      }

      // compare the two versions

      try {
        Version versionCurrent = versionScheme.parseVersion(v);
        Version versionReceived = versionScheme.parseVersion(response.getVersion());
        if (versionReceived.compareTo(versionCurrent) <= 0) {
          // version not newer
          response.setSuccessful(false);
        }
      }
      catch (InvalidVersionSpecificationException e) {
        getLogger().warn("Could not parse version ({}/{}/{})",
            new String[]{key, v, response.getVersion()});
        response.setSuccessful(false);
      }

      return response;
    }
    else {
      return getDisabledResponse();
    }
  }

  protected DiscoveryResponse getDisabledResponse() {
    DiscoveryResponse response = new DiscoveryResponse(null);

    response.setSuccessful(false);

    return response;
  }
}
